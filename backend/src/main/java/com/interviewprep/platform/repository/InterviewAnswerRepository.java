package com.interviewprep.platform.repository;

import com.interviewprep.platform.entity.InterviewAnswer;
import com.interviewprep.platform.entity.InterviewQuestion;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {

    List<InterviewAnswer> findAllByQuestion_Session_IdOrderByCreatedAtDesc(Long sessionId);

    Optional<InterviewAnswer> findTopByQuestionOrderByCreatedAtDesc(InterviewQuestion question);

    @Query("""
            select coalesce(avg((a.correctnessScore + a.confidenceScore + a.relevanceScore) / 3), 0)
            from InterviewAnswer a
            where a.question.session.id = :sessionId
            """)
    BigDecimal calculateAverageScoreBySessionId(Long sessionId);

    long countByQuestion_Session_User_Id(Long userId);
}
