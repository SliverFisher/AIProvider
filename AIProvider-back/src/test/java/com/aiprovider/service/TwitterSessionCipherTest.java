package com.aiprovider.service;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;

class TwitterSessionCipherTest {
    @Test
    void encryptsAndDecryptsStorageStateWithoutPlaintextLeak() {
        String key = Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));
        TwitterSessionCipher cipher = new TwitterSessionCipher(key);
        String state = "{\"cookies\":[{\"name\":\"auth_token\",\"value\":\"secret\"}]}";

        String encrypted = cipher.encrypt(state);

        assertNotEquals(state, encrypted);
        assertFalse(encrypted.contains("auth_token"));
        assertEquals(state, cipher.decrypt(encrypted));
    }

    @Test
    void rejectsMissingOrInvalidKey() {
        assertThrows(TwitterAutomationException.class, () -> new TwitterSessionCipher("").encrypt("state"));
        assertThrows(TwitterAutomationException.class, () -> new TwitterSessionCipher("not-base64").encrypt("state"));
    }

    @Test
    void rejectsTamperedCiphertext() {
        String key = Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));
        TwitterSessionCipher cipher = new TwitterSessionCipher(key);
        String encrypted = cipher.encrypt("state");
        byte[] bytes = Base64.getDecoder().decode(encrypted);
        bytes[bytes.length - 1] ^= 1;

        TwitterAutomationException error = assertThrows(TwitterAutomationException.class,
                () -> cipher.decrypt(Base64.getEncoder().encodeToString(bytes)));
        assertTrue(error.isSessionExpired());
    }
}
