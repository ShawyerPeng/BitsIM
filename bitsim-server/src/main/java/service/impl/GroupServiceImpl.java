package service.impl;

import mapper.GroupMapper;
import model.Group;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.GroupService;

@Service
public class GroupServiceImpl implements GroupService {
    @Autowired
    private GroupMapper groupMapper;

    public int insertGroup(Group group) {
        return groupMapper.insert(group);
    }

    public int deleteGroup(Group group) {
        return groupMapper.deleteByPrimaryKey(group.getGroupId());
    }


}