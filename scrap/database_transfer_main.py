import sqlite3
import json

conn = sqlite3.connect('courses.db')
cursor = conn.cursor()

conn = sqlite3.connect('courses.db')
cursor = conn.cursor()

cursor.execute('''
    CREATE TABLE IF NOT EXISTS courses (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        course_id TEXT,
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
    )
''')

cursor.execute('''
    CREATE TABLE IF NOT EXISTS meeting_sections (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        course_id TEXT,
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
for i in range(1,393):
    file_path = f'result/{i}.json'
    with open(file_path, 'r', encoding='utf-8') as file:
        data = json.load(file)

    courses_data = data['payload']["pageableCourse"]["courses"]

    for course_data in courses_data:
        course = {}
        course['course_id'] = course_data["id"]
        course['course_code'] = course_data["code"]
        course['name'] = course_data["name"]
        course['description'] = ''
        course['division'] = ''
        course['prerequisites'] = ''
        course['exclusions'] = ''
        if not course_data["cmCourseInfo"] == None:
            course['description'] = course_data["cmCourseInfo"]['description']
            course['division'] = course_data["cmCourseInfo"]['division']
            course['prerequisites'] = course_data["cmCourseInfo"]['prerequisitesText']
            course['exclusions'] = course_data["cmCourseInfo"]['exclusionsText']
        course['department'] = course_data["department"]["name"]
        course['campus'] = course_data["campus"]
        course['sessions'] = course_data["sessions"]
        
        cursor.execute('''
            INSERT INTO courses (course_id, course_code, name, description, division, department, prerequisites, exclusions, campus, sessions)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ''', (
            course['course_id'],
            course['course_code'],
            course['name'],
            course['description'],
            course['division'],
            course['department'],
            course['prerequisites'],
            course['exclusions'],
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
            for instructor in meeting_time_data['instructors']:
                meeting_section['instructors'].append(instructor["firstName"] + ' ' +  instructor["lastName"])
            meeting_section['times'] = []
            for time in meeting_time_data['meetingTimes']:
                meeting_dic = {}
                meeting_dic['day'] = time["start"]['day']
                meeting_dic['start'] = time["start"]['millisofday']
                meeting_dic['end'] = time["end"]["millisofday"]
                meeting_dic['location'] = time["building"]["buildingCode"]
                if meeting_dic not in meeting_section['times']:
                    meeting_section['times'].append(meeting_dic)
            meeting_section['size'] = meeting_time_data['maxEnrolment']
            meeting_section['enrolment'] = meeting_time_data['currentEnrolment']
            meeting_section['notes'] = meeting_time_data['deliveryModes'][0]['mode']

            cursor.execute('''
                INSERT INTO meeting_sections (course_id,course_code, section_code, instructors, times, size, enrolment, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ''', (
                meeting_section['course_id'],
                meeting_section['course_code'],
                meeting_section['section_code'],
                ','.join(meeting_section['instructors']),
                json.dumps(meeting_section['times']),
                meeting_section['size'],
                meeting_section['enrolment'],
                json.dumps(meeting_section['notes'])
            ))
    print(f'file{i} successfully entered the database')

conn.commit()
conn.close()
