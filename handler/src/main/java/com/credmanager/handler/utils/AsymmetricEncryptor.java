package com.credmanager.handler.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class AsymmetricEncryptor {

    @Value("${encryption.public-key-path}")
    private String publicKeyPath;   //  from resources

    @Value("${secrets.encrypted.private-key-base64}")
    private String privateKeyBase64;    //  from config-server

    private static Cipher cipher;
    private static OAEPParameterSpec oaepParams;

    @PostConstruct
    private void setCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        //  legacy - does not work between Java and Js (if js uses Web Crypto)
        //  vulnerable to padding oracle attacks - https://medium.com/@c0D3M/bleichenbacher-attack-explained-bc630f88ff25
        //  cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        //  modern - resistant to above attack
        //  interoperable between Java and Js
        cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        oaepParams = new OAEPParameterSpec(
                "SHA-256",
                "MGF1",
                MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT
        );
    }

    private PublicKey loadPublicKeyFromResources() throws Exception {
        InputStream is = AsymmetricEncryptor.class.getClassLoader().getResourceAsStream(publicKeyPath);
        if (is == null) throw new IllegalArgumentException("Public key not found");

        String pem = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        String base64 = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(base64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

    public String encrypt(String plainText) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, loadPublicKeyFromResources(), oaepParams);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private PrivateKey loadPrivateKey() throws Exception {
        byte[] decoded = Base64.getDecoder().decode(privateKeyBase64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    public String decrypt(String encryptedBase64) throws Exception {
        cipher.init(Cipher.DECRYPT_MODE, loadPrivateKey(), oaepParams);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedBase64));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

}
