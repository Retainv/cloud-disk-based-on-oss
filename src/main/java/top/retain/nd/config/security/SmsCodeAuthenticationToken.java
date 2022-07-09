package top.retain.nd.config.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.SpringSecurityCoreVersion;

/**
 * @author Retain
 * @date 2021/10/19 15:38
 */
public class SmsCodeAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;
    /**
     * 登录的手机号码
     */
    private final Object principal;
    private final Object credentials;

    /**
     * 构建一个有鉴权的 SmsCodeAuthenticationToken
     */
    public SmsCodeAuthenticationToken(Object principal, Object credentials) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true);
    }

  /**
   * 构建拥有鉴权的 SmsCodeAuthenticationToken
   */
//    public SmsCodeAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
//        super(authorities);
//        this.principal = principal;
//        // must use super, as we override
//        super.setAuthenticated(true);
//        credentials = null;
//    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException(
                    "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }

        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
    }
}
