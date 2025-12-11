package com.air.user_service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;

public class SecretKeyGenerator {
    public static void main(String[] args) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA512");
        keyGen.init(512);

        SecretKey secretKey = keyGen.generateKey();

        String base64Key = Base64.getEncoder().encodeToString(secretKey.getEncoded());

        System.out.println("GENERATED SIGNER KEY:");
        System.out.println(base64Key);
    }
}
