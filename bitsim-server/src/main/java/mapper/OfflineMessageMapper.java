package mapper;

import model.OfflineMessage;
import model.OfflineMessageExample;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface OfflineMessageMapper {
    int countByExample(OfflineMessageExample example);

    int deleteByExample(OfflineMessageExample example);

    int deleteByPrimaryKey(Integer offlineMessageId);

    int insert(OfflineMessage record);

    int insertSelective(OfflineMessage record);

    List<OfflineMessage> selectByExample(OfflineMessageExample example);

    OfflineMessage selectByPrimaryKey(Integer offlineMessageId);

    int updateByExampleSelective(@Param("record") OfflineMessage record, @Param("example") OfflineMessageExample example);

    int updateByExample(@Param("record") OfflineMessage record, @Param("example") OfflineMessageExample example);

    int updateByPrimaryKeySelective(OfflineMessage record);

    int updateByPrimaryKey(OfflineMessage record);

    List<OfflineMessage> selectAll();

    List<OfflineMessage> selectByTime(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
}