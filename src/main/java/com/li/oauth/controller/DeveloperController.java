package com.li.oauth.controller;

import com.li.oauth.annotation.Role;
import com.li.oauth.service.UserAccountService;
import com.li.oauth.utils.JpaPageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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
    @Role
    public ResponseEntity<Object> handleOauthSignUp(@RequestParam(value = "length", defaultValue = "10") Integer pageSize,
                                                    @RequestParam(value = "start", defaultValue = "0") Integer start,
                                                    @RequestParam(value = "sortField", required = false, defaultValue = "id") String sortField,
                                                    @RequestParam(value = "sortOrder", required = false, defaultValue = "desc") String sortOrder) {
        Pageable pageable = JpaPageUtils.createPageableOffset(start, pageSize, sortField, sortOrder);
        return new ResponseEntity<>(userAccountService.findAllDevelopers(pageable), HttpStatus.OK);
    }
}
