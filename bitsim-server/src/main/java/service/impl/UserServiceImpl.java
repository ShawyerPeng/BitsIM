package service.impl;

import com.alibaba.fastjson.JSONObject;
import mapper.UserMapper;
import model.JsonResult;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.UserService;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public User login(String username, String password) {
        if (isExistsUserName(username) && isPassCorrect(username, password)) {
            return userMapper.selectByUserName(username);
        }
        return null;
    }

    @Override
    public int insertUser(User user) {
        return userMapper.insert(user);
    }

    @Override
    public boolean isExistsUserName(String userName) {
        User user = userMapper.selectByUserName(userName);
        if (user != null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isPassCorrect(String username, String password) {
        Map<String, Object> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        User user = userMapper.selectByUserPass(username, password);
        if (user != null) {
            return true;
        }
        return false;
    }

    @Override
    public User getUserByUserName(String userName) {
        return userMapper.selectByUserName(userName);
    }

    @Override
    public int deleteUser(String userName, String userPass) {
        User user = userMapper.selectByUserName(userName);
        return userMapper.deleteByPrimaryKey(user.getUserId());
    }

    @Override
    public int updateUser(User user) {
        int userId = getUserByUserName(user.getUsername()).getUserId();
        user.setUserId(userId);
        return userMapper.updateByPrimaryKeySelective(user);
    }

    @Override
    public JSONObject imInit() {
        JSONObject result = new JSONObject();
        result.put("init", "success");
        return JsonResult.getJsonResult(result);
    }

    @Override
    public JSONObject chInit() {
        JSONObject result = new JSONObject();
        result.put("init", "success");
        return JsonResult.getJsonResult(result);
    }
}
