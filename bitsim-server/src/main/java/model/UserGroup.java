package model;

import java.io.Serializable;
import java.util.Date;

public class UserGroup implements Serializable {
    public static final long serialVersionUID = 1L;

    private Integer id;

    private Integer userId;

    private Integer groupId;

    private Date createTime;

    public UserGroup() {
    }

    public UserGroup(Integer userId, Integer groupId) {
        this.userId = userId;
        this.groupId = groupId;
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

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}