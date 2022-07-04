//package com.service.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//
//@EnableWebSecurity
//public class WebConfig extends WebSecurityConfigurerAdapter {
//
//
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        // TODO configure authentication manager
//    }
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
////        http
////                .csrf().disable().cors().disable().authorizeRequests().antMatchers("/fetch-all-product").permitAll()
////                .anyRequest().authenticated();
////        // TODO configure web security
//    }
//
//
//}