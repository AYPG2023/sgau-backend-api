package com.umg.sgau.auth.repository;

import com.umg.sgau.auth.entity.LoginChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoginChallengeRepository extends JpaRepository<LoginChallenge, Long> {
    @Modifying
    @Query(value = "UPDATE login_challenges SET consumed_at = CURRENT_TIMESTAMP WHERE challenge_hash = :hash "
            + "AND consumed_at IS NULL AND expires_at > CURRENT_TIMESTAMP", nativeQuery = true)
    int consumeIfValid(@Param("hash") String hash);
}
