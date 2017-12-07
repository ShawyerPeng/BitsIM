package protocol.process;

import io.netty.channel.Channel;

/**
 * 此类是每个客户端的会话，客户端ID，cleanSession的保存
 */
public class ConnectionDescriptor {
    private final String clientId;
    private final Channel client;
    private final boolean cleanSession;

    public ConnectionDescriptor(String clientId, Channel session, boolean cleanSession) {
        this.clientId = clientId;
        this.client = session;
        this.cleanSession = cleanSession;
    }

    public String getClientId() {
        return clientId;
    }

    public Channel getClient() {
        return client;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    @Override
    public String toString() {
        return "ConnectionDescriptor{" +
                "clientId='" + clientId + '\'' +
                ", client=" + client +
                ", cleanSession=" + cleanSession +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectionDescriptor that = (ConnectionDescriptor) o;

        if (clientId != null ? !clientId.equals(that.clientId) : that.clientId != null) return false;
        return !(client != null ? !client.equals(that.client) : that.client != null);
    }

    @Override
    public int hashCode() {
        int result = clientId != null ? clientId.hashCode() : 0;
        result = 31 * result + (client != null ? client.hashCode() : 0);
        return result;
    }
}