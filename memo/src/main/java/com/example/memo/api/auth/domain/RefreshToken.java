package com.example.memo.api.auth.domain;

import com.example.memo.web.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity(name = "refresh_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private String token;

    private Instant expiryDate;

    @Builder
    public RefreshToken(Member member, String token, Instant expiryDate) {
        this.member = member;
        this.token = token;
        this.expiryDate = expiryDate;
    }
}


