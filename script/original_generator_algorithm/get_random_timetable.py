import sqlite3
import json
import time
import random
import copy

conn = sqlite3.connect("courses.db")
cur = conn.cursor()


course_code_lst = ["CSC148H1", "CHM136H1", "BIO130H1", "CSC165H1", "MAT136H1"]

source = {}

for i in range(len(course_code_lst)):
    #
    get_course_id = cur.execute(
        "SELECT course_id FROM courses WHERE course_code = ? AND section_code = 'S'",
        (course_code_lst[i],),
    ).fetchall()
    get_info = cur.execute(
        "SELECT section_code, times FROM meeting_sections WHERE course_id = ?",
        get_course_id[0],
    ).fetchall()
    source[i] = (course_code_lst[i], {"LEC": {}, "PRA": {}, "TUT": {}})
    for course_info in get_info:
        class_time = json.loads(course_info[1])
        if len(class_time) != 0:
            source[i][1][course_info[0][:3]][course_info[0]] = class_time

time_available_temp = {}
day_template = {1: False, 2: False, 3: False, 4: False, 5: False, 6: False, 7: False}
day_info_template = {1: {"total_time": 0, "begain":86400000, "end":0}, 2: {"total_time": 0, "begain":86400000, "end":0}, 3: {"total_time": 0, "begain":86400000, "end":0}, 4: {"total_time": 0, "begain":86400000, "end":0}, 5: {"total_time": 0, "begain":86400000, "end":0}, 6: {"total_time": 0, "begain":86400000, "end":0}, 7: {"total_time": 0, "begain":86400000, "end":0}}
for i in range(9, 22):
    if i < 10:
        time_available_temp[f"0{i}:00"] = copy.deepcopy(day_template)
        time_available_temp[f"0{i}:30"] = copy.deepcopy(day_template)
    else:
        time_available_temp[f"{i}:00"] = copy.deepcopy(day_template)
        time_available_temp[f"{i}:30"] = copy.deepcopy(day_template)


def convert_ms_to_time(milliseconds):
    hours = int((milliseconds % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
    minutes = int((milliseconds % (1000 * 60 * 60)) / (1000 * 60))

    # 把时、分、秒都补全成两位数
    if hours < 10:
        hours = str(f"0{hours}")
    if minutes < 10:
        minutes = str(f"0{minutes}")

    # 组合时分秒
    return str(hours) + ":" + str(minutes)


def add_to_time_abailable(time_available, time_need_add):
    for key, value in time_need_add.items():
        for time in value:
            time_available[time][key] = True
        

def add_total_time(need_add_section, total_day_time):
    if len(need_add_section) == 0:
        return
    for one_day in need_add_section[1]:
        day = one_day["day"]
        class_start_time = one_day["start"]
        class_end_time = one_day["end"]
        total_day_time[day]["total_time"] += (class_end_time-class_start_time)
        total_day_time[day]["begain"] = min(total_day_time[day]["begain"], class_start_time)
        total_day_time[day]["end"] = min(total_day_time[day]["end"], class_end_time)

def check_conflict(time_available, need_add_section) -> bool:
    if len(need_add_section) == 0:
        return True
    time_need_add = {}
    for one_day in need_add_section[1]:
        day = one_day["day"]
        time_need_add[day] = []
        class_start_time = one_day["start"]
        class_end_time = one_day["end"]
        
        start_time = convert_ms_to_time(class_start_time)
        if time_available[start_time][day]:
            return False
        
        for i in range(class_start_time + 1800000, class_end_time, 1800000):
            temp_time = convert_ms_to_time(i)
            if temp_time in time_available:
                if time_available[temp_time][day]:
                    return False
                else:
                    time_need_add[day].append(temp_time)
                    
        # if start_time not in time_available:
        #     time_available[start_time] = copy.deepcopy(day_template)
        time_need_add[day].append(start_time)
    add_to_time_abailable(time_available, time_need_add)
    return True


def gen_random_table() -> dict:
    timetable = {"class": []}
    time_available = copy.deepcopy(time_available_temp)
    total_day_time = copy.deepcopy(day_info_template)
    for i in range(len(course_code_lst)):
        course_name = source[i][0]
        section_lec = random.choice(list(source[i][1]["LEC"].items()))
        section_tut = random.choice(list(source[i][1]["TUT"].items())) if len(source[i][1]["TUT"].items()) != 0 else []
        section_pra = random.choice(list(source[i][1]["PRA"].items())) if len(source[i][1]["PRA"].items()) != 0 else []
        
        LIMIT = 10
        
        counter = LIMIT
        can_add_lec = check_conflict(time_available, section_lec)
        while counter > 0 and not can_add_lec:
            section_lec = random.choice(list(source[i][1]["LEC"].items()))
            can_add_lec = check_conflict(time_available, section_lec)
            counter -= 1
        if not can_add_lec:
            return {}
        
        counter = LIMIT
        can_add_tut = check_conflict(time_available, section_tut)
        while counter > 0 and len(section_tut) != 0 and not can_add_tut:
            section_tut = random.choice(list(source[i][1]["TUT"].items())) if len(source[i][1]["TUT"].items()) != 0 else []
            can_add_tut = check_conflict(time_available, section_tut)
            counter -= 1
        if not can_add_tut:
            return {}
        
        counter = LIMIT
        can_add_pra = check_conflict(time_available, section_pra)
        while counter > 0 and len(section_pra) != 0 and not can_add_pra:
            section_pra = random.choice(list(source[i][1]["PRA"].items())) if len(source[i][1]["PRA"].items()) != 0 else []
            can_add_pra = check_conflict(time_available, section_pra)
            counter -= 1
        if not can_add_pra:
            return {}
        
        add_total_time(section_lec,total_day_time)
        add_total_time(section_tut,total_day_time)
        add_total_time(section_pra,total_day_time)
        
        if len(section_tut) != 0 and len(section_pra) != 0:
            timetable["class"].append(
                {"course": course_name, "section": [section_lec, section_tut, section_pra]}
            )
        elif len(section_pra) != 0:
            timetable["class"].append(
                {"course": course_name, "section": [section_lec, section_pra]}
            )
        elif len(section_tut) != 0:
            timetable["class"].append(
                {"course": course_name, "section": [section_lec, section_tut]}
            )
    timetable["day_class_total_time"] = total_day_time
    return timetable

def fitness(timetable):
    score = []

n = 100
gen = []
time_start = time.time()
for i in range(10000):
    temp = gen_random_table()
    # while len(temp) == 0:
    #     # print(i)
    #     temp = gen_random_table()
    if len(temp) != 0 and temp not in gen:
        gen.append(temp["class"])
    # else:
    #     print("ss")
time_end = time.time()
print(time_end-time_start)
print(len(gen))

for i in range(len(gen)):
    with open("./lib/test.json", "w", encoding="utf-8") as f:
        timetable = gen[i]
        output = []
        for each_class in timetable:
            for each_section in each_class["section"]:
                for each_section_time in each_section[1]:
                    c = {
                        "course": each_class["course"],
                        "section": each_section[0],
                        "time": each_section_time,
                    }
                    output.append(c)
        f.write(json.dumps(output))
    time.sleep(1)


# 只换可以换的课
# 如果不能换则保持原样
