package protocol.process.subscribe;

import java.io.Serializable;

import protocol.message.QoS;

/**
 * 订阅的树节点，保存订阅的每个节点的信息
 */
public class Subscription implements Serializable {
    private static final long serialVersionUID = 1L;

    private QoS requestedQos; //max QoS acceptable
    private String topicFilter;
    private String clientId;
    private boolean cleanSession;
    private boolean active = true;

    public Subscription(String clientId, String topicFilter, QoS requestedQos, boolean cleanSession) {
        this.clientId = clientId;
        this.requestedQos = requestedQos;
        this.topicFilter = topicFilter;
        this.cleanSession = cleanSession;
    }

    public QoS getRequestedQos() {
        return requestedQos;
    }

    public void setRequestedQos(QoS requestedQos) {
        this.requestedQos = requestedQos;
    }

    public String getTopicFilter() {
        return topicFilter;
    }

    public void setTopicFilter(String topicFilter) {
        this.topicFilter = topicFilter;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    @Override
    public String toString() {
        return String.format("[filter:%s, clientId: %s, qos: %s, active: %s]", this.topicFilter, this.clientId, this.requestedQos, this.active);
    }
}