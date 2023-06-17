package com.example.memo.api.v1.auth.controller;

import com.example.memo.api.auth.dto.RefreshTokenRequest;
import com.example.memo.api.auth.dto.SignInRequest;
import com.example.memo.api.auth.dto.SignUpRequest;
import com.example.memo.api.auth.repository.RefreshTokenRepository;
import com.example.memo.api.member.repository.MemberRepository;
import com.example.memo.api.member.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private final String username = "testuser@test.com";
    private final String password = "testPassword123";

    @BeforeEach
    public void setUp() {
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();

        SignUpRequest signUpRequest = SignUpRequest.builder()
                .email(username)
                .password(password)
                .build();
        memberService.save(signUpRequest);
    }

    @DisplayName("로그인을 성공적으로 수행한다.")
    @Test
    public void testAuthorize() throws Exception {
        SignInRequest signInRequest = SignInRequest.builder()
                .email(username)
                .password(password)
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.refresh_token").isNotEmpty());
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인을 시도하면 실패한다.")
    public void loginFailDueToWrongPassword() throws Exception {
        SignInRequest request = SignInRequest.builder()
                .email(username)
                .password(password + "wrong")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("리프레시 토큰으로 새로운 토큰을 성공적으로 발급한다.")
    @Test
    public void testReissue() throws Exception {
        SignInRequest signInRequest = SignInRequest.builder()
                .email(username)
                .password(password)
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest)))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        String refreshToken = JsonPath.read(response, "$.refresh_token");

        RefreshTokenRequest refreshTokenRequest = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();

        mockMvc.perform(post("/api/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.refresh_token").isNotEmpty());
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 새로운 토큰을 발급받을 수 없다.")
    public void reissueFailDueToInvalidRefreshToken() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("invalid_refresh_token")
                .build();

        mockMvc.perform(post("/api/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("중복된 이메일로 회원가입을 시도하면 실패한다.")
    public void signUpFailDueToDuplicateEmail() throws Exception {
        SignUpRequest request = SignUpRequest.builder()
                .email(username)
                .password("AnotherPassword123")
                .build();

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("새로운 사용자를 성공적으로 등록한다.")
    @Test
    public void testRegisterUser() throws Exception {
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .email("newuser@test.com")
                .password("newPassword1234")
                .build();

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated());
    }
}