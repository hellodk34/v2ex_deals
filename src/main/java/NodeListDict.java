
/**
 * @param * @param null:
 * @author hellodk
 * @description 关注的 node list dict
 * @date 9/15/2021 12:36 PM
 * @return
 */
public enum NodeListDict {

    flamewar("水深火热", "flamewar"),
    car("汽车", "car"),
    dns("DNS", "dns"),
    bike("骑行", "bike"),
    nanjing("南京", "nanjing"),
    deals("优惠信息", "deals"),
    bb("宽带症候群", "bb");

    private String nodeName;
    private String nodeCode;

    NodeListDict(String nodeName, String nodeCode) {
        this.nodeName = nodeName;
        this.nodeCode = nodeCode;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeCode() {
        return nodeCode;
    }

    public void setNodeCode(String nodeCode) {
        this.nodeCode = nodeCode;
    }

    /**
     * @param * @param nodeCode:
     * @return java.lang.Boolean
     * @author hellodk
     * @description 当前节点是否匹配，匹配则添加到返回 body
     * @date 9/15/2021 12:44 PM
     */
    public static Boolean nodeCodeMatch(String nodeCode) {
        for (NodeListDict item : NodeListDict.values()) {
            if (item.getNodeCode().equals(nodeCode)) {
                return true;
            }
        }
        return false;
    }
}
