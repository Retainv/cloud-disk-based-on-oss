package top.retain.nd.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;
import top.retain.nd.filter.JwtAuthenticationFilter;
import top.retain.nd.filter.JwtLoginFilter;
import top.retain.nd.filter.WrapperRequestFilter;
import top.retain.nd.handler.CustomerAccessDeniedHandler;
import top.retain.nd.handler.CustomizeLogoutSuccessHandler;
import top.retain.nd.service.impl.UserServiceImpl;

import javax.annotation.Resource;

/**
 * @author retain
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Resource
    private SmsCodeAuthenticationSecurityConfig smsCodeAuthenticationSecurityConfig;

    /**
     * 开放访问的请求
     */
    private final static String[] PERMIT_ALL_MAPPING = {
            "/file/downloadDir",
            "/user/**",
            "/login",
            "/sms/**",
            "/swagger*//**",
            "/webjars/**",
            "/v2/api-docs/**",
            "/tag/**",
            "/file/link/**",
            "/share/getShareDetail/**",
            "/share/**"
    };


    /**
     * 配置加密器
     */
    @Bean
    public PasswordEncoder getPw() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 重写spring security 获取用户逻辑
     */
    @Bean
    public UserDetailsService getUserDetailsService() {
        return new UserServiceImpl();
    }

    /**
     * 重写AuthenticationProvider，使得可以抛出用户名不存在异常
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setHideUserNotFoundExceptions(false);
        provider.setUserDetailsService(getUserDetailsService());
        provider.setPasswordEncoder(getPw());
        return provider;
    }


    /**
     * 跨域配置
     */
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        // 允许跨域访问的 URL
//        List<String> allowedOriginsUrl = new ArrayList<>();
//        allowedOriginsUrl.add("*");
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowCredentials(true);
//        // 设置允许跨域访问的 URL
//        config.setAllowedOrigins(allowedOriginsUrl);
//        config.addAllowedHeader("*");
//        config.addAllowedMethod("*");
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//        return source;
//    }
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(PERMIT_ALL_MAPPING)
                .permitAll()
                .anyRequest()
                .authenticated()
                // 自定义的FilterSecurityInterceptor
                .and()
                // 添加过滤器链,前一个参数过滤器， 后一个参数过滤器添加的地方
                // 登陆过滤器
                .addFilterBefore(new JwtLoginFilter("/login", authenticationManager()), UsernamePasswordAuthenticationFilter.class)
                // 请求过滤器
                .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                // 添加一个过滤器来封装request
                .addFilterBefore(new WrapperRequestFilter(), WebAsyncManagerIntegrationFilter.class)
                // 开启跨域
                .cors()
                .and()
                // 开启 csrf
                .csrf()
                .disable()
//                .ignoringAntMatchers(PERMIT_ALL_MAPPING)
//                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
//                .and()
                .exceptionHandling().accessDeniedHandler(new CustomerAccessDeniedHandler());
        //  todo: 可能存在问题
        http.logout()
                .logoutSuccessHandler(new CustomizeLogoutSuccessHandler())
                .deleteCookies("COOKIE-TOKEN")
                .deleteCookies("JSESSIONID");
        http.apply(smsCodeAuthenticationSecurityConfig);
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        auth.userDetailsService(getUserDetailsService())
                .passwordEncoder(getPw());

    }


//    @Override
//    public void configure(WebSecurity web) throws Exception {
//        web.ignoring().antMatchers(PERMIT_ALL_MAPPING);
//    }
}
