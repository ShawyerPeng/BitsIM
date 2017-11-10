package protocol.message;

import io.netty.util.internal.StringUtil;

/**
 * 定义MQTT协议固定头部，并作为细分的message的基类
 * Message拆分为固定头部fixHeader，variableHeader，payload三部分
 */
public class Message {
    // 固定头部
    private final FixedHeader fixedHeader;
    // 可变头部
    private final Object variableHeader;
    // 荷载
    private final Object payload;

    /**
     * 初始化message
     */
    public Message(FixedHeader fixedHeader, Object variableHeader, Object payload) {
        this.fixedHeader = fixedHeader;
        this.variableHeader = variableHeader;
        this.payload = payload;
    }
    /**
     * 初始化message，针对没有可变头部和荷载的协议类型
     */
    public Message(FixedHeader fixedHeader) {
        this(fixedHeader, null, null);
    }
    /**
     * 初始化message，针对没有荷载的协议类型
     */
    public Message(FixedHeader fixedHeader, Object variableHeader) {
        this(fixedHeader, variableHeader, null);
    }

    public FixedHeader getFixedHeader() {
        return fixedHeader;
    }

    public Object getVariableHeader() {
        return variableHeader;
    }

    public Object getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(StringUtil.simpleClassName(this))
                .append("[")
                .append("fixedHeader=")
                .append(getFixedHeader() != null ? getFixedHeader().toString() : "")
                .append(",variableHeader=")
                .append(getVariableHeader() != null ? getVariableHeader().toString() : "")
                .append(",payload=")
                .append(getPayload() != null ? getPayload().toString() : "")
                .append("]");
        return stringBuilder.toString();
    }

}
