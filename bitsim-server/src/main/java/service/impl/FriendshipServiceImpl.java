package service.impl;

import mapper.FriendshipMapper;
import model.Friendship;
import model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import service.FriendshipService;

import java.util.List;

@Service
public class FriendshipServiceImpl implements FriendshipService {
    @Autowired
    private FriendshipMapper friendshipMapper;

    @Override
    public Integer insert(Friendship friendship) {
        return friendshipMapper.insertSelective(friendship);
    }

    @Override
    public Integer delete(Integer id) {
        return friendshipMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<Friendship> selectByUserId(Integer userId) {
        return friendshipMapper.selectByUserId(userId);
    }
}