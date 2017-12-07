package mapper;

import model.Friendship;
import model.FriendshipExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FriendshipMapper {
    int countByExample(FriendshipExample example);

    int deleteByExample(FriendshipExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Friendship record);

    int insertSelective(Friendship record);

    List<Friendship> selectByExample(FriendshipExample example);

    Friendship selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Friendship record, @Param("example") FriendshipExample example);

    int updateByExample(@Param("record") Friendship record, @Param("example") FriendshipExample example);

    int updateByPrimaryKeySelective(Friendship record);

    int updateByPrimaryKey(Friendship record);

    List<Friendship> selectByUserId(Integer userId);
}