package protocol.process.event;

import java.io.Serializable;

/**
 * PubRel的事件类，只有Qos=2的时候才会有此事件
 */
public class PubRelEvent implements Serializable {
    private String clientId;
    private int packetId;

    public PubRelEvent(String clientId, int packetId) {
        this.clientId = clientId;
        this.packetId = packetId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getPacketId() {
        return packetId;
    }

    public void setPacketId(int packetId) {
        this.packetId = packetId;
    }
}