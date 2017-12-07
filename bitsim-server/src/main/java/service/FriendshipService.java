package service;

import model.Friendship;

import java.util.List;

public interface FriendshipService {
    Integer insert(Friendship friendship);

    Integer delete(Integer id);

    List<Friendship> selectByUserId(Integer userId);
}
