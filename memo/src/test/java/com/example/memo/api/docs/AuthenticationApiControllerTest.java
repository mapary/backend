package com.example.memo.api.docs;

import com.example.memo.api.auth.dto.RefreshTokenRequest;
import com.example.memo.api.auth.dto.SignInRequest;
import com.example.memo.api.auth.dto.SignUpRequest;
import com.example.memo.api.auth.service.RefreshTokenService;
import com.example.memo.api.member.service.MemberService;
import com.example.memo.config.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationApiControllerTest extends RestDocsConfiguration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    @Mock
    private AuthenticationManager authenticationManager;

    @MockBean
    private RefreshTokenService refreshTokenService;

    private final String username = "test@example.com";
    private final String password = "password1234";

    @BeforeEach
    void setup() {
        when(jwtTokenProvider.createAccessToken(any())).thenReturn("valid-access-token");
        when(jwtTokenProvider.createRefreshToken(any())).thenReturn("valid-refresh-token");
        when(refreshTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(anyString())).thenReturn(username);
        when(memberService.loadUserByUsername(anyString())).thenReturn(new User(username, password, new ArrayList<>()));

        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
    }

    @Test
    @DisplayName("회원 가입 성공")
    public void signUpSuccess() throws Exception {
        // Arrange
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .email(username)
                .password(password)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated())
                .andDo(document("sign-up",
                        requestFields(
                                fieldWithPath("email").description("회원가입 이메일"),
                                fieldWithPath("password").description("회원가입 비밀번호")
                        ),
                        responseHeaders(
                                headerWithName("Location").description("생성된 회원 정보 URL")
                        )
                ));
    }

    @Test
    @DisplayName("로그인 성공")
    public void loginSuccess() throws Exception {
        // Arrange
        SignInRequest signInRequest = SignInRequest.builder()
                .email(username)
                .password(password)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isOk())
                .andDo(document("login",
                        requestFields(
                                fieldWithPath("email").description("로그인 이메일"),
                                fieldWithPath("password").description("로그인 비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("access_token").description("액세스 토큰"),
                                fieldWithPath("refresh_token").description("리프레시 토큰")
                        )
                ));
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    public void reissueTokenSuccess() throws Exception {
        // Arrange
        RefreshTokenRequest refreshTokenRequest = RefreshTokenRequest.builder()
                .refreshToken("refresh_token")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andDo(document("reissue-token",
                        requestFields(
                                fieldWithPath("refresh_token").description("재발급 요청 토큰")
                        ),
                        responseFields(
                                fieldWithPath("access_token").description("액세스 토큰"),
                                fieldWithPath("refresh_token").description("리프레시 토큰")
                        )
                ));
    }
}