package service.impl;

import mapper.MessageMapper;
import model.Message;
import model.OfflineMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import service.MessageService;

import java.util.Date;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Override
    public Integer insertMessage(Message message) {
        return messageMapper.insert(message);
    }

    @Override
    public Integer deleteMessage(Message message) {
        Integer messageId = message.getMessageId();
        return messageMapper.deleteByPrimaryKey(messageId);
    }

    @Override
    public List<Message> selectAllMessage() {
        return messageMapper.selectAll();
    }

    @Override
    public List<Message> selectMessageByTime(Date startTime, Date endTime) {
        return messageMapper.selectByTime(startTime, endTime);
    }

    @Override
    public List<Message> selectMessageByFromToId(Integer fromId, Integer toId) {
        return messageMapper.selectByFromToId(fromId, toId);
    }

    @Override
    public List<Message> selectMessageByFromId(Integer fromId) {
        return messageMapper.selectByFromId(fromId);
    }

}
