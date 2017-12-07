package model;

import java.io.Serializable;
import java.util.Date;

public class Group implements Serializable {
    public static final long serialVersionUID = 1L;

    private Integer groupId;

    private Integer creatorId;

    private String groupName;

    private String groupInfo;

    private Date createTime;

    public Group() {
    }

    public Group(Integer creatorId, String groupName) {
        this.creatorId = creatorId;
        this.groupName = groupName;
    }

    public Group(Integer creatorId, String groupName, String groupInfo) {
        this.creatorId = creatorId;
        this.groupName = groupName;
        this.groupInfo = groupInfo;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName == null ? null : groupName.trim();
    }

    public String getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(String groupInfo) {
        this.groupInfo = groupInfo == null ? null : groupInfo.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Group{" +
                "groupId=" + groupId +
                ", creatorId=" + creatorId +
                ", groupName='" + groupName + '\'' +
                ", groupInfo='" + groupInfo + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}