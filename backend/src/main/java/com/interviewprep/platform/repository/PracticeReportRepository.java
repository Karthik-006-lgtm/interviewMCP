package com.interviewprep.platform.repository;

import com.interviewprep.platform.entity.InterviewSession;
import com.interviewprep.platform.entity.PracticeReport;
import com.interviewprep.platform.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PracticeReportRepository extends JpaRepository<PracticeReport, Long> {

    List<PracticeReport> findAllByUserOrderByCreatedAtDesc(User user);

    Optional<PracticeReport> findBySession(InterviewSession session);
}
