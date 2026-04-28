package com.interviewprep.platform.repository;

import com.interviewprep.platform.entity.RevokedToken;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

    boolean existsByTokenHashAndExpiresAtAfter(String tokenHash, Instant instant);

    void deleteByExpiresAtBefore(Instant instant);
}
