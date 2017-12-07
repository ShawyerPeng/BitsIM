package service;

import com.alibaba.fastjson.JSONObject;

public interface UserGroupService {
    JSONObject listUserIdByGroupId(Integer groupId);

    JSONObject insertUserGroup(Integer userId, Integer groupId);

    JSONObject deleteUserGroup(Integer groupId, Integer userId);
}