package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SpringBootApplication
@RestController
public class InformationSystemApplication {

    private final Jedis jedis = new Jedis("localhost");

    // 存储用户信息
    private final Map<String, User> userMap = new HashMap<>();

    // 存储课程成绩排名
    private final String COURSE_RANK_KEY = "course_rank";

    public static void main(String[] args) {
        SpringApplication.run(InformationSystemApplication.class, args);
    }

    // 注册用户
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        // 模拟生成验证码
        String verificationCode = generateVerificationCode();
        // 存入Redis，设置过期时间
        jedis.setex(user.getUsername(), 60, verificationCode);
        // 存储用户信息
        userMap.put(user.getUsername(), user);
        return "Verification code sent to your email.";
    }

    // 登录验证
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String verificationCode) {
        // 从Redis中获取验证码
        String storedCode = jedis.get(username);
        if (storedCode == null || !storedCode.equals(verificationCode)) {
            return "Invalid verification code";
        } else {
            return "Login successful!";
        }
    }

    // 个人信息查询
    @GetMapping("/profile/{username}")
    public User getProfile(@PathVariable String username) {
        return userMap.get(username);
    }

    // 排名查询
    @PostMapping("/ranking")
    public CourseRanking getRanking(@RequestBody Course course) {
        // 模拟查询课程成绩排名
        // 在Redis中设置初始数据
        jedis.zadd(COURSE_RANK_KEY, 100, "StudentA");
        jedis.zadd(COURSE_RANK_KEY, 90, "StudentB");
        jedis.zadd(COURSE_RANK_KEY, 80, "StudentC");

        // 获取排名
        Long rank = jedis.zrank(COURSE_RANK_KEY, course.getName());

        // 获取总人数
        Long totalStudents = jedis.zcard(COURSE_RANK_KEY);

        // 获取班级第一名
        String firstStudent = jedis.zrange(COURSE_RANK_KEY, 0, 0).iterator().next();

        return new CourseRanking(rank, totalStudents, firstStudent);
    }

    // 生成验证码
    private String generateVerificationCode() {
        // 生成随机UUID作为验证码
        return UUID.randomUUID().toString().substring(0, 6);
    }
}

class User {
    private String username;
    private String email;

    // getters and setters
}

class Course {
    private String name;

    // getters and setters
}

class CourseRanking {
    private Long rank;
    private Long totalStudents;
    private String firstStudent;

    // constructors, getters and setters
}
