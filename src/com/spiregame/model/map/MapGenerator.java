package com.spiregame.model.map;

import java.util.*;

/**
 * ──────────────────────────────────────────────────────────────────
 * MapGenerator  —  분기 맵을 생성하는 클래스
 * ──────────────────────────────────────────────────────────────────
 *
 * [책임 한 줄 요약]
 *   MapNode 객체들을 만들고, 서로 연결해서 분기 그래프를 완성합니다.
 *   생성된 그래프는 GameController가 보관하고,
 *   MapScene은 그것을 읽어서 화면에 그립니다.
 *
 * [왜 별도 클래스로 분리했나?]
 *   맵 생성 규칙(층 수, 분기 수, 노드 타입 비율 등)은
 *   나중에 바꾸거나 확장할 가능성이 높습니다.
 *   GameController 안에 섞어두면 그 파일이 거대해지므로
 *   생성 로직만 따로 뽑아 단일 책임 원칙을 지켰습니다.
 *
 * [맵 구조 설계]
 *
 *   층(floor) 구성:
 *     1층       — 시작: 전투 노드 1~2개 (분기 선택 첫 포인트)
 *     2~4층     — 초반: 전투 위주, 보물 가끔
 *     5층       — 중간 보스 (Elite)
 *     6~9층     — 중반: 전투 + 상점 + 휴식
 *     10층      — 중간 보스 (Elite)
 *     11~14층   — 후반: 전투 + 엘리트 + 휴식
 *     15층      — 최종 보스 (Boss, 단일 노드로 수렴)
 *
 *   각 층당 노드 수: 2~3개 (랜덤)
 *   각 노드는 다음 층의 1~2개 노드와 연결됩니다.
 *
 * [UI 좌표 계산]
 *   화면 하단이 1층, 상단이 최종 보스입니다.
 *   같은 층의 노드들은 수평으로 균등 배치됩니다.
 *
 *   예시 (3층, 노드 2개):
 *     x = (총 너비 / (노드 수 + 1)) * (인덱스 + 1)
 *     → 300, 600  (총 너비 900 기준)
 */
public class MapGenerator {

    // ── 맵 설정 상수 ──────────────────────────────────────────────
    /** 총 층 수 (마지막 층은 항상 보스) */
    public static final int TOTAL_FLOORS = 15;

    /** 보스가 등장하는 층 번호 */
    public static final int BOSS_FLOOR   = 15;

    // ── UI 레이아웃 상수 ──────────────────────────────────────────
    /** 맵 패널의 가로 픽셀 너비 */
    private static final double MAP_WIDTH  = 600.0;

    /** 맵 패널의 세로 픽셀 높이 */
    private static final double MAP_HEIGHT = 620.0;

    /** 상단/하단 여백 */
    private static final double PADDING_Y  = 50.0;

    private final Random rng = new Random();

    // ══════════════════════════════════════════════════════════════
    //  공개 메서드: generate()
    // ══════════════════════════════════════════════════════════════

    /**
     * 전체 분기 맵을 생성해서 반환합니다.
     *
     * 반환값은 "층별 노드 목록의 리스트"입니다.
     *   result.get(0) → 1층 노드들
     *   result.get(14) → 15층 노드들 (보스, 1개)
     *
     * [처리 순서]
     *   1. 층마다 노드 생성 (타입 결정 포함)
     *   2. 층 간 엣지(연결선) 생성
     *   3. UI 좌표 계산
     *   4. 첫 번째 층 노드를 "선택 가능" 상태로 초기화
     */
    public List<List<MapNode>> generate() {

        // ── 1단계: 각 층의 노드 생성 ──────────────────────────────
        List<List<MapNode>> floors = new ArrayList<>();

        for (int f = 1; f <= TOTAL_FLOORS; f++) {
            List<MapNode> floorNodes = createFloorNodes(f);
            floors.add(floorNodes);
        }

        // ── 2단계: 층 간 연결선(엣지) 생성 ───────────────────────
        for (int f = 0; f < TOTAL_FLOORS - 1; f++) {
            connectFloors(floors.get(f), floors.get(f + 1));
        }

        // ── 3단계: UI 좌표 계산 ───────────────────────────────────
        assignCoordinates(floors);

        // ── 4단계: 1층 노드를 모두 선택 가능으로 초기화 ──────────
        for (MapNode node : floors.get(0)) {
            node.setAvailable(true);
        }

        return floors;
    }

    // ══════════════════════════════════════════════════════════════
    //  내부 메서드
    // ══════════════════════════════════════════════════════════════

    /**
     * 특정 층(floorNumber)에 배치할 노드들을 생성합니다.
     *
     * [노드 수 결정]
     *   - 1층 : 항상 2개 (첫 선택의 의미를 부여)
     *   - 보스층 : 항상 1개 (수렴)
     *   - 나머지 : 2~3개 랜덤
     *
     * [타입 결정]
     *   pickNodeType()에 층 번호를 넘겨서
     *   층이 높을수록 어려운 노드가 많이 나오도록 합니다.
     */
    private List<MapNode> createFloorNodes(int floorNumber) {
        List<MapNode> nodes = new ArrayList<>();

        // 노드 수 결정
        int count;
        if (floorNumber == 1)          count = 2;          // 시작층: 항상 2갈래
        else if (floorNumber == BOSS_FLOOR) count = 1;     // 보스층: 단일 노드
        else                           count = 2 + rng.nextInt(2); // 2 또는 3

        for (int col = 0; col < count; col++) {
            // ID 형식: "f{층번호}_c{열번호}" → 예: "f3_c1"
            String id   = "f" + floorNumber + "_c" + col;
            NodeType type = pickNodeType(floorNumber, col, count);
            nodes.add(new MapNode(id, type, floorNumber));
        }

        return nodes;
    }

    /**
     * 층 번호와 위치를 고려해 노드 타입을 결정합니다.
     *
     * [설계 의도]
     *   - 초반(1~4층) : 전투 위주, 보물 약간
     *   - 중반(5~9층) : 전투 + 상점 + 휴식 + 엘리트 가끔
     *   - 후반(10~14층): 전투 + 엘리트 + 휴식
     *   - 특수층(5, 10층): 반드시 엘리트 포함
     *   - 보스층(15층)   : 항상 보스
     *
     *   같은 층에 노드가 여러 개일 때,
     *   col(열 인덱스)을 활용해서 "적어도 하나는 상점/휴식"이
     *   나오도록 보장합니다. 이렇게 하면 플레이어가
     *   전투 말고 다른 선택지를 항상 가질 수 있습니다.
     */
    private NodeType pickNodeType(int floor, int col, int totalInFloor) {
        // 보스층은 무조건 보스
        if (floor == BOSS_FLOOR) return NodeType.BOSS;

        // 엘리트 보장 층 (5층, 10층)
        if ((floor == 5 || floor == 10) && col == 0) return NodeType.ELITE;

        // 여러 노드가 있는 층: 마지막 노드는 전투가 아닌 선택지를 보장
        if (totalInFloor >= 2 && col == totalInFloor - 1) {
            if (floor >= 6 && floor <= 9) {
                // 중반: 상점 또는 휴식
                return rng.nextBoolean() ? NodeType.SHOP : NodeType.REST;
            }
            if (floor >= 2 && floor <= 4) {
                // 초반: 보물 또는 상점
                return rng.nextBoolean() ? NodeType.TREASURE : NodeType.SHOP;
            }
            if (floor >= 11) {
                // 후반: 휴식 또는 엘리트
                return rng.nextBoolean() ? NodeType.REST : NodeType.ELITE;
            }
        }

        // 기본값: 전투
        // 초반은 전투가 많고, 후반은 엘리트 비율이 올라감
        if (floor >= 11 && rng.nextInt(4) == 0) return NodeType.ELITE;
        return NodeType.BATTLE;
    }

    /**
     * 현재 층(currentFloor)의 노드들을 다음 층(nextFloor)에 연결합니다.
     *
     * [연결 규칙]
     *   1. 모든 현재 노드가 최소 하나의 자식을 가져야 합니다.
     *   2. 모든 다음 층 노드가 최소 하나의 부모를 가져야 합니다.
     *      (고립된 노드가 생기면 그 경로로 진행이 불가능해집니다)
     *   3. 자연스러운 분기를 위해 인접한 열끼리 우선 연결합니다.
     *
     * [알고리즘]
     *   a) 먼저 현재 층 노드 수:다음 층 노드 수를 비율로 매핑 (자연스러운 연결)
     *   b) 다음 층 노드 중 아직 부모가 없는 노드에 연결 추가 (고립 방지)
     *   c) 각 현재 노드에서 랜덤하게 추가 연결 1~2개 (풍성한 분기)
     */
    private void connectFloors(List<MapNode> currentFloor, List<MapNode> nextFloor) {
        int curSize  = currentFloor.size();
        int nextSize = nextFloor.size();

        // ── a) 인접 매핑으로 기본 연결 ────────────────────────────
        // 현재 층 노드 i → 다음 층의 대응 인덱스를 비례 계산
        // 예: 현재 3개, 다음 2개라면 0→0, 1→0 또는 1, 2→1
        for (int i = 0; i < curSize; i++) {
            // 비례 인덱스: 현재 노드의 위치를 다음 층 크기로 스케일
            int targetIdx = (int) Math.round((double) i / (curSize - 1) * (nextSize - 1));
            // curSize가 1일 때 0/0 나누기 방지
            if (curSize == 1) targetIdx = 0;
            currentFloor.get(i).addChild(nextFloor.get(targetIdx));
        }

        // ── b) 고립된 다음 층 노드 구제 ──────────────────────────
        // 부모가 하나도 없는 노드는 게임에서 절대 방문 불가하므로
        // 가장 가까운 현재 층 노드에서 강제로 연결
        for (int j = 0; j < nextSize; j++) {
            if (nextFloor.get(j).getParents().isEmpty()) {
                // 가장 가까운 현재 층 노드 인덱스 계산
                int srcIdx = Math.min(j, curSize - 1);
                currentFloor.get(srcIdx).addChild(nextFloor.get(j));
            }
        }

        // ── c) 추가 분기 연결 (자연스러운 분기감) ─────────────────
        // 각 현재 노드에서 인접 다음 층 노드로 추가 연결 (확률적)
        for (int i = 0; i < curSize; i++) {
            MapNode cur = currentFloor.get(i);

            // 인접한 다음 층 노드 범위: (i-1) ~ (i+1)을 nextSize 범위로 클램프
            int lo = Math.max(0, i - 1);
            int hi = Math.min(nextSize - 1, i + 1);

            for (int j = lo; j <= hi; j++) {
                // 50% 확률로 추가 연결 (너무 많으면 맵이 복잡해짐)
                if (rng.nextBoolean()) {
                    cur.addChild(nextFloor.get(j));
                }
            }
        }
    }

    /**
     * 각 노드의 화면 좌표(x, y)를 계산해서 설정합니다.
     *
     * [좌표 체계]
     *   - Y축: 1층 = 화면 하단, BOSS층 = 화면 상단
     *          (아래에서 위로 올라가는 게임 감각에 맞춤)
     *   - X축: 같은 층 노드들을 수평으로 균등 분배
     *
     * [계산 공식]
     *   y = PADDING_Y + (BOSS층 - 현재층) / (총층수 - 1) * (MAP_HEIGHT - 2*PADDING_Y)
     *   x = MAP_WIDTH / (노드수 + 1) * (열인덱스 + 1)
     */
    private void assignCoordinates(List<List<MapNode>> floors) {
        int totalFloors = floors.size(); // = TOTAL_FLOORS

        for (int f = 0; f < totalFloors; f++) {
            List<MapNode> floorNodes = floors.get(f);
            int floorNumber = f + 1; // 1-based 층 번호

            // Y 좌표: 1층이 아래, BOSS층이 위
            // floorNumber=1 → y 최대, floorNumber=TOTAL_FLOORS → y 최소(PADDING_Y)
            double y = PADDING_Y
                + (double)(TOTAL_FLOORS - floorNumber) / (TOTAL_FLOORS - 1)
                * (MAP_HEIGHT - 2 * PADDING_Y);

            int nodeCount = floorNodes.size();
            for (int col = 0; col < nodeCount; col++) {
                // X 좌표: 노드들을 수평으로 균등 배치
                // (nodeCount+1) 로 나눠서 양쪽 여백을 균등하게 줌
                double x = MAP_WIDTH / (nodeCount + 1.0) * (col + 1);
                floorNodes.get(col).setX(x);
                floorNodes.get(col).setY(y);
            }
        }
    }
}
