package com.nus.team3.aop;

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
import com.nus.team3.utils.Utils;

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

        ClassLoader classLoader = getClass().getClassLoader();
        byte[] fileBytes = Utils.getFileFromResourceAsStream(classLoader, "KeyPair/privateKey.der").readAllBytes();
        PrivateKey privateKey = RSAEncryptionWithAES.getPrivateKey(fileBytes);

        String encryptedAESKeyString = request.getHeader("aes-key");
        String decryptedAESKeyString = RSAEncryptionWithAES.decryptUsingPrivateKey(encryptedAESKeyString, privateKey);

        String decryptedText = RSAEncryptionWithAES.decryptTextUsingAES(encryptedText, decryptedAESKeyString);

        modifiedArgs[0] = decryptedText;

        return proceedingJoinPoint.proceed(modifiedArgs);
    }
}
