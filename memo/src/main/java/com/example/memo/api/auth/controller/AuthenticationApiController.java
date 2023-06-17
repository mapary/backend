package com.example.memo.api.auth.controller;

import com.example.memo.api.auth.dto.RefreshTokenRequest;
import com.example.memo.api.auth.dto.SignInRequest;
import com.example.memo.api.auth.dto.SignUpRequest;
import com.example.memo.api.auth.service.RefreshTokenService;
import com.example.memo.api.common.exceptions.AuthException;
import com.example.memo.api.common.exceptions.ErrorCode;
import com.example.memo.api.member.service.MemberService;
import com.example.memo.config.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationApiController {
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final MemberService memberService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<JwtTokens> authorize(@Valid @RequestBody SignInRequest request) {
        var authentication = authenticate(request.getEmail(), request.getPassword());
        return ResponseEntity.ok().body(generateJwtTokens(request.getEmail(), authentication));
    }

    @PostMapping("/reissue")
    public ResponseEntity<JwtTokens> reissue(@Valid @RequestBody RefreshTokenRequest request) {
        String currentRefreshToken = request.getRefreshToken();

        validateRefreshToken(currentRefreshToken);

        String username = tokenProvider.getUsernameFromToken(currentRefreshToken);

        var userDetails = memberService.loadUserByUsername(username);
        var authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        return ResponseEntity.ok().body(generateJwtTokens(username, authentication));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest request) {
        Long savedId = memberService.save(request);
        return ResponseEntity.created(URI.create("/api/members/" + savedId)).build();
    }

    private JwtTokens generateJwtTokens(String username, Authentication authentication) {
        String accessToken = tokenProvider.createAccessToken(authentication);
        String refreshToken = createAndSaveRefreshToken(username, authentication);

        return new JwtTokens(accessToken, refreshToken);
    }

    private Authentication authenticate(String email, String password) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
        var authentication = authenticationManagerBuilder.getObject()
                .authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    private String createAndSaveRefreshToken(String email, Authentication authentication) {
        String refreshToken = tokenProvider.createRefreshToken(authentication);

        refreshTokenService.deleteTokenByEmail(email);
        refreshTokenService.save(email, refreshToken);

        return refreshToken;
    }

    private void validateRefreshToken(String token) {
        if (!refreshTokenService.validateToken(token)) {
            throw new AuthException(ErrorCode.INVALID_TOKEN);
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
