package dao;

import model.Group;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.Date;

@Repository
public class GroupDao extends BasicDAO<Group, Serializable> {
    @Autowired
    protected GroupDao(Datastore dataStore) {
        super(dataStore);
        ensureIndexes();// 自动创建索引
    }

    public void saveUserGroup(Integer creatorId, String groupName, String groupInfo) {
        Group group = new Group();
        group.setCreatorId(creatorId);
        group.setGroupName(groupName);
        group.setGroupInfo(groupInfo);
        group.setCreateTime(new Date());
        save(group);
    }
}