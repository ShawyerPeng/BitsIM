package service;

import model.Message;

import java.util.Date;
import java.util.List;

public interface MessageService {
    Integer insertMessage(Message message);

    Integer deleteMessage(Message message);

    List<Message> selectAllMessage();

    List<Message> selectMessageByTime(Date startTime, Date endTime);

    List<Message> selectMessageByFromToId(Integer fromId, Integer toId);

    List<Message> selectMessageByFromId(Integer fromId);
}