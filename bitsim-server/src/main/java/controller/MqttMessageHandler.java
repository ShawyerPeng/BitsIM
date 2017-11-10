//package controller;
//
//import org.eclipse.paho.client.mqttv3.*;
//import org.joda.time.DateTime;
//import org.springframework.integration.handler.AbstractMessageHandler;
//import org.springframework.messaging.Message;
//import org.springframework.util.Assert;
//
//import java.io.UnsupportedEncodingException;
//
///**
// * 订阅方法类
// */
//public class MqttMessageHandler extends AbstractMessageHandler implements MqttCallback {
//    private IMqttClient client;
//    private String topic;
//    private boolean messagesRetained;
//    private QualityOfService qualityOfService = QualityOfService.AT_LEAST_ONCE;
//
//    public MqttMessageHandler() {
//    }
//
//    public MqttMessageHandler(IMqttClient client, String topic) {
//        setClient(client);
//        setTopic(topic);
//    }
//
//    @Override
//    protected void onInit() throws Exception {
//        Assert.notNull(this.client, String.format("you must specify a valid %s instance! ", MqttClient.class.getName()));
//        Assert.hasText(this.topic, "you must specify a 'topic'");
//        Assert.notNull(this.qualityOfService, String.format("you must specify a non-null instance of the %s enum.", QualityOfService.class.getName()));
//    }
//
//    public void setClient(IMqttClient client) {
//        this.client = client;
//    }
//
//    public void setQualityOfService(QualityOfService qualityOfService) {
//        this.qualityOfService = qualityOfService;
//    }
//
//    public void setMessagesRetained(boolean messagesRetained) {
//        this.messagesRetained = messagesRetained;
//    }
//
//    public void setTopic(String topic) {
//        this.topic = topic;
//    }
//
//    public String getTopic() {
//        return topic;
//    }
//
//    public IMqttClient getClient() {
//        return client;
//    }
//
//    @Override
//    protected void handleMessageInternal(Message<?> message) throws Exception {
//        Object payload = message.getPayload();
//        Assert.isTrue(payload instanceof byte[], String.format("the payload for %s must be of type byte[]", getClass().getName()));
//        byte[] payloadOfBytes = (byte[]) payload;
//
//        client.publish(this.topic, payloadOfBytes, this.qualityOfService.ordinal(), this.messagesRetained);
//        // client.subscribe(MqttHeaders.TOPIC);
//        client.subscribe("a");
//        client.subscribe("b");
//        client.setCallback(this);
//    }
//
//    @Override
//    public void connectionLost(Throwable arg0) {
//        // 处理重连
//        logger.debug("开始重连......");
//        String time = (new DateTime()).toString();
//        while (true) {
//            try {
//                client.connect();
//                break;
//            } catch (MqttException e) {
//                e.printStackTrace();
//            }
//        }
//        MqttService.initMessage("重连：" + time);
//
//    }
//
//    @Override
//    public void deliveryComplete(IMqttDeliveryToken token) {
//        // TODO Auto-generated method stub
//    }
//
//    @Override
//    public void messageArrived(String topic, MqttMessage msg) {
//        try {
//            String content = new String(msg.getPayload(), "UTF-8");
//            logger.debug("主题：" + topic + "  内容：" + content);
//            IMsgHandle msgHandle = getHandle(topic);
//            msgHandle.handle(topic, content);
//            logger.debug("..................消息处理完成................");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 根据接收的topic，生成对应的消息处理对象
//     */
//    public IMsgHandle getHandle(String topic) {
//        IMsgHandle msgHandle = null;
//        switch (topic) {
//            case "/a":
//                msgHandle = SpringContextUtils.getBean("AaaMsgHandle", AaaMsgHandle.class);
//                break;
//            case "/b":
//                msgHandle = SpringContextUtils.getBean("BbbMsgHandle", BbbMsgHandle.class);
//                break;
//            return msgHandle;
//        }
//    }
//}
