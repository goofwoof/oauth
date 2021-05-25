package com.li.oauth.Aspect;

import com.li.oauth.ErrorCodeConstant;
import com.li.oauth.annotation.Role;
import com.li.oauth.domain.Exception.OAuth2Exception;
import com.li.oauth.domain.RoleEnum;
import com.li.oauth.domain.UserAccount;
import com.li.oauth.service.UserAccountService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author futao
 * Created on 2018-12-13.
 */
@Order(1)
@Aspect
@Component
public class RoleInterceptor {
    private static final Pattern AUTHORIZATION_PATTERN = Pattern.compile("^[B|b]earer (?<token>[a-zA-Z0-9-._~+/]+)=*$");

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    UserAccountService userAccountService;

    @Autowired
    KeyPair keyPair;

    /**
     * 切入点
     */
    @Pointcut("@annotation(com.li.oauth.annotation.Role)")
    public void pointCut() {

    }

    @Before("pointCut()")
    public void checkUserRole(JoinPoint point) {
        List<RoleEnum> roleEnums = queryCurrentUser();
        //注解打在方法上
        Role annotation = ((MethodSignature) point.getSignature()).getMethod().getAnnotation(Role.class);
        if (annotation == null) {
            //注解打在类上
            annotation = (Role) point.getSignature().getDeclaringType().getAnnotation(Role.class);
        }
        if (annotation != null) {
            assert roleEnums != null;
            if (!roleEnums.contains(RoleEnum.ROLE_SUPER)) {
                List<RoleEnum> roleEnumsRequired = Arrays.asList(annotation.value());
                if (roleEnums.stream().noneMatch(roleEnumsRequired::contains)) {
                    throw new OAuth2Exception("access_dined", HttpStatus.UNAUTHORIZED, ErrorCodeConstant.USER_ROLES_ACCESS_DINED);
                }
            }
        }
    }

    private List<RoleEnum> queryCurrentUser() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String headerToken = request.getHeader("Authorization");
        String paramToken = request.getParameter("access_token");
        Map<String, Object> result = new HashMap<>(16);
        try {
            String token = null;
            if (StringUtils.isNoneBlank(headerToken)) {
                Matcher matcher = AUTHORIZATION_PATTERN.matcher(headerToken);
                if (matcher.matches()) {
                    token = matcher.group("token");
                }
            }

            if (token == null && StringUtils.isNoneBlank(paramToken)) {
                token = paramToken;
            }

            if (token != null) {
                try {
                    Claims claims = Jwts.parserBuilder().setSigningKey(keyPair.getPublic()).build().parseClaimsJws(token).getBody();

                    String username = claims.getSubject();
                    UserAccount userAccount = userAccountService.findByUsername(username);
                    result.put("username", username);
                    if (StringUtils.isNotEmpty(userAccount.getGender())) {
                        result.put("gender", userAccount.getGender());
                    }
                    if (StringUtils.isNotEmpty(userAccount.getNickName())) {
                        result.put("nickName", userAccount.getNickName());
                    }
                    result.put("accountOpenCode", "" + userAccount.getAccountOpenCode());
                    result.put("authorities", claims.get("roles"));
                    return null;

                } catch (Exception e) {
                    if (log.isDebugEnabled()) {
                        log.debug("exception", e);
                    }
                    throw new OAuth2Exception("access_token错误", HttpStatus.UNAUTHORIZED, ErrorCodeConstant.TOKEN_ERROR);
                }
            } else {
                throw new OAuth2Exception("未检测到access_token", HttpStatus.UNAUTHORIZED, ErrorCodeConstant.PARAM_INVALID);
            }


        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("/user/me exception", e);
            }
            throw new OAuth2Exception("access_token无效", HttpStatus.UNAUTHORIZED, ErrorCodeConstant.TOKEN_EXPIRED);
        }
    }
}