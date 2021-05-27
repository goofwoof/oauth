package com.li.oauth.controller;

import com.li.oauth.annotation.Role;
import com.li.oauth.domain.ApplyStatusEnum;
import com.li.oauth.domain.RoleApply;
import com.li.oauth.domain.RoleEnum;
import com.li.oauth.service.UserAccountService;
import com.li.oauth.utils.JpaPageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/developer")
public class DeveloperController {
    @Autowired
    UserAccountService userAccountService;

    @ResponseBody
    @GetMapping("/queryAll")
    @Role({RoleEnum.ROLE_ADMIN, RoleEnum.ROLE_SUPER})
    public ResponseEntity<Object> queryAllDeveloper(@RequestParam(value = "length", defaultValue = "10") Integer pageSize,
                                                    @RequestParam(value = "start", defaultValue = "0") Integer start,
                                                    @RequestParam(value = "sortField", required = false, defaultValue = "id") String sortField,
                                                    @RequestParam(value = "sortOrder", required = false, defaultValue = "desc") String sortOrder) {
        Pageable pageable = JpaPageUtils.createPageableOffset(start, pageSize, sortField, sortOrder);
        return new ResponseEntity<>(userAccountService.findAllDevelopers(pageable), HttpStatus.OK);
    }

    @PostMapping("/apply")
    @Role(value = RoleEnum.ROLE_USER)
    public ResponseEntity<Object> toBeDeveloper(Authentication authentication){
        RoleApply roleApply = userAccountService.applyRole(authentication.getName(), RoleEnum.ROLE_DEVELOPER);
        return new ResponseEntity<>(roleApply, HttpStatus.OK);
    }

    @PostMapping("/apply/review")
    @Role(value = RoleEnum.ROLE_ADMIN)
    public ResponseEntity<Object> reviewDeveloperApply(@RequestParam(value = "applyId") Long applyId,
                                                       @RequestParam(value = "review") Boolean review){
        userAccountService.reviewRole(applyId, review);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/apply/status")
    @Role(value = {RoleEnum.ROLE_ADMIN, RoleEnum.ROLE_SUPER})
    public ResponseEntity<Object> queryDeveloperApplyStatus(@RequestParam(value = "applyStatus", required = false) ApplyStatusEnum applyStatus,
                                                            @RequestParam(value = "length", defaultValue = "10") Integer pageSize,
                                                            @RequestParam(value = "start", defaultValue = "0") Integer start,
                                                            @RequestParam(value = "sortField", required = false, defaultValue = "id") String sortField,
                                                            @RequestParam(value = "sortOrder", required = false, defaultValue = "desc") String sortOrder){
        Pageable pageable = JpaPageUtils.createPageableOffset(start, pageSize, sortField, sortOrder);
        return new ResponseEntity<>(userAccountService.queryDeveloperApplyStatus(applyStatus, pageable), HttpStatus.OK);
    }
}
