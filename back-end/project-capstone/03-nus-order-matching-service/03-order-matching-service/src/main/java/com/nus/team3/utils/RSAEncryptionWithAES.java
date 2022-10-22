package com.nus.team3.utils;

import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

// Java 8 example for RSA-AES encryption/decryption.
// Uses strong encryption with 2048 key size.
public class RSAEncryptionWithAES {

    public static void main(String[] args) throws Exception {

        // Generate public and private keys using RSA
//        Map<String, Object> keys = getRSAKeys();
//        PrivateKey privateKey = (PrivateKey) keys.get("private");
//        PublicKey publicKey = (PublicKey) keys.get("public");

// Save private key & public key
//        try (FileOutputStream fos = new FileOutputStream("frontEndPublicKey")) {
//            fos.write(publicKey.getEncoded());
//        }
//        try (FileOutputStream fos1 = new FileOutputStream("frontEndPivateKey")) {
//            fos1.write(privateKey.getEncoded());
//        }

        String plainText = "buy#MSFT#1#101.2#777";
        PublicKey backendPublicKey = getPublicKey();
        PrivateKey backendPrivateKey = getPrivateKey();

        // MESSAGE CONFIDENTIALITY FLOW
        // First create an AES Key
//        String secretAESKeyString = getSecretAESKeyAsString();
        String secretAESKeyString = "2oilpo4azs1wZtnpiiAgHw==";

        // Encrypt our data with AES key
        String encryptedText = encryptTextUsingAES(plainText, secretAESKeyString);
        System.out.println("encryptedText :" + encryptedText);

        // Encrypt AES Key with RSA public Key
        String encryptedAESKeyString = encryptUsingPublicKey(secretAESKeyString, backendPublicKey);
        System.out.println("encryptedAESKeyString Key:" + encryptedAESKeyString);

        // The following logic is on the other side.

        // First decrypt the AES Key with RSA private key
        String decryptedAESKeyString = decryptUsingPrivateKey(encryptedAESKeyString, backendPrivateKey);

        // Now decrypt data using the decrypted AES key!
        String decryptedText = decryptTextUsingAES(encryptedText, decryptedAESKeyString);

        System.out.println("input:" + plainText);
        System.out.println("AES Key:" + secretAESKeyString);
        System.out.println("decrypted:" + decryptedText);

        // PROOF OF ORIGIN FLOW
        // Frontend
        // 1) hash and encrypt payload
        String messageDigest = DigestUtils.sha256Hex(plainText);

        // 2) encrypt using frontend private key
        PrivateKey frontEndPrivateKey = getFrontEndPrivateKey();
        String encryptedMessageDigest = encryptUsingPrivateKey(messageDigest, frontEndPrivateKey);
        System.out.println("encryptedMessageDigest: " + encryptedMessageDigest);

        // Try Proof of origin
        // 1) Received payload
        String sha256hexReceivedCalculated = DigestUtils.sha256Hex(decryptedText);
        System.out.println("sha256hexReceivedCalculated: " + sha256hexReceivedCalculated);

        // 2) Decrypt encrypted message digest from header using frontend public key
        PublicKey frontEndPublicKey = getFrontEndPublicKey();
        String decryptedMessageDigest = decryptUsingPublicKey(encryptedMessageDigest, frontEndPublicKey);
        System.out.println("decryptedMessageDigest: " + decryptedMessageDigest);

        System.out.println("sha256hexReceivedCalculated.equals(decryptedMessageDigest): " + sha256hexReceivedCalculated.equals(decryptedMessageDigest));
    }

    public static PublicKey getFrontEndPublicKey() throws Exception {

        File publicKeyFile = new File("KeyPair/frontEndPublicKey");
        byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        return keyFactory.generatePublic(publicKeySpec);
    }

    public static PrivateKey getFrontEndPrivateKey() throws Exception {

        File privateKeyFile = new File("KeyPair/frontEndPrivateKey");
        byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
        KeyFactory keyFactory2 = KeyFactory.getInstance("RSA");
        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        return keyFactory2.generatePrivate(privateKeySpec);
    }

    public static PublicKey getPublicKey() throws Exception {

        File publicKeyFile = new File("KeyPair/publicKey");
        byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        return keyFactory.generatePublic(publicKeySpec);
    }

    public static PrivateKey getPrivateKey() throws Exception {

        File privateKeyFile = new File("KeyPair/privateKey");
        byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
        KeyFactory keyFactory2 = KeyFactory.getInstance("RSA");
        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        return keyFactory2.generatePrivate(privateKeySpec);
    }

    // Create a new AES key. Uses 128 bit (weak)
    public static String getSecretAESKeyAsString() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(128); // The AES key size in number of bits
        SecretKey secKey = generator.generateKey();
        String encodedKey = Base64.getEncoder().encodeToString(secKey.getEncoded());
        return encodedKey;
    }

    // Encrypt text using AES key
    public static String encryptTextUsingAES(String plainText, String aesKeyString) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(aesKeyString);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

        // AES defaults to AES/ECB/PKCS5Padding in Java 7
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.ENCRYPT_MODE, originalKey);
        byte[] byteCipherText = aesCipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(byteCipherText);
    }

    // Decrypt text using AES key
    public static String decryptTextUsingAES(String encryptedText, String aesKeyString) throws Exception {

        byte[] decodedKey = Base64.getDecoder().decode(aesKeyString);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

        // AES defaults to AES/ECB/PKCS5Padding in Java 7
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.DECRYPT_MODE, originalKey);
        byte[] bytePlainText = aesCipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(bytePlainText);
    }

    // Get RSA keys. Uses key size of 2048.
    private static Map<String, Object> getRSAKeys() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put("private", privateKey);
        keys.put("public", publicKey);
        return keys;
    }

    public static String decryptUsingPrivateKey(String message, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(message)));
    }

    // Decrypt AES Key using RSA public key
    public static String decryptUsingPublicKey(String message, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(message)));
    }

    // Encrypt AES Key using RSA private key
    public static String encryptUsingPrivateKey(String message, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(message.getBytes()));
    }

    public static String encryptUsingPublicKey(String message, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(message.getBytes()));
    }

}
