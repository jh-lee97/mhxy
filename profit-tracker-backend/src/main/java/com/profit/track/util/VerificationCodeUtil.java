package com.profit.track.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 验证码工具类
 * 验证码存储在 Redis 中，5 分钟过期，一次性使用
 */
@Component
@RequiredArgsConstructor
public class VerificationCodeUtil {

    private final StringRedisTemplate stringRedisTemplate;

    /** Redis key 前缀 */
    private static final String CODE_KEY_PREFIX = "mhxy:code:";

    /** 验证码有效期：5 分钟 */
    private static final long EXPIRE_MINUTES = 5;

    /** 验证码长度 */
    private static final int CODE_LENGTH = 6;

    /**
     * 生成 6 位随机数字验证码
     */
    public String generateCode() {
        int code = (int) ((Math.random() + 1) * 1000000) % 1000000;
        return String.format("%06d", code);
    }

    /**
     * 发送验证码：生成验证码，存入 Redis，打印到控制台
     */
    public void sendCode(String phone) {
        String code = generateCode();
        String key = CODE_KEY_PREFIX + phone;

        stringRedisTemplate.opsForValue().set(key, code, EXPIRE_MINUTES, TimeUnit.MINUTES);
        System.out.println("========== 验证码 ==========");
        System.out.println("手机号: " + phone);
        System.out.println("验证码: " + code);
        System.out.println("有效期: " + EXPIRE_MINUTES + " 分钟");
        System.out.println("============================");
    }

    /**
     * 验证验证码：正确则删除（一次性使用），返回 true/false
     */
    public boolean verifyCode(String phone, String code) {
        String key = CODE_KEY_PREFIX + phone;
        String storedCode = stringRedisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            return false;
        }

        boolean match = storedCode.equals(code);

        // 无论是否匹配，都删除验证码（防止重放攻击）
        stringRedisTemplate.delete(key);

        return match;
    }
}
