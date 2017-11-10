//package controller;
//
//import org.apache.log4j.Logger;
//import org.eclipse.paho.client.mqttv3.IMqttClient;
//import org.springframework.context.annotation.AnnotationConfigApplicationContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.ImportResource;
//import org.springframework.integration.support.MessageBuilder;
//import org.springframework.messaging.MessageChannel;
//
//import static org.apache.log4j.Logger.getLogger;
//
///**
// * 客户端总体调用处理
// */
//public class MqttService {
//    private static final Logger logger = getLogger(MqttService.class);
//
//    private static AnnotationConfigApplicationContext context1 = new AnnotationConfigApplicationContext(MqttConfiguration.class);
//    private static AnnotationConfigApplicationContext context2 = new AnnotationConfigApplicationContext(MqttConfiguration.class);
//    private static AnnotationConfigApplicationContext context3 = new AnnotationConfigApplicationContext(MqttConfiguration.class);
//
//    private static final String TOPIC_CTRL_MSG = "/ctrlMsg";
//    private static final String TOPIC_HOST_MSG = "/hostMsg";
//
//    public static void initMessage(String content) {
//        reConnect(context1); //重连
//        logger.debug("------------initMessage start---------------");
//        MessageChannel messageChannel = context1.getBean("initMsgChannel", MessageChannel.class);
//        messageChannel.send(MessageBuilder.withPayload(content.getBytes()).build());
//    }
//
//    /**
//     * 发送控制方案信息通道
//     */
//    public static void sendCtrlMsg(String content) {
//        reConnect(context2); //重连
//        logger.debug("------------sendCtrlMsg ---------------");
//        MessageChannel messageChannel = context2.getBean("ctrlMsgChannel", MessageChannel.class);
//        messageChannel.send(MessageBuilder.withPayload(content.getBytes()).build());
//    }
//
//    /**
//     * 发送主机信息通道
//     */
//    public static void sendHostMsg(String content) {
//        reConnect(context3); //重连
//        logger.debug("------------sendHostMsg ---------------");
//        MessageChannel messageChannel = context3.getBean("hostMsgChannel",
//                MessageChannel.class);
//        messageChannel.send(MessageBuilder.withPayload(content.getBytes())
//                .build());
//    }
//
//    @Configuration
//    @ImportResource("classpath*:/applicationContext-mqtt.xml")
//    public static class MqttConfiguration {
//        @Bean
//        public MqttClientFactoryBean mqttClientFactoryBean() {
//            return new MqttClientFactoryBean("mqttx.gzdfbz.com");
//        }
//
//        @Bean
//        public MqttMessageHandler mqttInitMsg(IMqttClient client) {
//            return new MqttMessageHandler(client, "/status");
//        }
//        @Bean
//        public MqttSendingMessageHandler sendCtrlMsg(IMqttClient client) {
//            return new MqttSendingMessageHandler(client, TOPIC_CTRL_MSG);
//        }
//        @Bean
//        public MqttSendingMessageHandler sendHostMsg(IMqttClient client) {
//            return new MqttSendingMessageHandler(client, TOPIC_HOST_MSG);
//        }
//    }
//
//    /**
//     * 重连
//     */
//    private static void reConnect(AnnotationConfigApplicationContext context){
//        while(context==null){
//            logger.debug("-----reConnect()--------");
//            context = new AnnotationConfigApplicationContext(
//                    MqttConfiguration.class);
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}
