package dao;

import model.Friendship;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@Repository
public class FriendshipDao extends BasicDAO<Friendship, Serializable> {
    @Autowired
    protected FriendshipDao(Datastore dataStore) {
        super(dataStore);
        ensureIndexes();// 自动创建索引
    }

    // 接受申请
    public void acceptFriend(Integer userId, Integer friendId) {
        Query<Friendship> query = createQuery().field("userId").equal(userId).field("friendId").equal(friendId);
        UpdateOperations<Friendship> opt = createUpdateOperations().set("acceptTime", System.currentTimeMillis());
        updateFirst(query, opt);
    }

    // 申请好友
    public void applyFriend(Integer userId, Integer friendId, String applyMessage) {
        Friendship friend = new Friendship(userId, friendId, applyMessage);
        save(friend);
    }

    public void delFriend(Integer userId, Integer friendId) {
        Query<Friendship> query = createQuery().field("userId").equal(userId).field("friendId").equal(friendId);
        deleteByQuery(query);
    }
}