package com.nus.team3.aop;

import org.springframework.core.annotation.Order;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Order(value = 0)
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AesRsaDecrypt {

}
