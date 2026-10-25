package com.hungvers.services.catalog.common.cursor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class CursorCodec {
    private static final ObjectMapper MAPPER = new ObjectMapper();
//            .registerModule(new JavaTimeModule());
    private static final Base64.Encoder ENC = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DEC = Base64.getUrlDecoder();

    private final SecretKeySpec key;

    public CursorCodec(@Value("${common.cursor.secret}") byte[] secret) {
        this.key = new SecretKeySpec(secret, "HmacSHA256");
    }

    public String encode(Object cursor) {
        try {
            byte[] payload = MAPPER.writeValueAsBytes(cursor);
            return ENC.encodeToString(payload) + "." + ENC.encodeToString(sign(payload));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public <T> T decode(String token, Class<T> originalClass) {
        if (token == null || token.isBlank())
            return null;
        try {
            String[] parts = token.split("\\.");
            byte[] payload = DEC.decode(parts[0]);
            byte[] sig = DEC.decode(parts[1]);
            if (!MessageDigest.isEqual(sig, sign(payload))) {
                throw new IllegalArgumentException("Invalid cursor");
            }
            return MAPPER.readValue(payload, originalClass);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor", e);
        }
    }

    private byte[] sign(byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(key);
        return mac.doFinal(data);
    }
}