CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_name VARCHAR(40) NOT NULL,
    PRIMARY KEY (user_id, role_name)
);

CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(140) NOT NULL UNIQUE,
    summary TEXT NOT NULL,
    core_skills TEXT NOT NULL,
    interview_focus_areas TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS skills (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS resumes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size INTEGER NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    upload_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_parsed BOOLEAN NOT NULL DEFAULT FALSE,
    parsing_status VARCHAR(50) NOT NULL DEFAULT 'pending',
    parsing_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS parsed_resumes (
    id BIGSERIAL PRIMARY KEY,
    resume_id BIGINT NOT NULL REFERENCES resumes(id) ON DELETE CASCADE UNIQUE,
    full_name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    location VARCHAR(255),
    linkedin_url VARCHAR(500),
    github_url VARCHAR(500),
    portfolio_url VARCHAR(500),
    summary TEXT,
    skills JSONB,
    technical_skills JSONB,
    soft_skills JSONB,
    work_experience JSONB,
    total_years_experience FLOAT,
    education JSONB,
    certifications JSONB,
    projects JSONB,
    languages JSONB,
    parsing_confidence FLOAT CHECK (parsing_confidence >= 0.0 AND parsing_confidence <= 1.0),
    parsed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    parser_version VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS skills (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    category VARCHAR(100),
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS user_skills (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    proficiency_level VARCHAR(50),
    years_of_experience FLOAT,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_by VARCHAR(100),
    last_updated TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    source VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, skill_id)
);

CREATE TABLE IF NOT EXISTS skill_gaps (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_role VARCHAR(255),
    target_company VARCHAR(255),
    missing_skills JSONB,
    recommended_skills JSONB,
    skill_priority JSONB,
    gap_score FLOAT CHECK (gap_score >= 0.0 AND gap_score <= 1.0),
    analysis_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    analysis_version VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS resume_skills (
    resume_profile_id BIGINT NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    PRIMARY KEY (resume_profile_id, skill_id)
);

CREATE TABLE IF NOT EXISTS companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(140) NOT NULL,
    website VARCHAR(255) NOT NULL UNIQUE,
    hr_contact VARCHAR(160),
    hiring_manager VARCHAR(160),
    owner_name VARCHAR(160),
    employee_count INTEGER NOT NULL,
    company_history TEXT NOT NULL,
    culture TEXT NOT NULL,
    interview_focus_areas TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS company_roles (
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    role_name VARCHAR(140) NOT NULL,
    PRIMARY KEY (company_id, role_name)
);

CREATE TABLE IF NOT EXISTS interviews (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    resume_profile_id BIGINT REFERENCES resumes(id) ON DELETE SET NULL,
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
    overall_score NUMERIC(5,2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS interview_questions (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES interviews(id) ON DELETE CASCADE,
    prompt TEXT NOT NULL,
    category VARCHAR(40) NOT NULL,
    expected_answer_points TEXT,
    difficulty VARCHAR(40) NOT NULL,
    interviewer_cue TEXT,
    time_pressure_seconds INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS answers (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES interview_questions(id) ON DELETE CASCADE,
    transcript TEXT NOT NULL,
    correctness_score NUMERIC(5,2) NOT NULL,
    confidence_score NUMERIC(5,2) NOT NULL,
    relevance_score NUMERIC(5,2) NOT NULL,
    clarity_score NUMERIC(5,2) NOT NULL,
    completeness_score NUMERIC(5,2) NOT NULL,
    structure_score NUMERIC(5,2) NOT NULL DEFAULT 0,
    impact_score NUMERIC(5,2) NOT NULL DEFAULT 0,
    hesitation_score NUMERIC(5,2) NOT NULL DEFAULT 0,
    filler_word_count INTEGER NOT NULL DEFAULT 0,
    emotion_signal VARCHAR(120),
    grammar_feedback TEXT NOT NULL,
    vocabulary_feedback TEXT NOT NULL,
    tone_feedback TEXT NOT NULL,
    fluency_feedback TEXT NOT NULL,
    pronunciation_feedback TEXT NOT NULL,
    mentor_suggestions TEXT NOT NULL,
    polished_answer TEXT NOT NULL,
    live_coaching_hints TEXT,
    weakness_signals TEXT,
    weekly_improvement_plan TEXT,
    practice_tasks TEXT,
    targeted_questions TEXT,
    adaptive_difficulty_note TEXT,
    next_difficulty_level VARCHAR(40),
    audio_path VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS reports (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_id BIGINT NOT NULL UNIQUE REFERENCES interviews(id) ON DELETE CASCADE,
    title VARCHAR(180) NOT NULL,
    executive_summary TEXT NOT NULL,
    improvement_areas TEXT NOT NULL,
    weak_areas TEXT NOT NULL,
    recommended_actions TEXT NOT NULL,
    next_steps TEXT NOT NULL,
    progress_summary TEXT NOT NULL,
    weekly_improvement_plan TEXT NOT NULL DEFAULT '[]',
    practice_tasks TEXT NOT NULL DEFAULT '[]',
    targeted_questions TEXT NOT NULL DEFAULT '[]',
    overall_score NUMERIC(5,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS revoked_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(128) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE resumes ADD COLUMN IF NOT EXISTS candidate_name VARCHAR(160);
ALTER TABLE resumes ADD COLUMN IF NOT EXISTS contact_info TEXT;
ALTER TABLE resumes ADD COLUMN IF NOT EXISTS education TEXT;
ALTER TABLE resumes ADD COLUMN IF NOT EXISTS experience TEXT;
ALTER TABLE resumes ADD COLUMN IF NOT EXISTS projects TEXT;
ALTER TABLE resumes ADD COLUMN IF NOT EXISTS certifications TEXT;
ALTER TABLE resumes ADD COLUMN IF NOT EXISTS missing_skills TEXT;
ALTER TABLE resumes ADD COLUMN IF NOT EXISTS strength_indicators TEXT;
ALTER TABLE resumes ADD COLUMN IF NOT EXISTS weakness_indicators TEXT;
ALTER TABLE resumes ADD COLUMN IF NOT EXISTS improvement_roadmap TEXT;
ALTER TABLE resumes ADD COLUMN IF NOT EXISTS learning_suggestions TEXT;
ALTER TABLE resumes ADD COLUMN IF NOT EXISTS mentor_guidance TEXT;

ALTER TABLE companies ADD COLUMN IF NOT EXISTS culture TEXT NOT NULL DEFAULT 'Collaborative, delivery-focused, and outcome-oriented.';
ALTER TABLE companies ADD COLUMN IF NOT EXISTS interview_focus_areas TEXT;

ALTER TABLE answers ADD COLUMN IF NOT EXISTS clarity_score NUMERIC(5,2) NOT NULL DEFAULT 0;
ALTER TABLE answers ADD COLUMN IF NOT EXISTS completeness_score NUMERIC(5,2) NOT NULL DEFAULT 0;
ALTER TABLE answers ADD COLUMN IF NOT EXISTS vocabulary_feedback TEXT NOT NULL DEFAULT '';
ALTER TABLE answers ADD COLUMN IF NOT EXISTS tone_feedback TEXT NOT NULL DEFAULT '';
ALTER TABLE answers ADD COLUMN IF NOT EXISTS fluency_feedback TEXT NOT NULL DEFAULT '';
ALTER TABLE answers ADD COLUMN IF NOT EXISTS pronunciation_feedback TEXT NOT NULL DEFAULT '';
ALTER TABLE answers ADD COLUMN IF NOT EXISTS polished_answer TEXT NOT NULL DEFAULT '';
ALTER TABLE answers ADD COLUMN IF NOT EXISTS structure_score NUMERIC(5,2) NOT NULL DEFAULT 0;
ALTER TABLE answers ADD COLUMN IF NOT EXISTS impact_score NUMERIC(5,2) NOT NULL DEFAULT 0;
ALTER TABLE answers ADD COLUMN IF NOT EXISTS hesitation_score NUMERIC(5,2) NOT NULL DEFAULT 0;
ALTER TABLE answers ADD COLUMN IF NOT EXISTS filler_word_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE answers ADD COLUMN IF NOT EXISTS emotion_signal VARCHAR(120);
ALTER TABLE answers ADD COLUMN IF NOT EXISTS live_coaching_hints TEXT;
ALTER TABLE answers ADD COLUMN IF NOT EXISTS weakness_signals TEXT;
ALTER TABLE answers ADD COLUMN IF NOT EXISTS weekly_improvement_plan TEXT;
ALTER TABLE answers ADD COLUMN IF NOT EXISTS practice_tasks TEXT;
ALTER TABLE answers ADD COLUMN IF NOT EXISTS targeted_questions TEXT;
ALTER TABLE answers ADD COLUMN IF NOT EXISTS adaptive_difficulty_note TEXT;
ALTER TABLE answers ADD COLUMN IF NOT EXISTS next_difficulty_level VARCHAR(40);

ALTER TABLE reports ADD COLUMN IF NOT EXISTS weak_areas TEXT NOT NULL DEFAULT '[]';
ALTER TABLE reports ADD COLUMN IF NOT EXISTS recommended_actions TEXT NOT NULL DEFAULT '[]';
ALTER TABLE reports ADD COLUMN IF NOT EXISTS progress_summary TEXT NOT NULL DEFAULT '';
ALTER TABLE reports ADD COLUMN IF NOT EXISTS weekly_improvement_plan TEXT NOT NULL DEFAULT '[]';
ALTER TABLE reports ADD COLUMN IF NOT EXISTS practice_tasks TEXT NOT NULL DEFAULT '[]';
ALTER TABLE reports ADD COLUMN IF NOT EXISTS targeted_questions TEXT NOT NULL DEFAULT '[]';

ALTER TABLE interviews ADD COLUMN IF NOT EXISTS target_company_id BIGINT;
ALTER TABLE interviews ADD COLUMN IF NOT EXISTS target_company_name VARCHAR(160);
ALTER TABLE interviews ADD COLUMN IF NOT EXISTS target_company_website VARCHAR(255);
ALTER TABLE interviews ADD COLUMN IF NOT EXISTS interviewer_tone VARCHAR(80);
ALTER TABLE interviews ADD COLUMN IF NOT EXISTS coaching_intensity VARCHAR(40);
ALTER TABLE interviews ADD COLUMN IF NOT EXISTS live_coaching_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE interviews ADD COLUMN IF NOT EXISTS adaptive_difficulty_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE interviews ADD COLUMN IF NOT EXISTS reality_mode VARCHAR(60);
ALTER TABLE interviews ADD COLUMN IF NOT EXISTS camera_enabled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE interviews ADD COLUMN IF NOT EXISTS current_difficulty_level VARCHAR(40);

ALTER TABLE interview_questions ADD COLUMN IF NOT EXISTS interviewer_cue TEXT;
ALTER TABLE interview_questions ADD COLUMN IF NOT EXISTS time_pressure_seconds INTEGER;

INSERT INTO roles (name, summary, core_skills, interview_focus_areas)
VALUES
    ('Java Developer', 'Build and scale backend systems with Java, Spring, and relational data stores.',
     '["Java","Spring","Spring Boot","SQL","Microservices","REST APIs"]',
     '["coding","system design","debugging","API design","performance","ownership"]'),
    ('Python Developer', 'Develop automation, APIs, analytics workflows, and ML-adjacent services in Python.',
     '["Python","FastAPI","Flask","Pandas","SQL","Automation"]',
     '["problem solving","backend design","data handling","testing","scripting","ownership"]'),
    ('Full Stack Developer', 'Ship end-to-end product features across frontend, backend, API, and database layers.',
     '["React","TypeScript","Java","Spring","PostgreSQL","REST APIs"]',
     '["feature design","cross-functional delivery","trade-offs","testing","scalability","UX awareness"]'),
    ('Data Analyst', 'Turn messy data into useful decisions through analysis, dashboards, and stakeholder communication.',
     '["Python","SQL","Analytics","Statistics","Visualization","Dashboards"]',
     '["data storytelling","metric design","business context","SQL depth","insight communication","prioritization"]'),
    ('DevOps Engineer', 'Improve deployment speed, reliability, and observability across infrastructure and delivery pipelines.',
     '["Docker","Kubernetes","AWS","Terraform","CI/CD","Linux"]',
     '["incident response","automation","observability","resilience","cost-awareness","platform thinking"]'),
    ('Frontend Engineer', 'Create performant, usable, and maintainable interfaces across modern web applications.',
     '["React","TypeScript","JavaScript","HTML","CSS","Tailwind CSS"]',
     '["UI architecture","performance","accessibility","state management","testing","product thinking"]'),
    ('Backend Engineer', 'Design stable backend services, data models, and integration-heavy application layers.',
     '["Java","Spring","PostgreSQL","REST APIs","Microservices","SQL"]',
     '["API design","data modeling","performance","resilience","debugging","trade-offs"]')
ON CONFLICT (name) DO NOTHING;

INSERT INTO companies (name, website, hr_contact, hiring_manager, owner_name, employee_count, company_history, culture, interview_focus_areas)
VALUES
    ('Northstar Labs', 'https://northstarlabs.example.com', 'Alicia Gomez', 'David Chen', 'Priya Rao', 540,
     'Northstar Labs grew from an internal data platform consultancy into a product-led engineering firm focused on developer tooling, cloud-native analytics, and modern platform reliability.',
     'Highly collaborative and systems-minded, with a bias toward measurable engineering outcomes and platform excellence.',
     '["Java","Spring","Microservices","Platform Reliability","System Design"]'),
    ('Verve Commerce', 'https://vervecommerce.example.com', 'Marcus Bell', 'Sofia Patel', 'Noah Grant', 1300,
     'Verve Commerce operates a fast-scaling retail infrastructure stack and is known for investing in frontend performance, checkout optimization, and experimentation culture.',
     'Fast-moving, customer-obsessed, and experimentation-driven with high expectations around polish and execution.',
     '["React","TypeScript","A/B Testing","Product Metrics","Performance"]'),
    ('Orbit Cloud Systems', 'https://orbitcloud.example.com', 'Neha Kapoor', 'Arjun Mehta', 'Daniel Park', 850,
     'Orbit Cloud Systems builds managed infrastructure products for multi-region deployment, observability, and secure internal developer platforms for enterprise teams.',
     'Reliability-first and deeply technical, with strong emphasis on automation, resilience, and clean operational discipline.',
     '["AWS","Docker","Kubernetes","Observability","Incident Response"]'),
    ('Blue Peak Analytics', 'https://bluepeakanalytics.example.com', 'Rachel Kim', 'Mina Thompson', 'Ethan Brooks', 420,
     'Blue Peak Analytics specializes in decision intelligence, dashboards, and insight workflows for operations, finance, and customer success organizations.',
     'Analytical and stakeholder-facing, with a premium on business clarity, dashboards, and communication quality.',
     '["Python","SQL","Dashboards","Data Storytelling","Visualization"]'),
    ('Harbor Stack', 'https://harborstack.example.com', 'Lena Walsh', 'Karthik Iyer', 'Emma Foster', 760,
     'Harbor Stack delivers full-stack product squads for regulated industries, with deep emphasis on backend resilience, secure APIs, and user-centered interfaces.',
     'Delivery-focused and cross-functional, blending product ownership with secure engineering practices and strong communication.',
     '["Full Stack Delivery","Secure APIs","PostgreSQL","React","Stakeholder Communication"]')
ON CONFLICT (website) DO NOTHING;

INSERT INTO company_roles (company_id, role_name)
SELECT id, role_name
FROM (
    VALUES
        ('Northstar Labs', 'Backend Engineer'),
        ('Northstar Labs', 'Java Developer'),
        ('Northstar Labs', 'Full Stack Developer'),
        ('Northstar Labs', 'DevOps Engineer'),
        ('Verve Commerce', 'Frontend Engineer'),
        ('Verve Commerce', 'Full Stack Developer'),
        ('Orbit Cloud Systems', 'Backend Engineer'),
        ('Orbit Cloud Systems', 'Java Developer'),
        ('Orbit Cloud Systems', 'DevOps Engineer'),
        ('Blue Peak Analytics', 'Data Analyst'),
        ('Blue Peak Analytics', 'Python Developer'),
        ('Harbor Stack', 'Backend Engineer'),
        ('Harbor Stack', 'Java Developer'),
        ('Harbor Stack', 'Full Stack Developer')
) AS seed(company_name, role_name)
JOIN companies c ON c.name = seed.company_name
ON CONFLICT DO NOTHING;

DROP VIEW IF EXISTS role_profiles;
DROP VIEW IF EXISTS resume_profiles;
DROP VIEW IF EXISTS interview_sessions;
DROP VIEW IF EXISTS interview_answers;
DROP VIEW IF EXISTS practice_reports;

CREATE VIEW role_profiles AS
SELECT id, name, summary, core_skills, interview_focus_areas, created_at, updated_at
FROM roles;

CREATE VIEW resume_profiles AS
SELECT id, user_id, original_file_name, storage_path, extracted_text, summary, candidate_name, contact_info,
       strengths, weaknesses, extracted_skills, recommended_roles, education, experience, projects,
       certifications, missing_skills, strength_indicators, weakness_indicators, improvement_roadmap,
       learning_suggestions, mentor_guidance, created_at, updated_at
FROM resumes;

CREATE VIEW interview_sessions AS
SELECT id, user_id, resume_profile_id, selected_roles, personality_profile, technical_skills, target_company_id,
       target_company_name, target_company_website, interviewer_tone, coaching_intensity, live_coaching_enabled,
       adaptive_difficulty_enabled, reality_mode, camera_enabled, current_difficulty_level, overall_score, created_at, updated_at
FROM interviews;

CREATE VIEW interview_answers AS
SELECT id, question_id, transcript, correctness_score, confidence_score, relevance_score, clarity_score,
       completeness_score, structure_score, impact_score, hesitation_score, filler_word_count, emotion_signal,
       grammar_feedback, vocabulary_feedback, tone_feedback, fluency_feedback,
       pronunciation_feedback, mentor_suggestions, polished_answer, live_coaching_hints, weakness_signals,
       weekly_improvement_plan, practice_tasks, targeted_questions, adaptive_difficulty_note, next_difficulty_level,
       audio_path, created_at
FROM answers;

CREATE VIEW practice_reports AS
SELECT id, user_id, session_id, title, executive_summary, improvement_areas, weak_areas, recommended_actions,
       next_steps, progress_summary, weekly_improvement_plan, practice_tasks, targeted_questions, overall_score, created_at
FROM reports;
