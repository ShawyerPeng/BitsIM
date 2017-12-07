package service;

import com.alibaba.fastjson.JSONObject;
import model.User;

public interface UserService {
    User login(String username, String password);

    int insertUser(User user);

    boolean isExistsUserName(String userName);

    boolean isPassCorrect(String userName, String userPass);

    User getUserByUserName(String userName);

    int deleteUser(String userName, String userPass);

    int updateUser(User user);

    JSONObject imInit();

    JSONObject chInit();
}