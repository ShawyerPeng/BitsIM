package controller;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * MQTT 客户端连接工厂，实现初始化配置类接口，目的是能够在启动时加载
 */
public class MqttClientFactoryBean implements InitializingBean, FactoryBean<IMqttClient> {
    private static Logger logger = Logger.getLogger(MqttClientFactoryBean.class);

    // TCP协议
    private static String TCP_PROTOCOL = "tcp://";
    // SSL协议
    private static String SSL_PROTOCOL = "ssl://";
    // 协议类型
    private String protocol = TCP_PROTOCOL;
    // 是否使用SSL
    private boolean useSsl = false;
    // 客户端IP
    private String host;
    // 端口号
    private int port = 1883;
    // 客户端标识
    private String clientId = buildClientId();
    // MQTT消息存储
    private MqttClientPersistence mqttClientPersistence;
    // 用户名
    private String username;
    // 密码
    private String password;
    // MQTT连接选项
    private MqttConnectOptions mqttConnectOptions;
    // 是否清除Session
    private Boolean cleanSession = null;

    public MqttClientFactoryBean() {
    }

    public MqttClientFactoryBean(String host) {
        setup(host, this.username, this.password);
    }

    public MqttClientFactoryBean(String host, String username, String password) {
        setup(host, username, password);
    }

    public MqttClientFactoryBean(String host, int port, String username, String password) {
        setup(host, username, password);
        this.setPort(port);
    }

    private void setup(String host, String username, String password) {
        setHost(host);
        setUsername(username);
        setPassword(password);
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public void setPassword(String p) {
        this.password = p;
    }

    public void setUsername(String u) {
        this.username = u;
    }

    public void setMqttConnectOptions(MqttConnectOptions mqttConnectOptions) {
        this.mqttConnectOptions = mqttConnectOptions;
    }

    public void setClientId(String c) {
        this.clientId = c;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setUseSsl(boolean useSsl) {
        this.useSsl = useSsl;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setMqttClientPersistence(MqttClientPersistence mqttClientPersistence) {
        this.mqttClientPersistence = mqttClientPersistence;
    }

    @Override
    public IMqttClient getObject() throws Exception {
        String serverUri = buildServerUri();
        MqttClient client = this.mqttClientPersistence == null ?
                new MqttClient(serverUri, clientId) :
                new MqttClient(serverUri, clientId, mqttClientPersistence);
        MqttConnectOptions connectOptions = this.buildMqttConnectionOptions();
        if (null != connectOptions) {
            client.connect(connectOptions);
        } else {
            client.connect();
        }
        return client;
    }

    @Override
    public Class<?> getObjectType() {
        return IMqttClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(this.protocol, String.format("you must specify a non-null protocol value (either %s or %s)", SSL_PROTOCOL, TCP_PROTOCOL));
        Assert.isTrue(this.protocol.equalsIgnoreCase(SSL_PROTOCOL) || this.protocol.equalsIgnoreCase(TCP_PROTOCOL), "");
        Assert.hasText(this.clientId, "your clientId must be non-null");
        Assert.hasText(this.host, "you must specify a valid host");
        Assert.isTrue(this.port > 0, "you must specify a valid port");
        boolean connectionOptionsAreCorrectlySpecified =
                this.mqttConnectOptions != null && weShouldCreateConnectionOptions();
        Assert.isTrue(!connectionOptionsAreCorrectlySpecified,
                String.format("you must specify an instance of %s for the 'buildMqttConnectionOptions' attribute" +
                        " OR any of the following options ('cleanSession', 'username', 'password'), but not both!", MqttConnectOptions.class.getName()));
    }

    protected String buildServerUri() {
        if (this.useSsl) {
            this.protocol = SSL_PROTOCOL;
        }
        return this.protocol + this.host + ":" + this.port;
    }

    protected boolean weShouldCreateConnectionOptions() {
        return (this.cleanSession != null || StringUtils.hasText(this.username) || StringUtils.hasText(this.password));
    }

    protected String buildClientId() {
        String user = System.getProperty("user.name");
        int totalLength = 23;
        int userLength = user.length();
        if (userLength > 10) {
            user = user.substring(0, 10);
        }

        String clientId = user + System.currentTimeMillis();
        Assert.isTrue(clientId.length() <= totalLength);
        return clientId;
    }

    protected MqttConnectOptions buildMqttConnectionOptions() {
        MqttConnectOptions connectOptions = null;
        if (weShouldCreateConnectionOptions()) {
            connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(this.cleanSession);
            connectOptions.setUserName(this.username);
            connectOptions.setPassword(this.password.toCharArray());
        } else if (this.mqttConnectOptions != null) {
            connectOptions = this.mqttConnectOptions;
        }
        return connectOptions;
    }
}
