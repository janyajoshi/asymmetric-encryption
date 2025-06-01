package com.credmanager.handler;

import com.credmanager.handler.utils.AsymmetricEncryptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
public class BasicController {

    @Value("${secrets.encrypted.private-key-base64}")
    private String privateKey;

    @Autowired
    private AsymmetricEncryptor asymmetricEncryptor;

    @GetMapping("/encrypt/{query}")
    public void getConfig(@PathVariable String query) throws Exception {
        log.info("input: {}", query);
        String encrypted = asymmetricEncryptor.encrypt(query);
        log.info("encrypted: {}", encrypted);
        String decrypted = asymmetricEncryptor.decrypt(encrypted);
        log.info("decrypted: {}", decrypted);
    }

    @CrossOrigin
    @PostMapping("readCredentials")
    public void readCredentials(@RequestBody ReadCredRequest readCredRequest) throws Exception {
        printDecryptedCredentials(readCredRequest);
    }

    private void printDecryptedCredentials(ReadCredRequest readCredRequest) throws Exception {
        log.info("username: {}", asymmetricEncryptor.decrypt(readCredRequest.getUsername()));
        log.info("password: {}", asymmetricEncryptor.decrypt(readCredRequest.getPassword()));
    }
}
