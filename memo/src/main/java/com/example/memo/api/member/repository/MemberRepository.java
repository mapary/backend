package com.example.memo.api.member.repository;

import com.example.memo.api.member.domain.Member;
import com.example.memo.exception.MemberNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    default Member findByEmailOrThrow(String email) {
        return findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
    }
}

