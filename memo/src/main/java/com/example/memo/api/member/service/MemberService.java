package com.example.memo.api.member.service;

import com.example.memo.api.auth.dto.SignUpRequest;
import com.example.memo.api.member.domain.Member;
import com.example.memo.api.member.repository.MemberRepository;
import com.example.memo.exception.DuplicateMemberException;
import com.example.memo.exception.MemberNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
    public UserDetails loadUserByUsername(String username) throws MemberNotFoundException {
        var member = memberRepository.findByEmailOrThrow(username);
        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .build();
    }

    @Transactional
    public Long save(SignUpRequest member) {
        try {
            var newMember = new Member(member.getEmail(), member.getPassword());
            newMember.encodePassword(passwordEncoder);

            return memberRepository.save(newMember).getId();
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateMemberException(member.getEmail());
        }
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
    }
}