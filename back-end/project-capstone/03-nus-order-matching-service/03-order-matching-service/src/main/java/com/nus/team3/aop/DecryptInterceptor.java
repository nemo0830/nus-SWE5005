package com.nus.team3.aop;

import java.io.InputStream;
import java.security.PrivateKey;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.nus.team3.utils.RSAEncryptionWithAES;

import lombok.extern.slf4j.Slf4j;

@Component
@Aspect
@Slf4j
public class DecryptInterceptor {

    @Around(value = "@annotation(aesRsaDecrypt)", argNames = "proceedingJoinPoint, aesRsaDecrypt")
    private Object customizedTimeInterceptor(ProceedingJoinPoint proceedingJoinPoint,
            AesRsaDecrypt aesRsaDecrypt) throws Throwable {
        Object[] modifiedArgs = proceedingJoinPoint.getArgs();
        String encryptedText = (String) modifiedArgs[0];

        HttpServletRequest request = ((ServletRequestAttributes) Objects
                .requireNonNull(RequestContextHolder
                        .getRequestAttributes()))
                .getRequest();

        PrivateKey privateKey = RSAEncryptionWithAES
                .getPrivateKey(getFileFromResourceAsStream("KeyPair/privateKey.der").readAllBytes());

        String encryptedAESKeyString = request.getHeader("aes-key");
        String decryptedAESKeyString = RSAEncryptionWithAES.decryptUsingPrivateKey(encryptedAESKeyString, privateKey);

        String decryptedText = RSAEncryptionWithAES.decryptTextUsingAES(encryptedText, decryptedAESKeyString);

        modifiedArgs[0] = decryptedText;

        return proceedingJoinPoint.proceed(modifiedArgs);
    }

    private InputStream getFileFromResourceAsStream(String fileName) {
        // The class loader that loaded the class
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }
    }
}
