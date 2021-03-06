package com.li.oauth.controller;

import com.li.oauth.config.CachesEnum;
import com.li.oauth.domain.*;
import com.li.oauth.domain.Exception.AlreadyExistsException;
import com.li.oauth.service.CaptchaService;
import com.li.oauth.service.OauthClientService;
import com.li.oauth.service.RoleService;
import com.li.oauth.service.UserAccountService;
import com.li.oauth.utils.CheckPasswordStrength;
import com.li.oauth.utils.UuidCreateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SignInAndUpController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    UserAccountService userAccountService;

    @Autowired
    OauthClientService oauthClientService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    CaptchaService captchaService;

    @Autowired
    RoleService roleService;

    @GetMapping("/signIn")
    public String signIn(@RequestParam(value = "error", required = false) String error,
                         Model model) {
        if (StringUtils.isNotEmpty(error)) {
            model.addAttribute("error", error);
        }
        return "signIn";
    }

    @GetMapping("/signUp")
    public String signUp(@RequestParam(value = "error", required = false) String error,
                         Model model) {
        if (StringUtils.isNotEmpty(error)) {
            model.addAttribute("error", error);
        }
        return "signUp";
    }

    @ResponseBody
    @PostMapping("/oauth/signUp")
    public ResponseResult<Object> handleOauthSignUp(@RequestParam(value = GlobalConstant.VERIFICATION_CODE) String verificationCode,
                                                    @RequestParam(value = "graphId") String graphId,
                                                    @RequestParam(value = "username") String username,
                                                    @RequestParam(value = "password") String password) {

        ResponseResult<Object> responseResult = new ResponseResult<>();
        if (StringUtils.isAnyBlank(graphId, username, password)) {
            responseResult.setStatus(GlobalConstant.ERROR);
            responseResult.setMessage("???????????????");
            return responseResult;
        }

        username = StringUtils.trimToEmpty(username).toLowerCase();
        password = StringUtils.trimToEmpty(password);

        if (username.length() < 6) {
            responseResult.setStatus(GlobalConstant.ERROR);
            responseResult.setMessage("???????????????6???");
            return responseResult;
        }

        if (password.length() < 6) {
            responseResult.setStatus(GlobalConstant.ERROR);
            responseResult.setMessage("????????????6???");
            return responseResult;
        }

        if (CheckPasswordStrength.check(password) < 4) {
            responseResult.setStatus(GlobalConstant.ERROR);
            responseResult.setMessage("???????????????????????????????????????");
            return responseResult;
        }

        String captcha = captchaService.getCaptcha(CachesEnum.GraphCaptchaCache, graphId);
        if (!StringUtils.equalsIgnoreCase(verificationCode, captcha)) {
            responseResult.setStatus(GlobalConstant.ERROR);
            responseResult.setMessage("???????????????");
            return responseResult;
        }

        UserAccount userAccount = new UserAccount();
        Role userRole = roleService.findByRoleName(RoleEnum.ROLE_USER.name());
        userAccount.getRoles().add(userRole);
        userAccount.setUsername(StringEscapeUtils.escapeHtml4(username));
        userAccount.setPassword(passwordEncoder.encode(password));
        userAccount.setAccountOpenCode(UuidCreateUtils.createUserOpenId());
        try {
            userAccountService.create(userAccount);
            //???????????????
            captchaService.removeCaptcha(CachesEnum.GraphCaptchaCache, graphId);
        } catch (AlreadyExistsException e) {
            if (log.isErrorEnabled()) {
                log.error("create user exception", e);
            }
            responseResult.setStatus(GlobalConstant.ERROR);
            responseResult.setMessage("??????????????????");
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("create user exception", e);
            }
            responseResult.setStatus(GlobalConstant.ERROR);
            responseResult.setMessage("??????????????????");
        }
        return responseResult;
    }
}
