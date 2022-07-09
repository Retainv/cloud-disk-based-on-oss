package top.retain.nd.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;
import top.retain.nd.common.CommonResult;
import top.retain.nd.common.CommonResultTool;
import top.retain.nd.entity.User;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Component
public class TokenAuthenticationHelper {

    /**
     * Token保存7天
     * */
    private static final long EXPIRATION_TIME = 3600_000 * 24 * 7;

    /**
     * 记住我时 cookie token 过期时间
     * */
    private static final int COOKIE_EXPIRATION_TIME = 1296000;

    private static final String SECRET_KEY = "SpringSecurityNB";
    public static final String COOKIE_TOKEN = "COOKIE-TOKEN";
    public static final String XSRF = "XSRF-TOKEN";

    /**
     * 设置登陆成功后令牌返回
     * */
    public static void addAuthentication(HttpServletRequest request,  HttpServletResponse response, Authentication authResult) throws IOException {
        int cookExpirationTime = (int) (EXPIRATION_TIME / 1000);

        String jwt = Jwts.builder()
                // Subject 设置用户名
                .setSubject(authResult.getName())
                // 过期时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                // 签名算法
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
        Cookie cookie = new Cookie(COOKIE_TOKEN, jwt);
        cookie.setHttpOnly(false);
        cookie.setPath("/");
        //cookie.setMaxAge(cookExpirationTime);
        cookie.setMaxAge(cookExpirationTime);
        response.addCookie(cookie);

        // 向前端写入数据
        User user = (User) authResult.getPrincipal();
        user.setPassword(null);
        user.setBucketName(null);
        user.setSafeCode(null);
        if (user.getLastLoginTime() != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            user.setLastLoginTimeStr(simpleDateFormat.format(user.getLastLoginTime()));
        }


        CommonResult success = CommonResultTool.success(user);

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.write(new ObjectMapper().writeValueAsString(success));
        out.flush();
        out.close();
    }

    /**
     * 对请求的验证
     * */
    public static Authentication getAuthentication(HttpServletRequest request) {

        Cookie cookie = WebUtils.getCookie(request, COOKIE_TOKEN);
        String token = cookie != null ? cookie.getValue() : null;

        if (token != null) {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            String userName = claims.getSubject();
            if (userName != null) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userName, null, null);
                usernamePasswordAuthenticationToken.setDetails(claims);
                return usernamePasswordAuthenticationToken;
            }
            return null;
        }
        return null;
    }
}
