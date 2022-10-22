package com.nus.team3.aop;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
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
public class DigitalSignatureInterceptor {

    @Around(value = "@annotation(digitalSignature)", argNames = "proceedingJoinPoint, digitalSignature")
    private Object customizedTimeInterceptor(ProceedingJoinPoint proceedingJoinPoint,
                                             DigitalSignature digitalSignature) throws Throwable {
        Object[] modifiedArgs = proceedingJoinPoint.getArgs();
        String decryptedText = (String) modifiedArgs[0];
        String sha256hexReceivedCalculated = DigestUtils.sha256Hex(decryptedText);

        HttpServletRequest request = ((ServletRequestAttributes) Objects
                .requireNonNull(RequestContextHolder
                        .getRequestAttributes())).getRequest();
        // This has been encrypted using backend public key.
        String encryptedMessageDigestString = request.getHeader("encrypted-message-digest");
        String decryptedMessageDigestString = decryptUsingPublicKey(encryptedMessageDigestString, getFrontEndPublicKey());

        if (!sha256hexReceivedCalculated.equals(decryptedMessageDigestString)) {
            throw new RuntimeException("Digital Signature failed. Message has been tampered with or " +
                    "Frontend private key has been compromised.");
        }

        return proceedingJoinPoint.proceed();
    }
}