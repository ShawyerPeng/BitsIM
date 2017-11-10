package serialization.pojo;

public class Message {
    public final static int MQTT_CONNECT = 1;        // 请求连接
    public final static int MQTT_CONNACK = 2;        // 请求应答
    public final static int MQTT_PUBLISH = 3;        // 发布消息
    public final static int MQTT_PUBACK  = 4;        // 发布应答
    public final static int MQTT_PUBREC  = 5;        // 发布已接收，保证传递1
    public final static int MQTT_PUBREL  = 6;        // 发布释放，保证传递2
    public final static int MQTT_PUBCOMP = 7;        // 发布完成，保证传递3
    public final static int MQTT_SUBSCRIBE = 8;      // 订阅请求
    public final static int MQTT_SUBACK = 9;         // 订阅应答
    public final static int MQTT_UNSUBSCRIBE = 10;   // 取消订阅
    public final static int MQTT_UNSUBACK = 11;      // 取消订阅应答
    public final static int MQTT_PINGREQ = 12;       // ping请求(心跳请求)
    public final static int MQTT_PINGRESP = 13;      // ping响应(心跳响应)
    public final static int MQTT_DISCONNECT = 14;    // 断开连接
}
