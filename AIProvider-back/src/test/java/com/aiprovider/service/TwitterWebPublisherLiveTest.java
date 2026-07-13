package com.aiprovider.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 可选的真实 X 登录测试。敏感值必须通过临时环境变量传入，禁止写进源码或提交到 Git。
 */
@EnabledIfEnvironmentVariable(named = "TWITTER_LIVE_TEST", matches = "true")
class TwitterWebPublisherLiveTest {
    @Test
    void logsInAndReturnsBrowserSession() {
        String username = required("TWITTER_TEST_USERNAME");
        String password = required("TWITTER_TEST_PASSWORD");
        String identity = System.getenv("TWITTER_TEST_IDENTITY");
        String verificationCode = System.getenv("TWITTER_TEST_VERIFICATION_CODE");
        TwitterWebPublisher publisher = new TwitterWebPublisher(new ObjectMapper(), true, 60000,
                System.getenv("TWITTER_BROWSER_EXECUTABLE_PATH"));

        String storageState = publisher.login(username, password, identity, verificationCode);

        assertTrue(storageState.contains("auth_token"), "登录成功后应包含 X 的认证会话");
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) throw new IllegalStateException("缺少环境变量：" + name);
        return value;
    }
}
