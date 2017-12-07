package model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;
import java.util.Date;

public class OfflineMessage implements Serializable {
    public static final long serialVersionUID = 1L;

    private Integer offlineMessageId;

    private Integer userId;

    private Integer messageId;

    private Byte status;

    private Date createTime;

    public Integer getOfflineMessageId() {
        return offlineMessageId;
    }

    public void setOfflineMessageId(Integer offlineMessageId) {
        this.offlineMessageId = offlineMessageId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}