package protocol.process.store;

public enum TopicType {
    GROUP("g"), // for 群组
    FRIEND("f"),// for 朋友
    MYSELF("m"),// for 自己，属于问答模式
    SYS("s"),   // 系统内部消息，包括 syn，push
    OTHER("o"); // 其他

    private String type;

    TopicType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}