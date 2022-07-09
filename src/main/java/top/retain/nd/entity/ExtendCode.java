package top.retain.nd.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("extend_code")
public class ExtendCode {

    private String id;
    private String code;
    @TableField("extend_size")
    private Long extendSize;
    @TableField( value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    @TableField("expire_time")
    private Date expireTime;
    private boolean deleted;
}
