package protocol.process.store;

/**
 * ClientSession 存储类
 */
public class ClientSession {
    private final String clientId;
    private short nextMessageId = 1;
    private volatile boolean cleanSession;
    private boolean active = false;

    public ClientSession(String clientId, boolean cleanSession) {
        this.clientId = clientId;
        this.cleanSession = cleanSession;
    }

    public String getClientId() {
        return clientId;
    }

    public short getNextMessageId() {
        short rc = nextMessageId;
        nextMessageId++;
        if (nextMessageId == 0) {
            nextMessageId = 1;
        }
        return rc;
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
}
