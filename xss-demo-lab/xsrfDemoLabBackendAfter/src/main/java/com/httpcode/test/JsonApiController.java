package com.httpcode.test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class JsonApiController {

    private final Map<String, Long> activeTokens = new ConcurrentHashMap<>();

    private final Map<Long, Map<String, Object>> users = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, Object>> posts = new ConcurrentHashMap<>();

    private final AtomicLong userIdGenerator = new AtomicLong();
    private final AtomicLong postIdGenerator = new AtomicLong();

    @PostConstruct
    public void initData() {
        // --- 修改开始 ---

        // 使用 HashMap 和 put 方法来创建用户数据 (兼容 Java 8)
        // 给 Alice 增加密码
        Map<String, Object> alice = new HashMap<>();
        alice.put("name", "Alice");
        alice.put("email", "alice@example.com");
        alice.put("password", "123456"); // 新增密码
        alice.put("role", "admin");
        saveUser(alice);

        // 给 Bob 增加密码
        Map<String, Object> bob = new HashMap<>();
        bob.put("name", "Bob");
        bob.put("email", "bob@example.com");
        bob.put("password", "123456"); // 新增密码
        bob.put("role", "user");
        saveUser(bob);

        // 使用 HashMap 和 put 方法来创建帖子数据 (兼容 Java 8)
        Map<String, Object> post1 = new HashMap<>();
        post1.put("userId", 1L); // 注意：ID 是数字，用 1L 表示 Long 类型
        post1.put("title", "Hello World");
        post1.put("body", "This is my first post");
        post1.put("published", true);
        savePost(post1);

        Map<String, Object> post2 = new HashMap<>();
        post2.put("userId", 2L);
        post2.put("title", "Vue + Axios");
        post2.put("body", "Using axios with json-server");
        post2.put("published", false);
        savePost(post2);

        // --- 修改结束 ---


    }

    // --- Users API ---

    @GetMapping("/users")
    public Collection<Map<String, Object>> getAllUsers() {
        return users.values();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        return ResponseEntity.of(Optional.ofNullable(users.get(id)));
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> user) {
        Map<String, Object> savedUser = saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> user) {
        if (!users.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        user.put("id", id);
        Map<String, Object> updatedUser = saveUser(user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (users.remove(id) != null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // --- Posts API ---

    @GetMapping("/posts")
    public Collection<Map<String, Object>> getAllPosts() {
        return posts.values();
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<Map<String, Object>> getPostById(@PathVariable Long id) {
        return ResponseEntity.of(Optional.ofNullable(posts.get(id)));
    }

    @PostMapping("/posts")
    public ResponseEntity<Map<String, Object>> createPost(@RequestBody Map<String, Object> post) {
        Map<String, Object> savedPost = savePost(post);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPost);
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<Map<String, Object>> updatePost(@PathVariable Long id, @RequestBody Map<String, Object> post) {
        if (!posts.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        post.put("id", id);
        Map<String, Object> updatedPost = savePost(post);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        if (posts.remove(id) != null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // --- 内部辅助方法 ---

    private Map<String, Object> saveUser(Map<String, Object> user) {
        Long id = (Long) user.getOrDefault("id", userIdGenerator.incrementAndGet());
        user.put("id", id);
        users.put(id, user);
        return user;
    }

    private Map<String, Object> savePost(Map<String, Object> post) {
        Long id = (Long) post.getOrDefault("id", postIdGenerator.incrementAndGet());
        post.put("id", id);
        posts.put(id, post);
        return post;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest,
                                                     HttpServletResponse response) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        Optional<Map<String, Object>> userOpt = users.values().stream()
                .filter(u -> email.equals(u.get("email")) && password.equals(u.get("password")))
                .findFirst();

        if (userOpt.isPresent()) {
            String token = UUID.randomUUID().toString();
            activeTokens.put(token, (Long) userOpt.get().get("id"));

            // 1. 认证 Cookie (HttpOnly, 确保安全)
            Cookie authCookie = new Cookie("AUTH_TOKEN", token);
            authCookie.setHttpOnly(true);
            authCookie.setPath("/");
            response.addCookie(authCookie);

            // 2. XSRF Cookie (非 HttpOnly, 让 Axios 能读到)
            // 关键：这个 Cookie 必须能被 JS 读取，用来防御 CSRF
            Cookie xsrfCookie = new Cookie("XSRF-TOKEN", token);
            xsrfCookie.setHttpOnly(false); // 允许 JS 读取
            xsrfCookie.setPath("/");
            response.addCookie(xsrfCookie);

            Map<String, Object> result = new HashMap<>();
            result.put("user", userOpt.get());
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // 从请求中找到 Cookie 并从内存中删除
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("AUTH_TOKEN".equals(cookie.getName())) {
                    activeTokens.remove(cookie.getValue());
                }
            }
        }

        // 立即让客户端的 Cookie 过期
        Cookie cookie = new Cookie("AUTH_TOKEN", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.noContent().build();
    }

    // ... 其他 users 和 posts 的 API 保持不变 ...

    // 暴露一个方法供拦截器检查 Token 是否有效
    public boolean isValidToken(String token) {
        return activeTokens.containsKey(token);
    }
}