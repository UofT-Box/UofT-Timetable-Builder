import sqlite3
import json
import os

# conn = pymysql.connect(
#     host = 'localhost', 
#     database = 'uoft_course_info', 
#     user = 'root', 
#     passwd = '123456',
#     port = 3306
# )

# Using SQLite database
conn = sqlite3.connect('uoft_course_info.db')
cursor = conn.cursor()

cursor.execute('''
    CREATE TABLE IF NOT EXISTS courses (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        course_id VARCHAR(255) UNIQUE,
        course_code TEXT,
        section_code TEXT,
        name TEXT,
        description TEXT,
        division TEXT,
        department TEXT,
        prerequisites TEXT,
        corequisites TEXT,
        exclusions TEXT,
        recommended_preparation TEXT,
        breadth_requirements TEXT,  -- Storing array as a comma-separated string
        distribution_requirements TEXT,  -- Storing array as a comma-separated string
        campus TEXT,
        sessions TEXT  -- Storing array as a comma-separated string
    )
''')

cursor.execute('''
    CREATE TABLE IF NOT EXISTS meeting_sections (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        course_id VARCHAR(255),
        course_code TEXT,
        section_code TEXT,
        instructors TEXT,  -- Storing array as a comma-separated string
        times TEXT,  -- Storing complex structure as JSON string
        size INTEGER,
        enrolment INTEGER,
        notes TEXT,  -- Storing complex structure as JSON string
        FOREIGN KEY(course_id) REFERENCES courses(course_id)
    )
''')
number_of_files = len(os.listdir("./result"))
for i in range(1, number_of_files + 1):
    file_path = f'result/{i}.json'
    with open(file_path, 'r', encoding='utf-8') as file:
        data = json.load(file)

    courses_data = data['payload']["pageableCourse"]["courses"]

    for course_data in courses_data:
        course = {}
        course['course_id'] = course_data["id"]
        course['course_code'] = course_data["code"]
        course['section_code'] = course_data["sectionCode"]
        course['name'] = course_data["name"]
        course['description'] = ''
        course['division'] = ''
        course['prerequisites'] = ''
        course['corequisites'] = ''
        course['exclusions'] = ''
        course['recommended_preparation'] = ''
        course['breadth_requirements'] = []
        course['distribution_requirements'] = []
        if course_data.get("cmCourseInfo"):
            course['description'] = course_data["cmCourseInfo"].get('description', '')
            course['division'] = course_data["cmCourseInfo"].get('division', '')
            course['prerequisites'] = course_data["cmCourseInfo"].get('prerequisitesText', '')
            course['corequisites'] = course_data["cmCourseInfo"].get('corequisitesText', '')
            course['exclusions'] = course_data["cmCourseInfo"].get('exclusionsText', '')
            course['recommended_preparation'] = course_data["cmCourseInfo"].get('recommendedPreparation', '')
            course['breadth_requirements'] = course_data["cmCourseInfo"].get('breadthRequirements', [])
            course['distribution_requirements'] = course_data["cmCourseInfo"].get('distributionRequirements', [])
        course['department'] = course_data["department"]["name"] if course_data.get("department") else ''
        course['campus'] = course_data["campus"]
        course['sessions'] = course_data["sessions"]

        cursor.execute('''
            INSERT OR IGNORE INTO courses (course_id, course_code, section_code, name, description, division, department, prerequisites, corequisites, exclusions, recommended_preparation, breadth_requirements, distribution_requirements, campus, sessions)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (
            course['course_id'],
            course['course_code'],
            course['section_code'],
            course['name'],
            course['description'],
            course['division'],
            course['department'],
            course['prerequisites'],
            course['corequisites'],
            course['exclusions'],
            course['recommended_preparation'],
            ', '.join(course['breadth_requirements']) if course['breadth_requirements'] else '',
            ', '.join(course['distribution_requirements']) if course['distribution_requirements'] else '',
            course['campus'],
            ','.join(course['sessions'])
        ))

        meeting_times_data = course_data["sections"]

        for meeting_time_data in meeting_times_data:
            meeting_section = {}
            meeting_section['course_id'] = course_data["id"]
            meeting_section['course_code'] = course_data["code"]
            meeting_section['section_code'] = meeting_time_data['name']
            meeting_section['instructors'] = []
            for instructor in meeting_time_data.get('instructors', []):
                first = instructor.get("firstName", "")
                last = instructor.get("lastName", "")
                full_name = (first + ' ' + last).strip()
                if full_name:
                    meeting_section['instructors'].append(full_name)

            meeting_section['times'] = []
            for time in meeting_time_data.get('meetingTimes', []):
                meeting_dic = {
                    'day': time["start"]['day'],
                    'start': time["start"]['millisofday'],
                    'end': time["end"]["millisofday"],
                    'location': time["building"]["buildingCode"]
                }
                if meeting_dic not in meeting_section['times']:
                    meeting_section['times'].append(meeting_dic)

            meeting_section['size'] = meeting_time_data.get('maxEnrolment', 0)
            meeting_section['enrolment'] = meeting_time_data.get('currentEnrolment', 0)

            delivery = meeting_time_data.get("deliveryModes", [])
            meeting_section['notes'] = ''
            if delivery:
                first = delivery[0] or {}
                meeting_section['notes'] = first.get('mode', '') or ''
                
            cursor.execute('''
                INSERT INTO meeting_sections (course_id, course_code, section_code, instructors, times, size, enrolment, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ''', (
                meeting_section['course_id'],
                meeting_section['course_code'],
                meeting_section['section_code'],
                ','.join(meeting_section['instructors']) if meeting_section['instructors'] else '',
                json.dumps(meeting_section['times']),
                meeting_section['size'],
                meeting_section['enrolment'],
                json.dumps(meeting_section['notes'])
            ))

    print(f'file{i} successfully entered the database')

conn.commit()
conn.close()