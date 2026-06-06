package com.plantify.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

@Component
public class RsaKeyProvider {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final String keyId;

    public RsaKeyProvider(@Value("${jwt.private-key}") Resource privateKeyResource,
                          @Value("${jwt.public-key}") Resource publicKeyResource,
                          @Value("${jwt.key-id}") String keyId) {
        KeyPair fallbackKeyPair = null;
        if (!privateKeyResource.exists() || !publicKeyResource.exists()) {
            fallbackKeyPair = generateLocalExperimentKeyPair();
        }
        this.privateKey = fallbackKeyPair == null
                ? loadPrivateKey(privateKeyResource)
                : (RSAPrivateKey) fallbackKeyPair.getPrivate();
        this.publicKey = fallbackKeyPair == null
                ? loadPublicKey(publicKeyResource)
                : (RSAPublicKey) fallbackKeyPair.getPublic();
        this.keyId = fallbackKeyPair == null
                ? keyId
                : "local-generated-" + UUID.randomUUID();
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public String getKeyId() {
        return keyId;
    }

    private RSAPrivateKey loadPrivateKey(Resource resource) {
        try {
            byte[] keyBytes = decodePem(resource, "PRIVATE KEY");
            return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            throw new IllegalStateException("RSA private key load failed", e);
        }
    }

    private RSAPublicKey loadPublicKey(Resource resource) {
        try {
            byte[] keyBytes = decodePem(resource, "PUBLIC KEY");
            return (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            throw new IllegalStateException("RSA public key load failed", e);
        }
    }

    private byte[] decodePem(Resource resource, String keyType) throws Exception {
        String pem = resource.getContentAsString(StandardCharsets.UTF_8)
                .replace("-----BEGIN " + keyType + "-----", "")
                .replace("-----END " + keyType + "-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(pem);
    }

    private KeyPair generateLocalExperimentKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("Local experiment RSA key generation failed", e);
        }
    }
}
