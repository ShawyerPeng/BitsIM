package protocol.process;

import io.netty.channel.Channel;

/**
 * 此类是每个客户端的会话，客户端ID，cleanSession的保存
 */
public class ConnectionDescriptor {
    private String clientId;
    private Channel client;
    private boolean cleanSession;

    public ConnectionDescriptor(String clientId, Channel session, boolean cleanSession) {
        this.clientId = clientId;
        this.client = session;
        this.cleanSession = cleanSession;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Channel getClient() {
        return client;
    }

    public void setClient(Channel client) {
        this.client = client;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    @Override
    public String toString() {
        return "ConnectionDescriptor{" + "m_clientId=" + clientId + ", m_cleanSession=" + cleanSession + '}';
    }
}