package service.impl;

import mapper.OfflineMessageMapper;
import model.OfflineMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.OfflineMessageService;

import java.util.Date;
import java.util.List;

@Service
public class OfflineMessageServiceImpl implements OfflineMessageService {
    @Autowired
    private OfflineMessageMapper offlineMessageMapper;

    @Override
    public Integer insertOfflineMessage(OfflineMessage offlineMessage) {
        return offlineMessageMapper.insert(offlineMessage);
    }

    @Override
    public Integer deleteOfflineMessage(OfflineMessage offlineMessage) {
        Integer offlineMessageId = offlineMessage.getOfflineMessageId();
        return offlineMessageMapper.deleteByPrimaryKey(offlineMessageId);
    }

    @Override
    public List<OfflineMessage> selectAllOfflineMessage(Date startTime, Date endTime) {
        return offlineMessageMapper.selectAll();
    }

    @Override
    public List<OfflineMessage> selectOfflineMessage(Date startTime, Date endTime) {
        return offlineMessageMapper.selectByTime(startTime, endTime);
    }
}
