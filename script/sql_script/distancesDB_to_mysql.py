import pymysql
import json
import sqlite3

conn_sqlite = sqlite3.connect("walking_distances.db")
conn_mysql = pymysql.connect(
    host="localhost",
    database="uoft_course_info",
    user="root",
    passwd="123456",
    port=3306,
)

cursor_mysql = conn_mysql.cursor()
cursor_sqlite = conn_sqlite.cursor()

cursor_mysql.execute(
    """
    CREATE TABLE IF NOT EXISTS distances (
        id INTEGER PRIMARY KEY AUTO_INCREMENT,
        origin TEXT,
        destination TEXT,
        distance INTEGER, -- distance in meters
        duration INTEGER -- time in minute
    )
"""
)

for i in range(4692):
    sql = "SELECT * FROM distances WHERE id = ?;"
    data = cursor_sqlite.execute(sql, (i + 1,)).fetchone()
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
    sql = "INSERT INTO distances (origin, destination, distance, duration) VALUES (%s, %s, %s, %s)"
    cursor_mysql.execute(sql, (origin, destination, distance, duration))

    print(f"data {i+1} complete")

print("finish!")

conn_mysql.commit()
conn_sqlite.commit()
cursor_sqlite.close()
cursor_mysql.close()
