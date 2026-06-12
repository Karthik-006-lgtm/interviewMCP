-- H2 Database Schema Initialization
-- This will be auto-executed by Spring Boot on startup

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_name VARCHAR(40) NOT NULL,
    PRIMARY KEY (user_id, role_name),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(140) NOT NULL UNIQUE,
    summary TEXT NOT NULL,
    core_skills TEXT NOT NULL,
    interview_focus_areas TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS resumes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    extracted_text TEXT,
    candidate_name VARCHAR(160),
    contact_info TEXT,
    summary TEXT,
    strengths TEXT,
    weaknesses TEXT,
    extracted_skills TEXT,
    recommended_roles TEXT,
    education TEXT,
    experience TEXT,
    projects TEXT,
    certifications TEXT,
    missing_skills TEXT,
    strength_indicators TEXT,
    weakness_indicators TEXT,
    improvement_roadmap TEXT,
    learning_suggestions TEXT,
    mentor_guidance TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS resume_skills (
    resume_profile_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    PRIMARY KEY (resume_profile_id, skill_id),
    FOREIGN KEY (resume_profile_id) REFERENCES resumes(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(140) NOT NULL,
    website VARCHAR(255) NOT NULL UNIQUE,
    hr_contact VARCHAR(160),
    hiring_manager VARCHAR(160),
    owner_name VARCHAR(160),
    employee_count INTEGER NOT NULL,
    company_history TEXT NOT NULL,
    culture TEXT NOT NULL DEFAULT 'Collaborative, delivery-focused, and outcome-oriented.',
    interview_focus_areas TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS company_roles (
    company_id BIGINT NOT NULL,
    role_name VARCHAR(140) NOT NULL,
    PRIMARY KEY (company_id, role_name),
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS interviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    resume_profile_id BIGINT,
    selected_roles TEXT NOT NULL,
    personality_profile VARCHAR(160) NOT NULL,
    technical_skills TEXT NOT NULL,
    target_company_id BIGINT,
    target_company_name VARCHAR(160),
    target_company_website VARCHAR(255),
    interviewer_tone VARCHAR(80),
    coaching_intensity VARCHAR(40),
    live_coaching_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    adaptive_difficulty_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    reality_mode VARCHAR(60),
    camera_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    current_difficulty_level VARCHAR(40),
    overall_score DECIMAL(5,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (resume_profile_id) REFERENCES resumes(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS interview_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    prompt TEXT NOT NULL,
    category VARCHAR(40) NOT NULL,
    expected_answer_points TEXT,
    difficulty VARCHAR(40) NOT NULL,
    interviewer_cue TEXT,
    time_pressure_seconds INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES interviews(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    transcript TEXT NOT NULL,
    correctness_score DECIMAL(5,2) NOT NULL,
    confidence_score DECIMAL(5,2) NOT NULL,
    relevance_score DECIMAL(5,2) NOT NULL,
    clarity_score DECIMAL(5,2) NOT NULL DEFAULT 0,
    completeness_score DECIMAL(5,2) NOT NULL DEFAULT 0,
    structure_score DECIMAL(5,2) NOT NULL DEFAULT 0,
    impact_score DECIMAL(5,2) NOT NULL DEFAULT 0,
    hesitation_score DECIMAL(5,2) NOT NULL DEFAULT 0,
    filler_word_count INTEGER NOT NULL DEFAULT 0,
    emotion_signal VARCHAR(120),
    grammar_feedback TEXT NOT NULL,
    vocabulary_feedback TEXT NOT NULL DEFAULT '',
    tone_feedback TEXT NOT NULL DEFAULT '',
    fluency_feedback TEXT NOT NULL DEFAULT '',
    pronunciation_feedback TEXT NOT NULL DEFAULT '',
    mentor_suggestions TEXT NOT NULL,
    polished_answer TEXT NOT NULL DEFAULT '',
    live_coaching_hints TEXT,
    weakness_signals TEXT,
    weekly_improvement_plan TEXT,
    practice_tasks TEXT,
    targeted_questions TEXT,
    adaptive_difficulty_note TEXT,
    next_difficulty_level VARCHAR(40),
    audio_path VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES interview_questions(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id BIGINT NOT NULL UNIQUE,
    title VARCHAR(180) NOT NULL,
    executive_summary TEXT NOT NULL,
    improvement_areas TEXT NOT NULL,
    weak_areas TEXT NOT NULL DEFAULT '[]',
    recommended_actions TEXT NOT NULL DEFAULT '[]',
    next_steps TEXT NOT NULL,
    progress_summary TEXT NOT NULL DEFAULT '',
    weekly_improvement_plan TEXT NOT NULL DEFAULT '[]',
    practice_tasks TEXT NOT NULL DEFAULT '[]',
    targeted_questions TEXT NOT NULL DEFAULT '[]',
    overall_score DECIMAL(5,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (session_id) REFERENCES interviews(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS revoked_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_hash VARCHAR(128) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
