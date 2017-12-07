package dao;

import com.mongodb.WriteResult;
import model.UserGroup;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;

@Repository
public class UserGroupDao extends BasicDAO<UserGroup, Serializable> {
    @Autowired
    protected UserGroupDao(Datastore dataStore) {
        super(dataStore);
        ensureIndexes();// 自动创建索引
    }

    public List<UserGroup> listUserGroupByGroupId(Integer groupId) {
        return createQuery().field("groupId").equal(groupId).asList();
    }

    public WriteResult delUserGroup(Integer groupId, Integer creatorId) {
        Query<UserGroup> query = createQuery().field("groupId").equal(groupId).field("creatorId").equal(creatorId);
        return deleteByQuery(query);
    }
}