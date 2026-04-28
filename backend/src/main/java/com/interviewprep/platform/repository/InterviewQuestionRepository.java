package com.interviewprep.platform.repository;

import com.interviewprep.platform.entity.InterviewQuestion;
import com.interviewprep.platform.entity.InterviewSession;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

    List<InterviewQuestion> findAllBySessionOrderByIdAsc(InterviewSession session);
}
