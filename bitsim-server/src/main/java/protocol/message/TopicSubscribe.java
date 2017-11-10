package protocol.message;

/**
 * Subscribe荷载的封装，一个topic和一个qos是一组荷载
 */
public class TopicSubscribe {
    // 主题过滤器（Topic Filter）
    private String topicFilter;
    // 服务质量要求（Requested QoS）
    private QoS requestedQoS;

    public TopicSubscribe(String topicFilter, QoS requestedQoS) {
        super();
        this.topicFilter = topicFilter;
        this.requestedQoS = requestedQoS;
    }

    public String getTopicFilter() {
        return topicFilter;
    }

    public void setTopicFilter(String topicFilter) {
        this.topicFilter = topicFilter;
    }

    public QoS getRequestedQoS() {
        return requestedQoS;
    }

    public void setRequestedQoS(QoS requestedQoS) {
        this.requestedQoS = requestedQoS;
    }
}