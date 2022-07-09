package top.retain.nd.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;

/**
 * @author Retain
 * @date 2021/11/6 17:16
 */
@Data
@TableName("file")
@JsonIgnoreProperties( {"userId", "updateTime", "deleted", "parentId"})
public class UserFile {
    @TableId
    private String id;

    @TableField()
    private String name;

    @TableField()
    private Long size;

    @TableField("user_id")
    private Long userId;

    @TableField()
    private String type;

    @TableField("is_dir")
    private Boolean isDir;

    @TableField( value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableField()
    private Boolean deleted;

    @TableField("oss_path")
    private String ossPath;

    @TableField("request_url")
    private String requestUrl;

    @TableField("parent_id")
    private String parentId;

    @TableField(exist = false)
    private String lastModified;

    @TableField("in_safe")
    private Boolean inSafe;


}
