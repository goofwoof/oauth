package com.li.oauth.config;//package com.li.oauth.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerSecurityConfiguration;
//
//@Configuration
//class JwkSetEndpointConfiguration extends AuthorizationServerSecurityConfiguration {
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        super.configure(http);
//        http
//                .requestMatchers()
//                .mvcMatchers("/.well-known/**")
//                .and()
//                .authorizeRequests()
//                .mvcMatchers("/.well-known/**").permitAll();
//    }
//}
