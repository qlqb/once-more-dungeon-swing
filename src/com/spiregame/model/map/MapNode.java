package com.spiregame.model.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ──────────────────────────────────────────────────────────────────
 * MapNode  —  맵 위의 노드(칸) 하나를 표현하는 클래스
 * ──────────────────────────────────────────────────────────────────
 *
 * [왜 이 클래스가 필요한가?]
 *   기존 코드는 "현재 층 번호(int floor)" 하나로 위치를 나타냈습니다.
 *   하지만 분기 맵에서는 같은 층에 여러 노드가 존재하므로
 *   "노드 객체" 단위로 위치와 연결 관계를 관리해야 합니다.
 *
 * [분기 구조를 어떻게 표현하나?]
 *   children 리스트 : 이 노드에서 갈 수 있는 다음 노드들 (아래 방향)
 *   parents  리스트 : 이 노드로 이어지는 이전 노드들 (위 방향)
 *
 *   예시 :
 *       [전투A] ──┬──> [상점C]
 *       [전투B] ──┘
 *   → 상점C의 parents = [전투A, 전투B]
 *   → 전투A의 children = [상점C]
 *
 * [x, y 좌표란?]
 *   MapScene(화면)에서 노드 원을 그릴 때 사용하는 픽셀 좌표입니다.
 *   게임 로직과 무관하며, 오직 UI 렌더링에만 쓰입니다.
 *
 * [available 플래그란?]
 *   현재 플레이어가 "클릭해서 이동할 수 있는" 노드인지 여부입니다.
 *   부모 노드를 방문한 경우에만 true가 됩니다.
 */
public class MapNode {

    // ── 식별자 ────────────────────────────────────────────────────
    /** 노드 고유 ID. "floor_column" 형식으로 MapGenerator가 할당합니다. */
    private final String id;

    /** 이 노드가 전투인지 상점인지 등을 나타냅니다. */
    private final NodeType type;

    /** 이 노드가 속한 층 번호 (1 = 첫 번째 층, BOSS_FLOOR = 마지막 층) */
    private final int floor;

    // ── 그래프 연결 ───────────────────────────────────────────────
    /**
     * 이 노드에서 갈 수 있는 다음 노드 목록.
     * 분기의 핵심 — 보통 1~3개가 됩니다.
     */
    private final List<MapNode> children = new ArrayList<>();

    /**
     * 이 노드로 이어지는 이전 노드 목록.
     * UI에서 "어디서 왔는가" 선을 그릴 때 활용합니다.
     */
    private final List<MapNode> parents = new ArrayList<>();

    // ── UI용 좌표 ─────────────────────────────────────────────────
    /** MapScene이 노드 원을 그릴 픽셀 X 좌표 */
    private double x;

    /** MapScene이 노드 원을 그릴 픽셀 Y 좌표 */
    private double y;

    // ── 상태 플래그 ───────────────────────────────────────────────
    /** 플레이어가 이미 이 노드를 방문했는가? */
    private boolean visited;

    /**
     * 플레이어가 지금 이 노드를 선택할 수 있는가?
     * - 시작 노드들: 처음부터 true
     * - 나머지: 부모 노드를 방문하면 true로 바뀜
     */
    private boolean available;

    // ── 생성자 ────────────────────────────────────────────────────
    public MapNode(String id, NodeType type, int floor) {
        this.id    = id;
        this.type  = type;
        this.floor = floor;
    }

    // ── 연결 관계 편의 메서드 ─────────────────────────────────────

    /**
     * child를 이 노드의 자식으로 연결합니다.
     * 동시에 child의 부모로도 this를 등록해 양방향 참조를 유지합니다.
     *
     * [왜 양방향?]
     *   - children : "다음에 어디로 갈 수 있나" → 플레이어 이동 로직에 사용
     *   - parents  : "어디서 왔나" → UI에서 연결선을 그릴 때 사용
     */
    public void addChild(MapNode child) {
        if (!children.contains(child)) {
            children.add(child);
            child.parents.add(this);   // 역방향도 동시 등록
        }
    }

    /**
     * 이 노드를 방문 처리하고,
     * 연결된 자식 노드들을 모두 선택 가능 상태로 만듭니다.
     *
     * [게임 흐름]
     *   플레이어가 노드에 들어와 이벤트를 마치면 GameController가 이 메서드를 호출합니다.
     *   그러면 다음 층의 연결된 노드들이 클릭 가능해집니다.
     */
    public void visit() {
        this.visited = true;
        // 모든 자식 노드를 "선택 가능" 상태로 변경
        for (MapNode child : children) {
            child.setAvailable(true);
        }
    }

    // ── Getter / Setter ───────────────────────────────────────────
    public String   getId()        { return id; }
    public NodeType getType()      { return type; }
    public int      getFloor()     { return floor; }
    public double   getX()         { return x; }
    public double   getY()         { return y; }
    public boolean  isVisited()    { return visited; }
    public boolean  isAvailable()  { return available; }

    public void setX(double x)              { this.x = x; }
    public void setY(double y)              { this.y = y; }
    public void setAvailable(boolean value) { this.available = value; }

    /** 읽기 전용 자식 목록 반환 (외부에서 직접 수정 불가) */
    public List<MapNode> getChildren() { return Collections.unmodifiableList(children); }

    /** 읽기 전용 부모 목록 반환 */
    public List<MapNode> getParents()  { return Collections.unmodifiableList(parents); }

    @Override
    public String toString() {
        return "[" + type.getDisplayName() + " F" + floor + " id=" + id + "]";
    }
}
