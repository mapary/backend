package com.example.memo.api.auth.service;

import com.example.memo.api.auth.domain.RefreshToken;
import com.example.memo.api.auth.repository.RefreshTokenRepository;
import com.example.memo.api.member.domain.Member;
import com.example.memo.api.member.service.MemberService;
import com.example.memo.config.security.jwt.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider tokenProvider;
    private final MemberService memberService;

    @Transactional
    public void deleteTokenByEmail(String email) {
        refreshTokenRepository.deleteByMember(memberService.findByEmail(email));
    }

    @Transactional
    public Long save(String email, String token) {
        var refreshToken = RefreshToken.builder()
                .member(memberService.findByEmail(email))
                .token(hashToken(token))
                .expiryDate(Instant.now().plusMillis(tokenProvider.REFRESH_TOKEN_EXPIRE_TIME))
                .build();
        return refreshTokenRepository.save(refreshToken).getId();
    }

    private String hashToken(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing the token", e);
        }
    }

    public boolean validateToken(String token) {
        return isTokenValid(token) && isTokenStored(token) && isTokenNotExpired(token);
    }

    private boolean isTokenValid(String token) {
        return tokenProvider.validateToken(token);
    }

    private boolean isTokenStored(String token) {
        String username = tokenProvider.getUsernameFromToken(token);
        Member member = memberService.findByEmail(username);
        var refreshToken = refreshTokenRepository.findByMemberAndToken(member, hashToken(token));
        return refreshToken.isPresent();
    }

    private boolean isTokenNotExpired(String token) {
        String username = tokenProvider.getUsernameFromToken(token);
        Member member = memberService.findByEmail(username);
        var refreshToken = refreshTokenRepository.findByMemberAndToken(member, hashToken(token));
        return refreshToken
                .filter(value -> !value.getExpiryDate().isBefore(Instant.now()))
                .isPresent();
    }
}
