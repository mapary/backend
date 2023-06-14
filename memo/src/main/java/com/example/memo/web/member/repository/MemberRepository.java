package com.example.memo.web.member.repository;

import com.example.memo.exception.MemberNotFoundException;
import com.example.memo.web.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    default Member findByEmailOrThrow(String email) {
        return findByEmail(email)
            .orElseThrow(MemberNotFoundException::new);
    }
}

