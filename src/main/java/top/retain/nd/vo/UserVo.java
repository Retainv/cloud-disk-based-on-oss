package top.retain.nd.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Retain
 * @date 2021/11/9 21:12
 */
public class UserVo implements Serializable {
    private Long id;
    private String name;
    private String account;
    private String phoneNumber;
    private Date createTime;
    private Date updateTime;
    private boolean deleted;
    private String avater;
}
