package com.example.memo.api.v1.auth.service;

import com.example.memo.api.v1.auth.domain.RefreshToken;
import com.example.memo.api.v1.auth.repository.RefreshTokenRepository;
import com.example.memo.config.security.jwt.JwtTokenProvider;
import com.example.memo.web.member.domain.Member;
import com.example.memo.web.member.service.MemberService;
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
        var member = memberService.findByEmail(email);
        var refreshToken = RefreshToken.builder()
                .member(member)
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
        if (!tokenProvider.validateToken(token)) {
            return false;
        }

        // 토큰이 DB에 저장되어 있는지 검증
        String username = tokenProvider.getUsernameFromToken(token);
        Member member = memberService.findByEmail(username);
        var refreshToken = refreshTokenRepository.findByMemberAndToken(member, hashToken(token));
        if (refreshToken.isEmpty()) {
            return false;
        }

        // 토큰이 만료되지 않았는지 검증
        Instant expiryDate = refreshToken.get().getExpiryDate();
        return !expiryDate.isBefore(Instant.now());
    }
}
