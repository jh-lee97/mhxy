package com.profit.track.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 验证码工具类
 * 验证码存储在 Redis 中，5 分钟过期，一次性使用
 * 支持按场景隔离：login / register / reset
 */
@Component
@RequiredArgsConstructor
public class VerificationCodeUtil {

    private final StringRedisTemplate stringRedisTemplate;

    /** Redis key 前缀 */
    private static final String CODE_KEY_PREFIX = "mhxy:code:";

    /** 验证码有效期：5 分钟 */
    private static final long EXPIRE_MINUTES = 5;

    /**
     * 发送验证码：生成验证码，存入 Redis，打印到控制台
     *
     * @param scene  场景标识：login / register / reset
     * @param phone  手机号
     */
    public void sendCode(String scene, String phone) {
        String code = generateCode();
        String key = CODE_KEY_PREFIX + scene + ":" + phone;

        stringRedisTemplate.opsForValue().set(key, code, EXPIRE_MINUTES, TimeUnit.MINUTES);
        System.out.println("========== 验证码 ==========");
        System.out.println("场景: " + scene);
        System.out.println("手机号: " + phone);
        System.out.println("验证码: " + code);
        System.out.println("有效期: " + EXPIRE_MINUTES + " 分钟");
        System.out.println("============================");
    }

    /**
     * 验证验证码：正确则删除（一次性使用），错误不删除（允许重试）
     *
     * @param scene 场景标识：login / register / reset
     * @param phone 手机号
     * @param code  验证码
     * @return 是否验证成功
     */
    public boolean verifyCode(String scene, String phone, String code) {
        String key = CODE_KEY_PREFIX + scene + ":" + phone;
        String storedCode = stringRedisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            return false;
        }

        boolean match = storedCode.equals(code);

        // 只有验证成功时才删除，防止重复使用；失败则保留允许重试
        if (match) {
            stringRedisTemplate.delete(key);
        }

        return match;
    }

    /**
     * 生成 6 位随机数字验证码
     */
    public String generateCode() {
        int code = (int) ((Math.random() + 1) * 1000000) % 1000000;
        return String.format("%06d", code);
    }
}
