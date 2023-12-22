import sqlite3
import json
import time


def fetch_special_sections(conn, course_code, section_code):
    # Get all the meetting times for a course
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


def is_conflict(new_class, timetable):
    # Check that new courses do not conflict with existing courses in the ttb
    for existing_class in timetable:
        if new_class["time"]["day"] == existing_class["time"]["day"]:
            if (
                new_class["time"]["start"] < existing_class["time"]["end"]
                and new_class["time"]["end"] > existing_class["time"]["start"]
            ):
                return True
    return False


def generate_all_timetables(
    courses,
    course_keys,
    index=0,
    type_index=0,
    current_timetable=None,
    all_timetables=None,
):
    if all_timetables is None:
        all_timetables = []
    if current_timetable is None:
        current_timetable = []

    if len(all_timetables) >= 500000:  # Stop condition
        return

    if index == len(course_keys):
        all_timetables.append(current_timetable.copy())
        return

    course_key = course_keys[index]
    course_types = list(courses[course_key].keys())

    if type_index == len(course_types):
        # move to next course
        generate_all_timetables(
            courses, course_keys, index + 1, 0, current_timetable, all_timetables
        )
        return

    course_type = course_types[type_index]
    sections = courses[course_key][course_type]

    for section, time_slots in sections.items():
        for time_slot in time_slots:
            class_info = {"course": course_key, "section": section, "time": time_slot}
            if not is_conflict(class_info, current_timetable):
                current_timetable.append(class_info)
                # move to next meetingtime type
                generate_all_timetables(
                    courses,
                    course_keys,
                    index,
                    type_index + 1,
                    current_timetable,
                    all_timetables,
                )
                current_timetable.pop()


def calculate_timetable_score(timetable):
    # Define scoring criteria
    time_gap_score = 0  # Course interval rating
    daily_concentration_score = 0

    for i in range(len(timetable) - 1):
        for j in range(i + 1, len(timetable)):
            if timetable[i]["time"]["day"] == timetable[j]["time"]["day"]:
                gap = abs(timetable[i]["time"]["end"] - timetable[j]["time"]["start"])
                time_gap_score += gap

    daily_schedules = {}
    for course in timetable:
        day = course["time"]["day"]
        if day not in daily_schedules:
            daily_schedules[day] = []
        daily_schedules[day].append(course)

    for day, courses in daily_schedules.items():
        if len(courses) > 1:
            daily_concentration_score += 10

    total_score = time_gap_score + daily_concentration_score
    return total_score


def main():
    db_path = "courses.db"
    conn = connect_to_database(db_path)

    course_code_lst = [
        "CSC148H1",
        "CHM136H1",
        "BIO130H1",
        "CSC165H1",
        "MAT136H1",
        "STA130H1",
    ]
    course_code_lst.sort()
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
    course_keys = list(courses_example.keys())
    all_timetables = []
    generate_all_timetables(courses_example, course_keys, all_timetables=all_timetables)

    # 1. 为每个课程表计算得分
    timetables_with_scores = [
        (timetable, calculate_timetable_score(timetable))
        for timetable in all_timetables
    ]

    # 2. 根据得分排序课程表
    timetables_with_scores.sort(key=lambda x: x[1], reverse=True)

    # 3. 得分最高的前一百个课程表
    top_100_timetables = timetables_with_scores[:100]

    timetables_json = json.dumps(top_100_timetables, indent=4)
    with open("top100ttbs.json", "w") as file:
        file.write(timetables_json)


if __name__ == "__main__":
    main()
