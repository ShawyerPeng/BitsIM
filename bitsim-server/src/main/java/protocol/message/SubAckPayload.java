package protocol.message;

import java.util.List;

/**
 * MQTT协议SubAck消息类型的荷载
 */
public class SubAckPayload {
    // 返回码清单，每个返回码对应等待确认的 SUBSCRIBE 报文中的一个主题过滤器
    private List<Integer> grantedQosLevel;

    public SubAckPayload(List<Integer> grantedQosLevel) {
        this.grantedQosLevel = grantedQosLevel;
    }

    public List<Integer> getGrantedQosLevel() {
        return grantedQosLevel;
    }

}
