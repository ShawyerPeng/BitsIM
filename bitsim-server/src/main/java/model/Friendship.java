package model;

import java.io.Serializable;
import java.util.Date;

public class Friendship implements Serializable {
    public static final long serialVersionUID = 1L;

    private Integer id;
    private Integer userId;
    private Integer friendId;
    private Boolean userPass;
    private Boolean friendPass;
    private Date applyTime;
    private String applyMessage;
    private Date createTime;

    public Friendship() {
    }

    public Friendship(Integer userId, Integer friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }

    public Friendship(Integer userId, Integer friendId, String applyMessage) {
        this.userId = userId;
        this.friendId = friendId;
        this.applyMessage = applyMessage;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getFriendId() {
        return friendId;
    }

    public void setFriendId(Integer friendId) {
        this.friendId = friendId;
    }

    public Boolean getUserPass() {
        return userPass;
    }

    public void setUserPass(Boolean userPass) {
        this.userPass = userPass;
    }

    public Boolean getFriendPass() {
        return friendPass;
    }

    public void setFriendPass(Boolean friendPass) {
        this.friendPass = friendPass;
    }

    public Date getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Date applyTime) {
        this.applyTime = applyTime;
    }

    public String getApplyMessage() {
        return applyMessage;
    }

    public void setApplyMessage(String applyMessage) {
        this.applyMessage = applyMessage == null ? null : applyMessage.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}