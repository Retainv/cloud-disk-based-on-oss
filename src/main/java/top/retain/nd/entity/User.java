package top.retain.nd.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;

/**
 * @author Retain
 * @date 2021/9/29 16:43
 */
@TableName("user")
@ApiModel("用户表")
@Getter
@Setter
@ToString
@Accessors(chain = true)
@JsonIgnoreProperties({"bucketName, safeCode"})
public class User implements Serializable,  UserDetails, Principal {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField()
    private String name;


    @TableField()
    private String account;

    @TableField()
    private String password;

    @TableField()
    private String phoneNumber;
    @TableField(fill= FieldFill.INSERT, updateStrategy = FieldStrategy.NOT_NULL )
    private Date createTime;

    @TableField(fill= FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableField("deleted")
    private boolean deleted = false;

    @TableField
    private String bucketName;

    @TableField
    private String avator;

    @TableField("safe_on")
    private Boolean isSafeOn;

    @TableField("safe_code")
    private String safeCode;
    @TableField(value = "last_login_time", fill= FieldFill.INSERT)
    private Date lastLoginTime;

    @TableField("total_space")
    private Long totalSpace;

    /**
     * 格式化日期
     */
    @TableField(exist = false)
    private String lastLoginTimeStr;

    @TableField(exist = false)
    private String createTimeStr;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getUsername() {
        return getAccount();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !isDeleted();
    }
}

