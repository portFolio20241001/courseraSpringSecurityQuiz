package com.app.quiz.config;

import com.app.quiz.service.QuizUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

    private final QuizUserDetailsService userDetailsService;

    public WebSecurityConfig(QuizUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // パスワードをハッシュ化するためのPasswordEncoderをBean登録
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManagerをBeanとして登録するメソッド
     * 
     * Spring Securityの認証処理は通常SecurityFilterChainで自動的に行われるため、
     * このBeanは必須ではありません。
     * ただし、コントローラなどで認証処理をプログラム的に呼び出したい場合（例: 自動ログイン処理など）には
     * AuthenticationManagerが必要となるため、その場合にこのBeanを登録します。
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = 
            http.getSharedObject(AuthenticationManagerBuilder.class);

        authBuilder.userDetailsService(userDetailsService)  //ユーザの認証方法を追加
                   .passwordEncoder(passwordEncoder());     //passwordのエンコード方法を追加（ここではBCryptPasswordEncoder）

        return authBuilder.build(); //上記設定のもと、AuthenticationManagerを作成        
    }

    /**
     * セキュリティのフィルター設定
     * 各URLごとのアクセス権限を設定
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/register", "/login").permitAll()          // どなたでもアクセス可能
                .requestMatchers("/quizlist").hasRole("ADMIN")               // ADMINロールのみ
                .requestMatchers("/quiz").hasRole("USER")                    // USERロールのみ
                .anyRequest().authenticated()                                // それ以外は認証必須
            )
            .formLogin(form -> form
                .loginPage("/login")  // ログインページのURL
                .permitAll()
            )
            .logout(logout -> logout
                .permitAll()
            );

        return http.build();
    }
}
