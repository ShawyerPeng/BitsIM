package model;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    public static final long serialVersionUID = 1L;

    private Integer messageId;

    private Integer fromId;

    private Integer toId;

    private Byte type;

    private String content;

    private Date createTime;

    public Message() {
    }

    public Message(Integer fromId, Integer toId, Byte type, String content) {
        this.fromId = fromId;
        this.toId = toId;
        this.type = type;
        this.content = content;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public Integer getFromId() {
        return fromId;
    }

    public void setFromId(Integer fromId) {
        this.fromId = fromId;
    }

    public Integer getToId() {
        return toId;
    }

    public void setToId(Integer toId) {
        this.toId = toId;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId=" + messageId +
                ", fromId=" + fromId +
                ", toId=" + toId +
                ", type=" + type +
                ", content='" + content + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}