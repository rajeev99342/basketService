package com.service.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
public class SecurityConfigurer extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;


    @Autowired
    MyUserDetailsService myUserDetailsService;

    private final String[] WHITE_LIST = {
            "/sign-in",
            "/sign-up",
            "/get-product-details",
            "/delete-cart-item",
            "/save-address",
            "/get-address",
            "/add-category",
            "/fetch-all-category",
            "/fetch-all-category-name",
            "/upload-image",
            "/get-uploaded-image",
            "/get-order-by-user",
            "/cancel-order",
            "/fetch-all-product",

    };
    private final String[] BLACK_LIST = {
            "/xyz",
            "/fetch-product-by-cart",
            "/add-to-cart",
            "/place-order",
            "/hello"
    };

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {


        http.csrf().disable()
                .cors()
                .and()
                // dont authenticate this particular request
                .authorizeRequests().antMatchers(WHITE_LIST).permitAll().
                and().authorizeRequests().antMatchers(BLACK_LIST).authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        // all other requests need to be authenticated
        // make sure we use stateless session; session won't be used to
        // store user's sta


    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}
