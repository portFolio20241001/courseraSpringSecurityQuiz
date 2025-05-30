package com.app.quiz.service;

import com.app.quiz.model.User;
import com.app.quiz.config.QuizUser;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.User.UserBuilder;
// import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class QuizUserDetailsService implements UserDetailsService {

    private final Map<String, User> userStore = new HashMap<>();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ユーザー登録メソッド
    public void registerUser(String username, String password, String email, String role) {
        if (userStore.containsKey(username)) {
            throw new IllegalArgumentException("ユーザー名は既に存在します: " + username);
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // パスワードをハッシュ化
        user.setEmail(email);
        user.setRole(role);
        userStore.put(username, user);
    }

    // Spring Security用のユーザーロード処理
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userStore.get(username);
        if (user == null) {
            throw new UsernameNotFoundException("ユーザーが見つかりません: " + username);
        }

        // Spring SecurityのUserオブジェクトを構築
        return  new QuizUser(user);
    }
}
