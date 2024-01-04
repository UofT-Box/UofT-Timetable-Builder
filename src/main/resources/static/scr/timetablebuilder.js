var fallCourseChoose = [];
var winterCourseChoose = [];
var yearCourseChoose = [];
var fallCredit = 0;
var winterCredit = 0;
var lastInput = "";

var $select = $('.relevantCourses').selectize();
var selectControl = $select[0].selectize;
$(document).ready(function () {
    selectControl.on('change', function(){
        courseSelectChangeEvent(selectControl);
    });
});

$(document).ready(function(){
    $('#select-state-selectized').on('input', function () {
        let input = document.querySelector("#select-state-selectized").value;
        if(input.length == 3 && input != lastInput){
            selectControl.clearOptions();
            selectControl.refreshOptions();
            addCourseToSelect(input);
            lastInput = input;
        }
    });
})
function courseSelectChangeEvent(selectControl){
    let courseCode = selectControl.getValue();
    if (courseCode == ""){
        return;
    }

    let sectionCode = getSectionCode(courseCode);
    if (!canAdd(courseCode, sectionCode)){
        return; 
    }
    
    addSelectedCourse(courseCode, sectionCode);
    
    selectControl.clear();
    return;
}   
function addSelectedCourse(courseCode, sectionCode){
    if (sectionCode == 'S'){
        winterCourseChoose.push(courseCode);
        // console.log(winterCourseChoose);
        generateCourses("winter","S");
        switchTerm("winter");
        winterCredit += 0.5;
    }else if(sectionCode == "F"){
        fallCourseChoose.push(courseCode);
        generateCourses("fall","F");
        switchTerm("fall");
        fallCredit += 0.5;
    }else{
        winterCourseChoose.push(courseCode);
        generateCourses("winter","Y");
        fallCourseChoose.push(courseCode);
        generateCourses("fall","Y");

        fallCredit += 0.5;
        winterCredit += 0.5;
    }
}
function getSectionCode(courseCode){
    let sectionCodeSubString = courseCode.substring(courseCode.length-3);
    let sectionCode;
    if (sectionCodeSubString.includes("F")){
        sectionCode = 'F';
    }else if (sectionCodeSubString.includes("S")){
        sectionCode = 'S';
    }else{
        sectionCode = 'Y';
    }
    return sectionCode;
}
function canAdd(courseCode , sectionCode){
    console.log(winterCredit);
    if (fallCourseChoose.includes(courseCode) || winterCourseChoose.includes(courseCode) || yearCourseChoose.includes(courseCode)){
        alert("You have already added this course");
        return false;
    }
    if (sectionCode == 'Y' && winterCredit >= 3.0 && fallCredit >= 3.0){
        alert("Failed adding the course.\nYou have reached the maximum number of credits you can earn in a both semester");
        return false;
    }else if(sectionCode == 'S' && winterCredit >= 3.0){
        alert("Failed adding the course.\nYou have reached the maximum number of credits you can earn in a Winter semester");
        return false;
    }else if(sectionCode == 'F' && fallCredit >= 3.0){
        alert("Failed adding the course.\nYou have reached the maximum number of credits you can earn in a Fall semester");
        return false;
    }
    return true;
}
function addCourseToSelect(input) {
    $.ajax({
        type: "post",
        url: "http://localhost:8080/course-input",
        data: {
            courseInput: input
        },
        dataType: "json",
        async: false,
        success: function (data) {
            for (let course of data) {
                let courseCode = course["courseCode"]
                let division = getDivision(courseCode);
                let sectionCode = course["sectionCode"];
                let name = course["name"]
                let info = `${courseCode} ${sectionCode}: ${name} (${division})`;
                selectControl.addOption({ value: `${courseCode}${sectionCode}`, text: info });
            }
            selectControl.refreshOptions();
        },
        error: function () {
            alert("Error, something went wrong pleace contact admin!");
        }
    });
}
function getDivision(courseCode) {
    let len = courseCode.length;
    switch (courseCode[len - 1]) {
        case '1':
            return "UTSG";
        case '3':
            return "UTSC";
        case '5':
            return "UTM";
        default:
            return null;
    }
}

function addCourseToTimetable() {
    $.ajax({
        type: "post",
        url: "http://localhost:8080/test/generateTimetable",
        contentType: "application/json",
        data: JSON.stringify({
            courseList: winterCourseChoose,
            sectionCode: "S"
        }),
        dataType: "json",
        async: false,
        success: function (data){
            displayTimetable(data);
        },
        error: function () {
            alert("Error, something went wrong pleace contact admin!");
        }
    });
}

function displayTimetable (generatedTimetable) {
    var timetable = {};
    var isDisplay = {};
    let dayTemplate = {
        1: true,
        2: true,
        3: true,
        4: true,
        5: true
    };
    for (var i = 9; i <= 21; i++) {
        if (i < 10) {
            timetable["0" + i + ":00"] = Object.assign({}, dayTemplate);
            isDisplay["0" + i + ":00"] = Object.assign({}, dayTemplate);

            timetable["0" + i + ":30"] = Object.assign({}, dayTemplate);
            isDisplay["0" + i + ":30"] = Object.assign({}, dayTemplate);
        } else {
            timetable["" + i + ":00"] = Object.assign({}, dayTemplate);
            isDisplay["" + i + ":00"] = Object.assign({}, dayTemplate);

            timetable["" + i + ":30"] = Object.assign({}, dayTemplate);
            isDisplay["" + i + ":30"] = Object.assign({}, dayTemplate);
        }
    }

    // console.log(timetable);

    for (let courseInfo of generatedTimetable) {
        for(let sectionInfo of courseInfo.timeAndPlaceList){
            let day = sectionInfo.day;
            let timeStart = convertMillisecondsToHM(sectionInfo.start);
            let counter = 1;
            let lastTime = null;
            for (var i = sectionInfo.start; i < sectionInfo.end; i += 1800000) {
                let tempTime = convertMillisecondsToHM(i);
                if ((tempTime != timeStart) && (tempTime in timetable) && (tempTime != lastTime)) {
                    isDisplay[tempTime][day] = false;
                    lastTime = tempTime;
                    counter++;
                }
            }
            let location = sectionInfo.location;
            let info = `Course: ${courseInfo.course}<br>Section: ${courseInfo.section}<br>Location: ${(location != "" ? location : "TBA")}`;
            timetable[timeStart][day] = `<td id="${info}" rowspan="${counter}" onclick="tdHaveSection(this)">${courseInfo.course}<br>${courseInfo.section}</td>`;
        }
    }
    let p = document.querySelector("#output");
    let output = "";
    let times = Object.keys(timetable).sort();
    for (let time of times) {
        let tLen = time.length;
        output += (time[tLen - 2] == 0 ? `<tr><th>${time}</th>` : `<tr><th></th>`);
        for (let j = 1; j <= 5; j++) {
            if (isDisplay[time][j] == true) {
                if (timetable[time][j] != true) {
                    output += timetable[time][j];

                } else {
                    output += `<td onclick="tdNoSection(this)"></td>`;
                }
            }
        }
        output += `</tr>`;
    }
    p.innerHTML = output;
}

function convertMillisecondsToHM(milliseconds) {
    var hours = parseInt((milliseconds % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    var minutes = parseInt((milliseconds % (1000 * 60 * 60)) / (1000 * 60));

    // 把时、分、秒都补全成两位数
    if (hours < 10) {
        hours = "0" + hours;
    }
    if (minutes < 10) {
        minutes = "0" + minutes;
    }

    // 组合时分秒
    return hours + ":" + minutes;
}

function tdHaveSection(event){
    let element = document.querySelector("#courseDetaillInfo")
    let detaillInfo = event.id + "<br>";
    let proofName = "";
    detaillInfo += `<a href="https://www.ratemyprofessors.com/search/professors?q=${proofName}">Click me to view the professor ratings</a>`;
    element.innerHTML = detaillInfo;
    document.querySelector("#displayDetaillCourseInfo").click();
}

function tdNoSection(event){
    
}

function switchTerm(term) {
    // 获取所有的 term 按钮和课程列表
    var termButtons = document.querySelectorAll('.term-btn');
    var coursesSections = document.querySelectorAll('.courses-section-content');

    // 移除所有按钮的 active 类，并隐藏所有课程列表
    termButtons.forEach(function(btn) {
        btn.classList.remove('active');
    });
    coursesSections.forEach(function(section) {
        section.style.display = 'none';
    });

    // 根据选择的学期显示课程列表，并激活相应的按钮
    if(term === 'fall') {
        document.getElementById('fall-courses').style.display = 'block';
        document.querySelector('button[onclick="switchTerm(\'fall\')"]').classList.add('active');
    } else {
        document.getElementById('winter-courses').style.display = 'block';
        document.querySelector('button[onclick="switchTerm(\'winter\')"]').classList.add('active');
    }
}

// 随机颜色
function getRandomColor() {
    const letters = '0123456789ABCDEF';
    let color = '#';
    for (let i = 0; i < 6; i++) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}

// 生成课程元素
function generateCourses(term) {
    var CourseChoose;
    var continerId = 'winter-courses';
    if (term === "fall"){
        CourseChoose = fallCourseChoose;
        continerId = 'fall-courses';
    }else{
        CourseChoose = winterCourseChoose;
    }
    const coursesDiv = document.getElementById(continerId);
    coursesDiv.innerHTML = ''; // 清空现有的课程内容

    CourseChoose.forEach(course => {
        const courseItem = document.createElement('div');
        courseItem.className = 'course-item';

        // 颜色
        const colorIndicator = document.createElement('div');
        colorIndicator.className = 'color-indicator';
        colorIndicator.style.backgroundColor = getRandomColor(); // 设置随机颜色

        // 名称
        const courseName = document.createElement('span');
        courseName.className = 'course-name';
        courseName.textContent = course; // 设置课程名称

        // 编辑按钮
        const editBtn = document.createElement('button');
        editBtn.className = 'edit-btn';
        editBtn.textContent = '✏️';
        // TODO: 添加编辑按钮的事件监听

        // 删除按钮
        const deleteBtn = document.createElement('button');
        deleteBtn.className = 'delete-btn';
        deleteBtn.textContent = '🗑️';
        // TODO: 添加删除按钮的事件监听

        // 将所有元素添加到课程元素中
        courseItem.appendChild(colorIndicator);
        courseItem.appendChild(courseName);
        courseItem.appendChild(editBtn);
        courseItem.appendChild(deleteBtn);

        coursesDiv.appendChild(courseItem);
    });
}


let dragSrcEl = null;

function handleDragStart(e) {
    dragSrcEl = this;
    e.dataTransfer.effectAllowed = 'move';
    e.dataTransfer.setData('text/html', this.outerHTML);

    // 保存当前的选项状态
    this.storedInputs = [];
    let inputs = this.querySelectorAll('input[type="radio"]');
    inputs.forEach(input => {
        this.storedInputs.push({
            id: input.id,
            checked: input.checked
        });
    });
}

function handleDragOver(e) {
    //默认不允许放置
    if (e.preventDefault) {
        e.preventDefault();
    }
    // 设置放置效果
    e.dataTransfer.dropEffect = 'move';
    return false;
}

function handleDragEnter(e) {
    // 当某被拖拽的元素进入另一元素时调用
    this.classList.add('over');
}

function handleDragLeave(e) {
    // 当被拖拽的元素离开某元素时调用
    this.classList.remove('over');
}

function handleDrop(e) {
    if (e.stopPropagation) {
        e.stopPropagation(); // 阻止事件冒泡
    }

    if (dragSrcEl !== this) {
        // 执行放置操作
        this.parentNode.removeChild(dragSrcEl);
        const dropHTML = e.dataTransfer.getData('text/html');
        this.insertAdjacentHTML('beforebegin', dropHTML);
        const droppedElem = this.previousSibling;
        addDnDHandlers(droppedElem);

        // 恢复选项状态
        droppedElem.storedInputs.forEach(inputInfo => {
            const input = droppedElem.querySelector(`#${inputInfo.id}`);
            if (input) {
                input.checked = inputInfo.checked;
            }
        });
    }

    return false;
}

function handleDragEnd(e) {
    // 拖拽结束时调用
    this.classList.remove('over');
    this.classList.remove('dragging');
}

function addDnDHandlers(elem) {
    elem.addEventListener('dragstart', handleDragStart, false);
    elem.addEventListener('dragover', handleDragOver, false); // 已定义的其他处理函数
    elem.addEventListener('drop', handleDrop, false);
    elem.addEventListener('dragend', handleDragEnd, false); // 已定义的其他处理函数

    // 存储每个偏好设置的当前选中状态
    let inputs = elem.querySelectorAll('input[type="radio"]');
    elem.storedInputs = [];
    inputs.forEach(input => {
        elem.storedInputs.push({
            id: input.id,
            checked: input.checked
        });
    });
}

// 获取所有偏好设置项
const prefs = document.querySelectorAll('.preference');
prefs.forEach(addDnDHandlers);