package controller;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;

/**
 * 入口方法
 */
public class Main {
    public static void main(String[] args) throws Throwable {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MqttConfiguration.class);

        MessageChannel messageChannel = context.getBean("messages", MessageChannel.class);
        messageChannel.send(MessageBuilder.withPayload("Josh and Andy say hi!".getBytes()).build());
    }

    @Configuration
    @ImportResource("spring/spring-mqtt.xml")
    public static class MqttConfiguration {
        @Bean
        public MqttClientFactoryBean mqttClientFactoryBean() {
            return new MqttClientFactoryBean("m2m.eclipse.org", 1883, "test", "test");
        }

        @Bean
        public MqttSendingMessageHandler mqttSendingMessageHandler(IMqttClient client) {
            return new MqttSendingMessageHandler(client, "cats");
        }
    }
}