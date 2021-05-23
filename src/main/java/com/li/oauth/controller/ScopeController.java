package com.li.oauth.controller;

import com.li.oauth.service.ScopeDefinitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/scope")
public class ScopeController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ScopeDefinitionService scopeDefinitionService;

    @ResponseBody
    @GetMapping("/query")
    public ResponseEntity<Object> handleOauthSignUp() {
        return new ResponseEntity<>(scopeDefinitionService.findAll(), HttpStatus.OK);
    }
}
