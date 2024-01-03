import sqlite3
import json


def fetch_special_sections(conn, course_code, section_code):
    cursor = conn.cursor()

    query = "SELECT * FROM courses WHERE course_code = ? AND section_code = ?"
    param = (course_code, section_code)
    cursor.execute(query, param)
    course = cursor.fetchall()

    course_id = course[0][1]  # get id
    cursor.execute("SELECT * FROM meeting_sections WHERE course_id = ?", (course_id,))
    meeting_times = cursor.fetchall()

    return meeting_times


def connect_to_database(db_path):
    conn = sqlite3.connect(db_path)
    return conn


def can_schedule(timetable, time_slot):
    # 是否冲突
    for scheduled_slot in timetable:
        if scheduled_slot["time"]["day"] == time_slot["day"] and not (
            scheduled_slot["time"]["end"] <= time_slot["start"]
            or scheduled_slot["time"]["start"] >= time_slot["end"]
        ):
            return False
    return True


def generate_timetable(courses, timetable, course_keys, course_index=0, type_index=0):
    if course_index == len(course_keys):
        return True  # 所有的课程的所有课程类型已排列完成

    course_key = course_keys[course_index]
    course_types = list(courses[course_key].keys())

    if type_index == len(course_types):
        # 当前课程的所有类型已经安排完成
        return generate_timetable(courses, timetable, course_keys, course_index + 1, 0)

    course_type = course_types[type_index]
    sections = courses[course_key][course_type]

    for section, time_slots in sections.items():
        for time_slot in time_slots:
            if can_schedule(timetable, time_slot):
                # 如果没有冲突则安排课程表
                timetable_entry = {
                    "course": course_key,
                    "section": section,
                    "time": time_slot,
                }
                timetable.append(timetable_entry)
                if generate_timetable(
                    courses, timetable, course_keys, course_index, type_index + 1
                ):
                    return True
                timetable.pop()  # 回溯

    return False  # 寻找失败


def main():
    db_path = "courses.db"
    conn = connect_to_database(db_path)

    course_code_lst = ["CSC108H1", "CHM136H1", "BIO130H1", "CSC165H1", "MAT136H1","STA130H1"]
    section_code = "S"
    courses_example = {}

    for course_code in course_code_lst:
        sections = fetch_special_sections(conn, course_code, section_code)
        courses_example[course_code] = {}

        for section in sections:
            section_type = section[3][:3]
            data_list = json.loads(section[5])
            if section_type in courses_example[course_code]:
                courses_example[course_code][section_type][section[3]] = data_list
            else:
                courses_example[course_code][section_type] = {}
                courses_example[course_code][section_type][section[3]] = data_list

    # print(courses_example)
    timetable = []
    course_keys = list(courses_example.keys())

    # 生成课程表
    if generate_timetable(courses_example, timetable, course_keys):
        print("成功生成课程表:", timetable)
    else:
        print("没有符合条件的课程表")


if __name__ == "__main__":
    main()
