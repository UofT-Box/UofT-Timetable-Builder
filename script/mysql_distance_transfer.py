import pymysql
import sqlite3

conn_sqlite = sqlite3.connect("walking_distances.db")
# conn_mysql = pymysql.connect(
#     host = 'localhost', 
#     database = 'uoft_course_info', 
#     user = 'root', 
#     passwd = '123456',
#     port = 3306
# )
conn_main = sqlite3.connect('uoft_course_info.db')

cursor_main = conn_main.cursor()
cursor_walking_dis = conn_sqlite.cursor()

cursor_main.execute(
    """
    CREATE TABLE IF NOT EXISTS distances (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        origin TEXT,
        destination TEXT,
        distance INTEGER, -- distance in meters
        duration INTEGER -- time in minute
    )
"""
)

for i in range(4692):
    sql = "SELECT * FROM distances WHERE id = ?;"
    data = cursor_walking_dis.execute(sql, (i + 1,)).fetchone()
    origin = data[1]
    destination = data[2]
    distance, dis_unit = str(data[3]).split(" ")
    duration, dur_unit = str(data[4]).split(" ")
    if dis_unit == "km":
        distance = float(distance) * 1000
    if dur_unit == "km":
        duration = float(duration) * 1000

    distance = int(distance)
    duration = int(duration)
    # sql = "INSERT INTO distances (origin, destination, distance, duration) VALUES (%s, %s, %s, %s)"
    sql = "INSERT INTO distances (origin, destination, distance, duration) VALUES (?, ?, ?, ?)"
    cursor_main.execute(sql, (origin, destination, distance, duration))

    print(f"data {i+1} complete")

print("finish!")

conn_main.commit()
conn_sqlite.commit()
cursor_walking_dis.close()
cursor_main.close()
