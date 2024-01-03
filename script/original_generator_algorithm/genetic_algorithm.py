import sqlite3
import json
import time
import random
import tqdm
from tqdm import tqdm


def connect_to_database(db_path):
    conn = sqlite3.connect(db_path)
    return conn


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


def get_course_schedule(conn, course_code_lst, section_code, courses_example):
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


# 初始化种群
def generate_initial_population(courses, population_size=1):
    population = []
    for _ in range(population_size):
        chromosome = []
        # 染色体格式示例：["CSC108H1-LEC-LEC0101", "CHM136H1-TUT-TUT0602", "CHM136H1-LEC-LEC5101"]
        for course_key in courses:
            for type_key in courses[course_key]:
                sections = list(courses[course_key][type_key].keys())
                selected_section = random.choice(sections)  # 随机选择meeting time
                gene = f"{course_key}-{type_key}-{selected_section}"
                chromosome.append(gene)
        population.append(chromosome)
    return population


def is_conflict(gene1, gene2, courses):
    # 解析
    course_key1, type_key1, section_key1 = gene1.split("-")
    course_key2, type_key2, section_key2 = gene2.split("-")

    # 检查
    for time1 in courses[course_key1][type_key1][section_key1]:
        for time2 in courses[course_key2][type_key2][section_key2]:
            if time1["day"] == time2["day"]:
                if (
                    time1["start"] < time2["start"] < time1["end"]
                    or time2["start"] < time1["start"] < time2["end"]
                    or time1["start"] < time2["end"] < time1["end"]
                    or time2["start"] < time1["end"] < time2["end"]
                ):
                    return True
    return False


def calculate_fitness(chromosome, courses, weights):
    conflict_score = 0
    # 检查课程间的冲突
    for i in range(len(chromosome)):
        for j in range(i + 1, len(chromosome)):
            if is_conflict(chromosome[i], chromosome[j], courses):
                conflict_score -= 1  # 如果冲突则惩罚

    # 其他惩罚逻辑
    daily_course_hours = {day: 0 for day in range(1, 6)}  # 创建一个一周五天的空时间表
    daily_course_count = {day: 0 for day in range(1, 6)}

    for gene in chromosome:
        course_key, type_key, section_key = gene.split("-")
        for time in courses[course_key][type_key][section_key]:
            day = time["day"]
            start = time["start"]
            end = time["end"]
            daily_course_hours[day] += end - start
            daily_course_count[day] += 1

    # 评估密集度
    intensity_score = 0

    for hours in daily_course_hours.values():
        # 检查课时数是否大于 0
        if hours > 0:
            # 如果是，则加分
            intensity_score += 1

    # 评估均匀性
    # 所有非零的课程天数
    non_zero_days = []
    for count in daily_course_count.values():
        if count > 0:
            non_zero_days.append(count)

    # 计算非零天数的平均课程数量
    average_courses = sum(non_zero_days) / len(non_zero_days) if non_zero_days else 0

    # 计算每个非零天数与平均值的差的绝对值，并求和
    uniformity_sum = 0
    for count in non_zero_days:
        difference = abs(count - average_courses)
        uniformity_sum += difference

    uniformity_score = -uniformity_sum

    conflict_score *= weights["conflict"]
    intensity_score *= weights["intensity"]
    uniformity_score *= weights["uniformity"]
    total_score = conflict_score + intensity_score + uniformity_score

    return total_score


def calculate_total_fitness(fitness_scores):
    """计算种群的总适应度"""
    return sum(fitness_scores)


def select_parents(population, fitness_scores):
    """根据fitness来选择双亲的染色体生产下一代"""
    total_fitness = calculate_total_fitness(fitness_scores)

    # 计算每个染色体的选择概率
    selection_probabilities = []

    for fitness in fitness_scores:
        # 根据改个体的fitness来更改随机权重
        probability = fitness / total_fitness
        selection_probabilities.append(probability)

    # 基于fitness比例权重选择父母染色体
    parents = []
    for _ in range(len(population)):
        # 使random会更容易随机到fitness更高的染色体
        parent = random.choices(population, weights=selection_probabilities, k=1)[0]
        # weights = 权重（lst）
        # k = 选择数量
        parents.append(parent)
    return parents


def perform_crossover(selected_parents, crossover_rate=0.7):
    """对第一轮筛选后的父母交叉匹配 产生相同数量的后代"""
    children = []

    # 两两父母染色体进行交叉
    for i in range(0, len(selected_parents), 2):
        # 如果为奇数跳过最后一个
        if i + 1 >= len(selected_parents):
            break

        parent1 = selected_parents[i]
        parent2 = selected_parents[i + 1]
        if random.random() < crossover_rate:
            # 选择一个交叉点
            crossover_point = random.randint(1, len(parent1) - 1)  # 随机整数
            # 交换基因 两个孩子会继承父母全部的基因 只是位置不同
            child1 = parent1[:crossover_point] + parent2[crossover_point:]
            child2 = parent2[:crossover_point] + parent1[crossover_point:]
        else:
            # 如果没有发生交叉，则子代与父母相同
            child1, child2 = parent1, parent2

        # 添加子代到列表
        children.append(child1)
        children.append(child2)

    return children


def perform_mutation(children, courses, mutation_rate=0.05):
    """对所有子代执行变异操作"""
    mutated_children = []
    for child in children:
        for i in range(len(child)):
            if random.random() < mutation_rate:
                # 解析
                course_key, type_key, _ = child[i].split("-")
                # 选择一个新的section
                new_section = random.choice(list(courses[course_key][type_key].keys()))
                # 变异
                child[i] = f"{course_key}-{type_key}-{new_section}"
        mutated_children.append(child)
    return mutated_children


def convert_chromosome_to_schedule(chromosome, courses):
    """将染色体转换为课程表的详细格式"""
    schedule = []
    for gene in chromosome:
        course_key, type_key, section_key = gene.split("-")
        section_info = courses[course_key][type_key][section_key]
        for time in section_info:
            schedule_entry = {
                "course": course_key,
                "section": section_key,
                "time": time,
            }
            schedule.append(schedule_entry)
    return schedule


def conf(chromosome,courses):
    for i in range(len(chromosome)):
        for j in range(i + 1, len(chromosome)):
            if is_conflict(chromosome[i], chromosome[j], courses):
                return False
    return True

def main():
    db_path = "courses.db"
    conn = connect_to_database(db_path)

    course_code_lst = ["CSC148H1", "CHM136H1", "BIO130H1", "CSC165H1", "MAT136H1"]
    course_code_lst.sort()
    section_code = "S"
    courses_example = {}
    get_course_schedule(conn, course_code_lst, section_code, courses_example)

    # 初始化参数：
    population_size = 500  # 初始种群大小
    crossover_rate = 0.5  # 基因交叉率 0 - 1 之间
    mutation_rate_example = 0.5
    max_generations = 20
    weights = {"conflict": 1000, "intensity": 0.5, "uniformity": 0.5}

    population = generate_initial_population(courses_example, population_size)
    # timetables_json = json.dumps(initial_population, indent=4)
    # with open("GA_population.json", "w") as file:
    #     file.write(timetables_json)
    for generation in tqdm(range(max_generations)):
        population_fitness_scores = []
        for chromosome in population:
            fitness_example = calculate_fitness(chromosome, courses_example, weights)
            population_fitness_scores.append(fitness_example)

        # Selection 通过fitness权重获得第一次后代（自然选择）
        selected_parents = select_parents(population, population_fitness_scores)

        # Crossover 根据两两父代的基因创造出数量相同的子代
        children_after_crossover = perform_crossover(selected_parents, crossover_rate)

        # Mutation 随机变异
        mutated_children = perform_mutation(
            children_after_crossover, courses_example, mutation_rate_example
        )

        population = mutated_children

    population_fitness_scores = []
    for chromosome in population:
        fitness_example = calculate_fitness(chromosome, courses_example, weights)
        population_fitness_scores.append(fitness_example)

    best_solution_index = population_fitness_scores.index(
        max(population_fitness_scores)
    )

    best_solution = population[best_solution_index]
    population_fitness_scores.sort()
    
    print(population_fitness_scores[-100:])
    print(best_solution)
    print(conf(best_solution,courses_example))

    ttb = convert_chromosome_to_schedule(best_solution, courses_example)
    timetable_json = json.dumps(ttb, indent=4)
    with open("lib\\test.json", "w") as file:
        file.write(timetable_json)


if __name__ == "__main__":
    main()
