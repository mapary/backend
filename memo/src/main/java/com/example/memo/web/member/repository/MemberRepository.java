package com.example.memo.member.repository;

import com.example.memo.config.exception.MemberNotFoundException;
import com.example.memo.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    default Member findByEmailOrThrow(String email) {
        return findByEmail(email)
            .orElseThrow(MemberNotFoundException::new);
    }
}

