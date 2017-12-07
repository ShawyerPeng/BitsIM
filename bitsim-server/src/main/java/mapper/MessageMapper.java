package mapper;

import model.Message;
import model.MessageExample;
import model.OfflineMessage;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface MessageMapper {
    int countByExample(MessageExample example);

    int deleteByExample(MessageExample example);

    int deleteByPrimaryKey(Integer messageId);

    int insert(Message record);

    int insertSelective(Message record);

    List<Message> selectByExample(MessageExample example);

    Message selectByPrimaryKey(Integer messageId);

    int updateByExampleSelective(@Param("record") Message record, @Param("example") MessageExample example);

    int updateByExample(@Param("record") Message record, @Param("example") MessageExample example);

    int updateByPrimaryKeySelective(Message record);

    int updateByPrimaryKey(Message record);

    List<Message> selectAll();

    List<Message> selectByTime(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<Message> selectByFromToId(@Param("fromId") Integer fromId, @Param("toId") Integer toId);

    List<Message> selectByFromId(@Param("fromId") Integer fromId);
}