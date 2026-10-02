package com.gifts.demo.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Objects;

@Configuration
public class JwtConfig {

    @Bean
    public RSAPublicKey jwtPublicKey(
            @Value("${spring.security.oauth2.resourceserver.jwt.public-key-location}")
            Resource publicKeyResource
    ) throws IOException {
        try (InputStream input = publicKeyResource.getInputStream()) {
            return Objects.requireNonNull(
                    RsaKeyConverters.x509().convert(input),
                    "The JWT public key could not be loaded"
            );
        }
    }

    @Bean
    public JwtEncoder jwtEncoder(
            RSAPublicKey jwtPublicKey,
            @Value("${security.jwt.private-key-location}")
            Resource privateKeyResource
    ) throws IOException {
        RSAPrivateKey privateKey;

        try (InputStream input = privateKeyResource.getInputStream()) {
            privateKey = Objects.requireNonNull(
                    RsaKeyConverters.pkcs8().convert(input),
                    "The JWT private key could not be loaded"
            );
        }

        if (!jwtPublicKey.getModulus().equals(privateKey.getModulus())) {
            throw new IllegalStateException(
                    "The JWT public and private keys do not match"
            );
        }

        RSAKey rsaKey = new RSAKey.Builder(jwtPublicKey)
                .privateKey(privateKey)
                .build();

        return new NimbusJwtEncoder(
                new ImmutableJWKSet<>(new JWKSet(rsaKey))
        );
    }

    @Bean
    public JwtDecoder jwtDecoder(
            RSAPublicKey jwtPublicKey,
            @Value("${security.jwt.issuer}") String issuer
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withPublicKey(jwtPublicKey)
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(issuer)
        );

        return decoder;
    }
}