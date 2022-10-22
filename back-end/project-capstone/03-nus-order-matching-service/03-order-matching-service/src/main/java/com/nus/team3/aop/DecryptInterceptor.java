package com.nus.team3.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

import static com.nus.team3.utils.RSAEncryptionWithAES.*;

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
                        .getRequestAttributes())).getRequest();
        String encryptedAESKeyString = request.getHeader("aes-key");
        String decryptedAESKeyString = decryptUsingPrivateKey(encryptedAESKeyString, getPrivateKey());

        String decryptedText = decryptTextUsingAES(encryptedText, decryptedAESKeyString);

        modifiedArgs[0] = decryptedText;

        return proceedingJoinPoint.proceed(modifiedArgs);
    }
}
