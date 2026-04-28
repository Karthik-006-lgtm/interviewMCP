package com.interviewprep.platform.repository;

import com.interviewprep.platform.entity.InterviewSession;
import com.interviewprep.platform.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

    List<InterviewSession> findAllByUserOrderByCreatedAtDesc(User user);
}
