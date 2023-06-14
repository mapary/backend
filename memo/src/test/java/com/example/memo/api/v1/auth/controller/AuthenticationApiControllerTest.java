package com.example.memo.api.v1.auth.controller;

import com.example.memo.api.v1.auth.dto.RefreshTokenRequest;
import com.example.memo.api.v1.auth.dto.SignInRequest;
import com.example.memo.api.v1.auth.dto.SignUpRequest;
import com.example.memo.api.v1.auth.service.RefreshTokenService;
import com.example.memo.config.security.jwt.JwtAuthTokenFilter;
import com.example.memo.config.security.jwt.JwtTokenProvider;
import com.example.memo.web.member.domain.Member;
import com.example.memo.web.member.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class AuthenticationApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private MemberService memberService;

    @MockBean
    private RefreshTokenService refreshTokenService;

    String username = "test@example.com";
    String password = "password123";

    @BeforeEach
    public void setup() {
        var user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build();
        var member = Member.builder()
                .email(username)
                .password(password)
                .build();

        given(memberService.loadUserByUsername(username)).willReturn(user);
        given(memberService.findByEmail(username)).willReturn(member);
    }

    @Test
    @DisplayName("로그인 요청 처리")
    void testLogin() throws Exception {
        SignInRequest signInRequest = SignInRequest.builder()
                .email(username)
                .password(password)
                .build();

        given(tokenProvider.createAccessToken(any())).willReturn("mocked-access-token");
        given(tokenProvider.createRefreshToken(any())).willReturn("mocked-refresh-token");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(signInRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("login",
                        requestFields(
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("password").description("비밀번호")
                        ),
                        responseHeaders(
                                headerWithName(JwtAuthTokenFilter.AUTHORIZATION_HEADER).description("JWT 토큰")),
                        responseFields(
                                fieldWithPath("access_token").description("액세스 토큰"),
                                fieldWithPath("refresh_token").description("리프레시 토큰")
                        )));
    }

    @Test
    @DisplayName("토큰 재발급 요청 처리")
    void testReissue() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("refresh-token")
                .build();

        given(tokenProvider.getUsernameFromToken(any())).willReturn(username);
        given(tokenProvider.createAccessToken(any())).willReturn("mocked-access-token");
        given(tokenProvider.createRefreshToken(any())).willReturn("mocked-refresh-token");
        given(refreshTokenService.validateToken(any())).willReturn(true);

        mockMvc.perform(post("/api/v1/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("reissue",
                        requestFields(
                                fieldWithPath("refreshToken").description("리프레시 토큰")),
                        responseFields(
                                fieldWithPath("access_token").description("액세스 토큰"),
                                fieldWithPath("refresh_token").description("리프레시 토큰")
                        )));
    }

    @Test
    @DisplayName("회원 가입 요청 처리")
    void testSignUp() throws Exception {
        SignUpRequest request = SignUpRequest.builder()
                .email(username)
                .password(password)
                .build();

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("signup",
                        requestFields(
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("password").description("비밀번호"))));
    }

}