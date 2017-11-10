package controller;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;

/**
 * Spring Integration {@link AbstractMessageHandler message handler} that delivers Spring Integration messages
 */
public class MqttSendingMessageHandler extends AbstractMessageHandler {
    private IMqttClient client;
    private String topic;
    private boolean messagesRetained;
    private QualityOfService qualityOfService = QualityOfService.AT_MOST_ONCE;

    public MqttSendingMessageHandler(IMqttClient client, String topic) {
        setClient(client);
        setTopic(topic);
    }

    @Override
    protected void onInit() throws Exception {
        Assert.notNull(this.client, String.format("you must specify a valid %s instance! ", MqttClient.class.getName()));
        Assert.hasText(this.topic, "you must specify a 'topic'");
        Assert.notNull(this.qualityOfService, String.format("you must specify a non-null instance of the %s enum.", QualityOfService.class.getName()));
    }

    public void setClient(IMqttClient client) {
        this.client = client;
    }

    public void setQualityOfService(QualityOfService qualityOfService) {
        this.qualityOfService = qualityOfService;
    }

    public void setMessagesRetained(boolean messagesRetained) {
        this.messagesRetained = messagesRetained;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    protected void handleMessageInternal(Message<?> message) throws Exception {
        Object payload = message.getPayload();
        Assert.isTrue(payload instanceof byte[], String.format("the payload for %s must be of type byte[]", getClass().getName()));
        byte[] payloadOfBytes = (byte[]) payload;

        MessageHeaders messageHeaders = message.getHeaders();

        // todo is this a thing?
        String topicForThisMessage = this.topic;
        if (messageHeaders.containsKey(MqttHeaders.TOPIC)) {
            topicForThisMessage = (String) messageHeaders.get(MqttHeaders.TOPIC);
        }

        // todo should we support mapping other things like qos? messagesRetained?

        this.client.publish(topicForThisMessage, payloadOfBytes, this.qualityOfService.ordinal(), this.messagesRetained);
    }
}
