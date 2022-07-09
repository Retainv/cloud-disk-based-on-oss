package top.retain.nd.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import top.retain.nd.filter.SmsCodeAuthenticationFilter;
import top.retain.nd.handler.CustomAuthenticationFailureHandler;
import top.retain.nd.handler.CustomAuthenticationSuccessHandler;
import top.retain.nd.handler.CustomizeLogoutSuccessHandler;
import top.retain.nd.service.impl.UserSmsServiceImpl;

/**
 * @author Retain
 * @date 2021/10/19 17:13
 */
@Configuration
@EnableWebSecurity
public class SmsCodeAuthenticationSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
    @Autowired
    private UserSmsServiceImpl userDetailsService;
    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Autowired
    private SmsCodeAuthenticationProvider smsCodeAuthenticationProvider;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        SmsCodeAuthenticationFilter smsCodeAuthenticationFilter = new SmsCodeAuthenticationFilter();
        smsCodeAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        smsCodeAuthenticationFilter.setAuthenticationSuccessHandler(customAuthenticationSuccessHandler);
        smsCodeAuthenticationFilter.setAuthenticationFailureHandler(customAuthenticationFailureHandler);

        smsCodeAuthenticationProvider.setUserDetailsService(userDetailsService);

        http.authenticationProvider(smsCodeAuthenticationProvider)
                .addFilterAt(smsCodeAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .cors();

        http.logout()
                .logoutSuccessHandler(new CustomizeLogoutSuccessHandler())
                .deleteCookies("COOKIE-TOKEN")
                .deleteCookies("JSESSIONID");
    }
}
