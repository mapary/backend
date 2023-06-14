package com.example.memo.api.v1.auth.controller;

import com.example.memo.api.v1.auth.dto.RefreshTokenRequest;
import com.example.memo.api.v1.auth.dto.SignInRequest;
import com.example.memo.api.v1.auth.dto.SignUpRequest;
import com.example.memo.api.v1.auth.service.RefreshTokenService;
import com.example.memo.config.security.jwt.JwtAuthTokenFilter;
import com.example.memo.config.security.jwt.JwtTokenProvider;
import com.example.memo.web.member.service.MemberService;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationApiController {
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final MemberService memberService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<JwtTokens> authorize(@Valid @RequestBody SignInRequest request) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        var authentication = authenticationManagerBuilder.getObject()
            .authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.createAccessToken(authentication);
        String refreshToken = tokenProvider.createRefreshToken(authentication);

        refreshTokenService.deleteTokenByEmail(request.getEmail());
        refreshTokenService.save(request.getEmail(), refreshToken);

        return ResponseEntity.ok()
            .header(
                JwtAuthTokenFilter.AUTHORIZATION_HEADER,
                "Bearer " + accessToken
            ).body(new JwtTokens(accessToken, refreshToken));
    }

    @PostMapping("/reissue")
    public ResponseEntity<JwtTokens> reissue(@Valid @RequestBody RefreshTokenRequest request) {
        String currentRefreshToken = request.getRefreshToken();
        if (!refreshTokenService.validateToken(currentRefreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String username = tokenProvider.getUsernameFromToken(currentRefreshToken);

        var userDetails = memberService.loadUserByUsername(username);
        var authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());

        String newAccessToken = tokenProvider.createAccessToken(authentication);
        String newRefreshToken = tokenProvider.createRefreshToken(authentication);

        refreshTokenService.deleteTokenByEmail(username);
        refreshTokenService.save(username, newRefreshToken);

        return ResponseEntity.ok()
            .header(
                JwtAuthTokenFilter.AUTHORIZATION_HEADER,
                "Bearer " + newAccessToken
            ).body(new JwtTokens(newAccessToken, newRefreshToken));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest request) {
        try {
            memberService.save(request);
            return ResponseEntity.ok().body("User registered successfully!");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed!");
        }
    }

    private record JwtTokens(String accessToken, String refreshToken) {
        @JsonProperty("access_token")
        String getAccessToken() {
            return accessToken;
        }

        @JsonProperty("refresh_token")
        String getRefreshToken() {
            return refreshToken;
        }
    }
}
