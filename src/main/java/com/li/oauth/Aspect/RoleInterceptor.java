package com.li.oauth.Aspect;

import com.li.oauth.ErrorCodeConstant;
import com.li.oauth.annotation.Role;
import com.li.oauth.domain.Exception.OAuth2Exception;
import com.li.oauth.domain.RoleEnum;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author futao
 * Created on 2018-12-13.
 */
@Order(1)
@Aspect
@Component
public class RoleInterceptor {
    private static final Pattern AUTHORIZATION_PATTERN = Pattern.compile("^[B|b]earer (?<token>[a-zA-Z0-9-._~+/]+)=*$");

    private String failureUrl = "/signIn";

    @Value("${queryUserRoleInToken:true}")
    private boolean queryUserRoleInToken;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    KeyPair keyPair;

    /**
     * 切入点
     */
    @Pointcut("@annotation(com.li.oauth.annotation.Role)")
    public void pointCut() {

    }

    @Before("pointCut()")
    public void checkUserRole(JoinPoint point) throws IOException {
        //注解打在方法上
        Role annotation = ((MethodSignature) point.getSignature()).getMethod().getAnnotation(Role.class);
        if (Objects.isNull(annotation)) {
            //注解打在类上
            annotation = (Role) point.getSignature().getDeclaringType().getAnnotation(Role.class);
        }
        if (Objects.nonNull(annotation)) {
            List<String> roleEnums = queryCurrentUser();
            if(Objects.isNull(roleEnums)){
                return;
            }
            Set<String> roleEnumsRequired = Arrays.stream(annotation.value()).map(RoleEnum::name).collect(Collectors.toSet());
            if (roleEnums.stream().noneMatch(roleEnumsRequired::contains)) {
                throw new OAuth2Exception("access_dined", HttpStatus.UNAUTHORIZED, ErrorCodeConstant.USER_ROLES_ACCESS_DINED);
            }
        }

    }

    private List<String> queryCurrentUser() throws IOException {
        if (queryUserRoleInToken) {
            return queryCurrentUserFromToken();
        } else {
            UserDetails userDetails = null;
            try {
                userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            } catch (Exception e){

            }
            if (Objects.isNull(userDetails)){
                HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
                response.sendRedirect(failureUrl + "?authentication_error");
                return null;
            }
            return userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        }
    }

    private List<String> queryCurrentUserFromToken() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String headerToken = request.getHeader("Authorization");
        String paramToken = request.getParameter("access_token");
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
                return (List<String>) claims.get("roles");
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("exception", e);
                }
                throw new OAuth2Exception("access_token已过期", HttpStatus.UNAUTHORIZED, ErrorCodeConstant.TOKEN_EXPIRED);
            }
        }
        return null;
    }
}