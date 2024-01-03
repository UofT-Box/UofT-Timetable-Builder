CREATE TABLE IF NOT EXISTS courses (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    course_id VARCHAR(255) UNIQUE,
    course_code TEXT,
    section_code TEXT,
    name TEXT,
    description TEXT,
    division TEXT,
    department TEXT,
    prerequisites TEXT,
    exclusions TEXT,
    campus TEXT,
    sessions TEXT  -- Storing array as a comma-separated string
);

CREATE TABLE IF NOT EXISTS meeting_sections (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    course_id VARCHAR(255),
    course_code TEXT,
    section_code TEXT,
    instructors TEXT,  -- Storing array as a comma-separated string
    times TEXT,  -- Storing complex structure as JSON string
    size INTEGER,
    enrolment INTEGER,
    notes TEXT,  -- Storing complex structure as JSON string
    FOREIGN KEY(course_id) REFERENCES courses(course_id)
);

CREATE TABLE IF NOT EXISTS distances (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    origin TEXT,
    destination TEXT,
    distance INTEGER, -- distance in meters
    duration INTEGER -- time in minute
);