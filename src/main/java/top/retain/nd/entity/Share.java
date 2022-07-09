package top.retain.nd.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * share
 * @author Retain
 */
@Data
public class Share implements Serializable {
    private String id;

    private String fileName;

    private String fileType;

    private String userName;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(exist = false)
    private String createTimeStr;

    private Date expireTime;

    @TableField(exist = false)
    private String expireTimeStr;

    private Boolean hasExpired;

    private String url;

    @TableField("user_id")
    private Long userId;

    @TableField("file_path")
    private String filePath;

    @TableField("size")
    private Long size;

    @TableField("view_count")
    private int viewCount;

    @TableField("download_count")
    private int downloadCount;

    @TableField("transfer_count")
    private int transferCount;

    @TableField("status")
    private int status;

    @TableField("is_encoded")
    private boolean isEncoded;

    @TableField("code")
    private String code;

    @TableField(exist = false)
    private String shareUrl;

    @TableField(exist = false)
    private String statusDesc;
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Share other = (Share) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getFileName() == null ? other.getFileName() == null : this.getFileName().equals(other.getFileName()))
            && (this.getFileType() == null ? other.getFileType() == null : this.getFileType().equals(other.getFileType()))
            && (this.getUserName() == null ? other.getUserName() == null : this.getUserName().equals(other.getUserName()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getExpireTime() == null ? other.getExpireTime() == null : this.getExpireTime().equals(other.getExpireTime()))
            && (this.getHasExpired() == null ? other.getHasExpired() == null : this.getHasExpired().equals(other.getHasExpired()))
            && (this.getUrl() == null ? other.getUrl() == null : this.getUrl().equals(other.getUrl()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getFileName() == null) ? 0 : getFileName().hashCode());
        result = prime * result + ((getFileType() == null) ? 0 : getFileType().hashCode());
        result = prime * result + ((getUserName() == null) ? 0 : getUserName().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getExpireTime() == null) ? 0 : getExpireTime().hashCode());
        result = prime * result + ((getHasExpired() == null) ? 0 : getHasExpired().hashCode());
        result = prime * result + ((getUrl() == null) ? 0 : getUrl().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", fileName=").append(fileName);
        sb.append(", fileType=").append(fileType);
        sb.append(", userName=").append(userName);
        sb.append(", createTime=").append(createTime);
        sb.append(", expireTime=").append(expireTime);
        sb.append(", hasExpired=").append(hasExpired);
        sb.append(", url=").append(url);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}