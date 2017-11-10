package protocol.message;

import protocol.message.ConnAckMessage.ConnectionStatus;

/**
 * MQTT协议ConnAck消息类型的可变头部
 */
public class ConnAckVariableHeader {
    private ConnectionStatus status;    // 返回给客户端的状态码
    private Boolean sessionPresent;     // sessionPresent是告知客户端服务器是否存储了session的位

    public ConnAckVariableHeader(ConnectionStatus status, Boolean sessionPresent) {
        this.status = status;
        this.sessionPresent = sessionPresent;
    }

    public ConnectionStatus getStatus() {
        return status;
    }

    public void setStatus(ConnectionStatus status) {
        this.status = status;
    }

    public Boolean isSessionPresent() {
        return sessionPresent;
    }

    public void setSessionPresent(Boolean sessionPresent) {
        this.sessionPresent = sessionPresent;
    }
}
