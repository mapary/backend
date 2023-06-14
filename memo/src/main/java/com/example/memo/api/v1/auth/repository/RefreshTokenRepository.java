package com.example.memo.api.v1.auth.repository;

import com.example.memo.api.v1.auth.domain.RefreshToken;
import com.example.memo.web.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByMemberAndToken(Member member, String token);

    Long deleteByMember(Member member);
}
