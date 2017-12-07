//package service.impl;
//
//import com.alibaba.fastjson.JSONObject;
//import com.google.common.collect.Lists;
//import com.mongodb.WriteResult;
//import dao.UserGroupDao;
//import mapper.UserGroupMapper;
//import model.JsonResult;
//import model.UserGroup;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import service.UserGroupService;
//
//import java.util.List;
//
//@Service
//public class UserGroupServiceImpl implements UserGroupService {
//    @Autowired
//    private UserGroupMapper userGroupMapper;
//    @Autowired
//    private UserGroupDao userGroupDao;
//
//    @Override
//    public JSONObject listUserIdByGroupId(Integer groupId) {
//        List<UserGroup> userGroupList = userGroupDao.listUserGroupByGroupId(groupId);
//        List<Integer> uidList = Lists.newArrayList();
//        for (UserGroup userGroup : userGroupList) {
//            uidList.add(userGroup.getUserId());
//        }
//        return JsonResult.getJsonResult(uidList);
//    }
//
//    @Override
//    public JSONObject insertUserGroup(Integer userId, Integer groupId) {
//        UserGroup userGroup = new UserGroup(userId, groupId);
//        userGroupDao.save(userGroup);
//        return JsonResult.getJsonResult(0);
//    }
//
//    @Override
//    public JSONObject deleteUserGroup(Integer groupId, Integer userId) {
//        userGroupDao.delUserGroup(groupId, userId);
//        return JsonResult.getJsonResult(0);
//    }
//}