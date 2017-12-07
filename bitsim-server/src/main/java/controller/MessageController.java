package controller;

import model.Message;
import model.OfflineMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import service.MessageService;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/message")
public class MessageController {
    //@Resource
    //private MqttPahoMessageHandler mqtt;
    @Autowired
    private MessageService messageService;

    //@RequestMapping(value = "/send")
    //@ResponseBody
    //public void sendMessage() {
    //    Message<String> message = MessageBuilder.withPayload("===================")
    //            .setHeader(MqttHeaders.TOPIC, "robot_server").build();
    //    mqtt.handleMessage(message);
    //    System.out.println("成功");
    //}

    @RequestMapping(value = "/insert")
    @ResponseBody
    public Integer insert(@RequestBody Message message) {
        return messageService.insertMessage(message);
    }

    @RequestMapping(value = "/delete")
    @ResponseBody
    public Integer delete(@RequestBody Message message) {
        return messageService.deleteMessage(message);
    }

    @RequestMapping(value = "/selectAll")
    @ResponseBody
    public List<Message> selectAll() {
        return messageService.selectAllMessage();
    }

    @RequestMapping(value = "/selectByTime")
    @ResponseBody
    public List<Message> selectByTime(@RequestParam("startTime") Date startTime, @RequestParam("endTime") Date endTime) {
        return messageService.selectMessageByTime(startTime, endTime);
    }

    @RequestMapping(value = "/selectByFromToId")
    @ResponseBody
    public List<Message> selectByFromToId(@RequestParam("fromId") Integer fromId, @RequestParam("toId") Integer toId) {
        return messageService.selectMessageByFromToId(fromId, toId);
    }

    @RequestMapping(value = "/selectByFromId")
    @ResponseBody
    public List<Message> selectByFromId(@RequestParam("fromId") Integer fromId) {
        return messageService.selectMessageByFromId(fromId);
    }
}