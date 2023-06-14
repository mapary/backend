package com.example.memo.config.oauth;

import com.example.memo.config.oauth.userinfo.CustomOAuth2UserInfo;
import com.example.memo.web.member.domain.Member;
import com.example.memo.web.member.repository.MemberRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        var user = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        var userInfo = CustomOAuth2UserInfo.of(registrationId, user.getAttributes());

        Optional<Member> foundMember = memberRepository.findByEmail(userInfo.getEmail());
        if (foundMember.isEmpty()) {
            Member member = Member.builder()
                .email(userInfo.getEmail())
                .password(createDummyPassword())
                .build();
            member.encodePassword(passwordEncoder);
            memberRepository.save(member);
            return new CustomOAuth2UserPrincipal(member, userInfo);
        }
        return new CustomOAuth2UserPrincipal(foundMember.get(), userInfo);
    }

    private String createDummyPassword() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}