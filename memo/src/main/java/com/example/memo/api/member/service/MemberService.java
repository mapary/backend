package com.example.memo.api.member.service;

import com.example.memo.api.auth.dto.SignUpRequest;
import com.example.memo.api.common.exceptions.EntityNotFoundException;
import com.example.memo.api.common.exceptions.ErrorCode;
import com.example.memo.api.common.exceptions.InvalidValueException;
import com.example.memo.api.member.domain.Member;
import com.example.memo.api.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) {
        var member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .build();
    }

    @Transactional
    public Long save(SignUpRequest member) {
        validateDuplicateEmail(member.getEmail());
        var newMember = new Member(member.getEmail(), member.getPassword());
        newMember.encodePassword(passwordEncoder);
        return memberRepository.save(newMember).getId();
    }

    private void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new InvalidValueException(ErrorCode.DUPLICATED_EMAIL);
        }
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }
}