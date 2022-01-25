package com.example.ssilvermandistl1;

import com.example.ssilvermandistl1.Models.UserPOJO;
import com.example.ssilvermandistl1.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.ArrayList;

@Configuration
public class Security extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserRepository userRepo;
    static InMemoryUserDetailsManager memAuth = new InMemoryUserDetailsManager();

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        System.out.println("configure - A");
        UserDetails newUser = User.withUsername("user")
                .password(passEncode().encode("asdf"))
                .roles("USER").build();
        memAuth.createUser(newUser);

        ArrayList<UserPOJO> userList = (ArrayList<UserPOJO>) userRepo.findAll();
        for(UserPOJO user : userList){
            UserDetails newUserAdd = User.withUsername(user.getEmail())
                    .password(passEncode().encode(user.getPassword()))
                    .roles("USER").build();
            memAuth.createUser(newUserAdd);
        }

        auth.userDetailsService(memAuth);

    }
    // ///////////////////////////////////////////
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        System.out.println("configure - B");

        http.authorizeRequests()
                .antMatchers(HttpMethod.POST,"/createUser").permitAll()
                .antMatchers("/user/userVer/**").hasRole("USER")
                .antMatchers("/videoGamePOJOes/").hasRole("USER")
                .antMatchers("/videoGamePOJOes").hasRole("USER")
                .antMatchers(HttpMethod.PATCH,"/videoGamePOJOes").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST,"/videoGamePOJOes").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE,"/videoGamePOJOes").hasRole("ADMIN")
                .antMatchers(HttpMethod.PATCH, "/userPOJOes/**").hasRole("ADMIN") //figure out a way to lock the user editing
                .antMatchers("/").permitAll()
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf().disable()
                .httpBasic();
        http.cors();

    }
    // ///////////////////////////////////////////


    @Bean
    public PasswordEncoder passEncode() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public InMemoryUserDetailsManager getInMemoryUserDetailsManager(){
        System.out.println("*** Enter getInMemoryUserDetailsManager(");
        return memAuth;
    }




}
