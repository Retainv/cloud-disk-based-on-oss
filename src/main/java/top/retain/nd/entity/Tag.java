package top.retain.nd.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author Retain
 * @date 2021/11/11 9:22
 */
@Data
public class Tag {

    @TableId
    private String id;

    @TableField()
    private String name;

    @TableField( value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableField()
    private Boolean deleted;

    @TableField("user_id")
    private Long userId;
}
