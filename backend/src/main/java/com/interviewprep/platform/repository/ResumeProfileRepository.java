package com.interviewprep.platform.repository;

import com.interviewprep.platform.entity.ResumeProfile;
import com.interviewprep.platform.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeProfileRepository extends JpaRepository<ResumeProfile, Long> {

    List<ResumeProfile> findAllByUserOrderByCreatedAtDesc(User user);

    Optional<ResumeProfile> findTopByUserOrderByCreatedAtDesc(User user);
}
