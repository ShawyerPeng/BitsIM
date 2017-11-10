package protocol.message;

import java.util.List;

/**
 * MQTT协议Connect消息类型的荷载
 */
public class UnsubscribePayload {
    // 客户端想要取消订阅的主题过滤器列表
    private List<String> topics;

    public UnsubscribePayload(List<String> topics) {
        super();
        this.topics = topics;
    }

    public List<String> getTopics() {
        return topics;
    }
}