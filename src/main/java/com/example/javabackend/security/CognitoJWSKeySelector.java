package com.example.javabackend.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.SecurityContext;

import java.security.Key;
import java.util.List;

public class CognitoJWSKeySelector implements JWSKeySelector<SecurityContext> {
    private final JWKSource<SecurityContext> keySource;
    
    public CognitoJWSKeySelector(JWKSource<SecurityContext> keySource) {
        this.keySource = keySource;
    }
    
    @Override
    public List<? extends Key> selectJWSKeys(JWSHeader header, SecurityContext context) {
        if (!JWSAlgorithm.RS256.equals(header.getAlgorithm())) {
            throw new IllegalArgumentException("Only RS256 algorithm is supported");
        }
        
        JWKMatcher matcher = new JWKMatcher.Builder()
                .keyID(header.getKeyID())
                .keyType(KeyType.RSA)
                .build();
        
        try {
            return keySource.get(new JWKSelector(matcher), context)
                    .stream()
                    .map(JWK::toRSAKey)
                    .map(rsaKey -> {
                        try {
                            return rsaKey.toRSAPublicKey();
                        } catch (JOSEException e) {
                            throw new RuntimeException("Failed to convert RSA key to public key", e);
                        }
                    })
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve JWK keys", e);
        }
    }
} 