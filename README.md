# UofT-Timetable-Builder
## Overview
The UofT Timetable Builder designed to simplify and optimize the creation of academic schedules for students. It allows users to build custom timetables based on course selections and personal preferences, enhancing their educational planning experience. This tool is especially useful for students looking to balance their academic workload with personal commitments and preferences for specific times of day.

## Features
### Course Selection: 
Students can select courses for the Fall and Winter terms from a comprehensive list. The system supports the inclusion of full-year courses, ensuring consistency across terms.

### Preference Customization: 
Users can set their preferences for class start times, intervals between classes, the number of days spent on campus, and reasonable walking times between classes. The system prioritizes these preferences when generating the timetable.

### Timetable Generation: 
With the selected courses and preferences, the system generates multiple timetable options, allowing users to review and choose the one that best fits their needs.

### View Modes: 
The application offers two viewing modes for convenience:

Large View: A detailed view of the timetable with course names, times, and locations.

Small View: A simplified overview of the timetables, displaying only the color-coded courses for quick comparison.

## Technologies
The Timetable Builder utilizes a combination of front-end and back-end technologies:

### Front-end: 
Developed using HTML, CSS, and JavaScript, the front-end is built with an emphasis on usability and aesthetics. The interface includes elements from libraries such as Bootstrap and Selectize.js for enhanced interaction and style.

### Back-end: 
The server-side logic is implemented in Java, handling data processing, timetable generation algorithms(include Backtracking Algorithm and Genetic Algorithm), and integration with the front-end.

## Getting Started
### Prerequisites

```
Java Development Kit (JDK) 17 or higher
```
```
python packages include sqlite3 pymysql
```

## Installation

OS X & Linux:


1. Open a terminal.
2. Clone the repository using Git:
```sh
git clone https://github.com/UofT-Box/UofT-Timetable-Builder.git
```
3. Navigate to the project directory:
```sh
cd UofT-Timetable-Builder
```
4. Ensure that Java(17) is properly installed by running:
```sh
java -version
```
5. Enter target file
```sh
cd target
```
6. run using the 'java -jar' command
```sh
java -jar uofttimetablebuilder-1.1.2-RELEASE.jar
```
7. Go to browser tap:
```sh
http://localhost:8080/
```

Windows:
1. Open Command Prompt or PowerShell.
2. Clone the repository:
```sh
git clone https://github.com/yourusername/UofT-Timetable-Builder.git
```
3. Navigate to the project directory:
```sh
cd UofT-Timetable-Builder
```
4. Verify Java installation:
```
java -version
```
5. Enter target file
```sh
cd target
```
6. run using the 'java -jar' command
```sh
java -jar uofttimetablebuilder-1.1.2-RELEASE.jar
```
7. Go to browser tap:
```sh
http://localhost:8080/
```

### Usage example
Enter the first three letters of the course code in the search bar to display related available courses.
![](https://github.com/UofT-Box/image/blob/671ee8dff6486f3130e1987e6cf46cad3ad5666f/timetablebuilder1.png?raw=true)
You can arrange your own preference by selecting different options and dragging the slider.
![](https://github.com/UofT-Box/image/blob/master/timetablebuilder2.png?raw=true)
Click on "GENERATE SCHEDULE" to generate a schedule.
![](https://github.com/UofT-Box/image/blob/master/timetablebuilder3.png?raw=true)
You can switch between semesters by clicking on "Fall" or "Winter".
![](https://github.com/UofT-Box/image/blob/master/timetablebuilder4.png?raw=true)
You can click on the section of the class schedule to get the section detail information.
![](https://github.com/UofT-Box/image/blob/master/timetablebuilder5.png?raw=true)
Click on "MORE OPTIONS" to enter the thumbnail view.
![](https://github.com/UofT-Box/image/blob/master/timetablebuilder6.png?raw=true)
Select the class schedule you want in the thumbnails
![](https://github.com/UofT-Box/image/blob/master/timetablebuilder7.png?raw=true)

## Deployment

To be continue

## Release History
* 1.1.0
    * mobile adaptation
* 1.0.0
    * Fix all known bugs
    * Official release
* 0.6.0
    * CHANGE: improve user preferences
* 0.5.0
    * CHANGE: change styles
* 0.4.0
    * UPDATE: update color on timetable
* 0.3.0
    * UPDATE: User select Course feature
* 0.2.0
    * CHANGE: Upload Maven Wrapper
* 0.1.0
    * CHANGE: initialization version

## Authors

* **Zhengyu-Joey-Wang**
* **Zheng-August**


## License
This project is protected under the MIT licence. Please refer to the Licence.txt for more information.