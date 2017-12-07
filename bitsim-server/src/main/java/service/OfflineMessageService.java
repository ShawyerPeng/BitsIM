package service;

import model.OfflineMessage;

import java.util.Date;
import java.util.List;

public interface OfflineMessageService {
    Integer insertOfflineMessage(OfflineMessage offlineMessage);

    Integer deleteOfflineMessage(OfflineMessage offlineMessage);

    List<OfflineMessage> selectAllOfflineMessage(Date startTime, Date endTime);

    List<OfflineMessage> selectOfflineMessage(Date startTime, Date endTime);
}