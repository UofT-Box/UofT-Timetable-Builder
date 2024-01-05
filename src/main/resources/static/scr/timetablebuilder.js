// var allTimeTable = [[timetableFall,timetableWinter]];
var timetableFall = {};
var isDisplayFall = {};
var timetableWinter = {};
var isDisplayWinter = {};
var allTimeTables = [];
var timeTableIndex = 0;
var fallCourseChoose = [];
var winterCourseChoose = [];
var yearCourseChoose = [];
var fallCredit = 0;
var winterCredit = 0;
var lastInput = "";


var $select = $('.relevantCourses').selectize(); // 输入-下滑选择框生成
var selectControl = $select[0].selectize;
$(document).ready(function () { 
    initTimetableTemplat();
    displayTimetable ("F");
    document.getElementById('large-view').classList.add('active');
});

$(document).ready(function () { 
    selectControl.on('change', function(){ 
        courseSelectChangeEvent(selectControl); // 将用户选择的可成加入进List和HTML
    });
});

$(document).ready(function(){
    // 用户输入后将符合要求的可成加入进下滑选择中
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
    let courseCode = selectControl.getValue(); // 获取用户的选择
    if (courseCode == ""){ //判断用户选择是否为空
        return;
    }

    let sectionCode = getSectionCode(courseCode); // 获取Section code
    if (!canAdd(courseCode, sectionCode)){ // 判断是否可以添加
        return; 
    }
    
    addSelectedCourse(courseCode, sectionCode); // 添加课程
    
    selectControl.clear();
    return;
}   
function addSelectedCourse(courseCode, sectionCode){
    if (sectionCode === 'S'){
        winterCourseChoose.push(courseCode);
        generateCourses(courseCode,"winter");
        switchTerm("winter");
        winterCredit += 0.5;
    }else if(sectionCode === "F"){
        fallCourseChoose.push(courseCode);
        generateCourses(courseCode,"fall");
        switchTerm("fall");
        fallCredit += 0.5;
    }else{
        fallCourseChoose.push(courseCode);
        var color = getRandomColor()
        generateCourses(courseCode,"fall",color);
        winterCourseChoose.push(courseCode);
        generateCourses(courseCode,"winter",color);
        

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
    if (fallCourseChoose.includes(courseCode) || winterCourseChoose.includes(courseCode) || yearCourseChoose.includes(courseCode)){
        alert("You have already added this course");
        return false;
    }
    if (sectionCode === 'Y' && winterCredit >= 3.0 && fallCredit >= 3.0){
        alert("Failed adding the course.\nYou have reached the maximum number of credits you can earn in a both semester");
        return false;
    }else if(sectionCode === 'S' && winterCredit >= 3.0){
        alert("Failed adding the course.\nYou have reached the maximum number of credits you can earn in a Winter semester");
        return false;
    }else if(sectionCode === 'F' && fallCredit >= 3.0){
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
        url: "http://localhost:8080/generateTimetable",
        contentType: "application/json",
        data: JSON.stringify({
            fallCourseList: fallCourseChoose,
            winterCourseList: winterCourseChoose,
            preferenceWeight: []
        }),
        dataType: "json",
        async: false,
        success: function (data){
            //小视角加载

            saveTimetable(data);
            if (fallCourseChoose.length != 0 || winterCourseChoose.length == 0){
                switchTerm("fall");
                displayTimetable("F");
            }else{
                switchTerm("winter");
                displayTimetable("W");
            }
        },
        error: function () {
            alert("Error, something went wrong pleace contact admin!");
        }
    });
}

function getCourseColor(courseCode, session) {
    // 获取特定学期的颜色指示器
    var colorIndicator = document.querySelector('.color-indicator[data-course-code="' + courseCode + session + '"]');
    // 如果没有找到，尝试获取年课的颜色指示器
    if (!colorIndicator) {
        colorIndicator = document.querySelector('.color-indicator[data-course-code="' + courseCode + 'Y"]');
    }   
    // 如果找到了元素，返回它的背景颜色，否则返回白色
    return colorIndicator ? colorIndicator.style.backgroundColor : '#FFFFFF';
}

function saveTimetable(allGeneratedTimetable){
    //初始化timetable模板
    initTimetableTemplat();
    var index = 0
    for (let generatedTimetable of allGeneratedTimetable){
        allTimeTables.push()
        index ++;
        console.log(generatedTimetable[0]);
        console.log(generatedTimetable[1]);
        // 将fall课程元素加入至fall timetable模板
        for (let courseInfo of generatedTimetable[0]) {
            let color = getCourseColor(courseInfo.course, "F"); // 获取课程颜色
            for(let sectionInfo of courseInfo.timeAndPlaceList){
                let day = sectionInfo.day;
                let timeStart = convertMillisecondsToHM(sectionInfo.start);
                let counter = 1;
                let lastTime = null;
                for (var i = sectionInfo.start; i < sectionInfo.end; i += 1800000) {
                    let tempTime = convertMillisecondsToHM(i);
                    if ((tempTime != timeStart) && (tempTime in timetableFall) && (tempTime != lastTime)) {
                        isDisplayFall[tempTime][day] = false;
                        lastTime = tempTime;
                        counter++;
                    }
                }
                let location = sectionInfo.location;
                let info = `Course: ${courseInfo.course}<br>Section: ${courseInfo.section}<br>Location: ${(location != "" ? location : "TBA")}`;
                timetableFall[timeStart][day] = `<td id="${info}" rowspan="${counter}" onclick="tdHaveSection(this)" style="background-color: ${color};">${courseInfo.course}<br>${courseInfo.section}</td>`;
            }
        }
        // 将winter课程元素加入至winter timetable模板
        for (let courseInfo of generatedTimetable[1]) {
            let color = getCourseColor(courseInfo.course,"S"); // 获取课程颜色
            for(let sectionInfo of courseInfo.timeAndPlaceList){
                let day = sectionInfo.day;
                let timeStart = convertMillisecondsToHM(sectionInfo.start);
                let counter = 1;
                let lastTime = null;
                for (var i = sectionInfo.start; i < sectionInfo.end; i += 1800000) {
                    let tempTime = convertMillisecondsToHM(i);
                    if ((tempTime != timeStart) && (tempTime in timetableWinter) && (tempTime != lastTime)) {
                        isDisplayWinter[tempTime][day] = false;
                        lastTime = tempTime;
                        counter++;
                    }
                }
                let location = sectionInfo.location;
                let info = `Course: ${courseInfo.course}<br>Section: ${courseInfo.section}<br>Location: ${(location != "" ? location : "TBA")}`;
                timetableWinter[timeStart][day] = `<td id="${info}" rowspan="${counter}" onclick="tdHaveSection(this)" style="background-color: ${color};">${courseInfo.course}<br>${courseInfo.section}</td>`;
            }
        }
        console.log(timetableFall);
        console.log(timetableWinter);
    }

}

function initTimetableTemplat(){
    // 初始化timetable
    let dayTemplate = {
        1: true,
        2: true,
        3: true,
        4: true,
        5: true
    };
    // 生成课表模板
    for (var i = 9; i <= 21; i++) {
        if (i < 10) {
            timetableFall["0" + i + ":00"] = Object.assign({}, dayTemplate);
            isDisplayFall["0" + i + ":00"] = Object.assign({}, dayTemplate);
            timetableWinter["0" + i + ":00"] = Object.assign({}, dayTemplate);
            isDisplayWinter["0" + i + ":00"] = Object.assign({}, dayTemplate);

            timetableFall["0" + i + ":30"] = Object.assign({}, dayTemplate);
            isDisplayFall["0" + i + ":30"] = Object.assign({}, dayTemplate);
            timetableWinter["0" + i + ":30"] = Object.assign({}, dayTemplate);
            isDisplayWinter["0" + i + ":30"] = Object.assign({}, dayTemplate);
        } else {
            timetableFall["" + i + ":00"] = Object.assign({}, dayTemplate);
            isDisplayFall["" + i + ":00"] = Object.assign({}, dayTemplate);
            timetableWinter["" + i + ":00"] = Object.assign({}, dayTemplate);
            isDisplayWinter["" + i + ":00"] = Object.assign({}, dayTemplate);

            timetableFall["" + i + ":30"] = Object.assign({}, dayTemplate);
            isDisplayFall["" + i + ":30"] = Object.assign({}, dayTemplate);
            timetableWinter["" + i + ":30"] = Object.assign({}, dayTemplate);
            isDisplayWinter["" + i + ":30"] = Object.assign({}, dayTemplate);
        }
    }
}

function displayTimetable (term) {
    // 将元素加入至HTML
    let p = document.querySelector("#timetable-output");
    let output = "";
    let times = (term === "F" ? Object.keys(timetableFall).sort() : Object.keys(timetableWinter).sort());
    for (let time of times) {
        let tLen = time.length;
        output += (time[tLen - 2] == 0 ? `<tr><th>${time}</th>` : `<tr><th></th>`);
        for (let j = 1; j <= 5; j++) {
            //判断现在用户选择的是否为Fall的课表，否则输出Winter的课表
            if(term === "F"){
                if (isDisplayFall[time][j] == true) {
                    if (timetableFall[time][j] != true) {
                        output += timetableFall[time][j];
    
                    } else {
                        output += `<td onclick="tdNoSection(this)"></td>`;
                    }
                }
            }else{
                if (isDisplayWinter[time][j] == true) {
                    if (timetableWinter[time][j] != true) {
                        output += timetableWinter[time][j];
    
                    } else {
                        output += `<td onclick="tdNoSection(this)"></td>`;
                    }
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

// 展示具体课程信息
function tdHaveSection(event){
    let element = document.querySelector("#courseDetaillInfo")
    let detaillInfo = event.id + "<br>";
    let proofName = "";
    detaillInfo += `<a href="https://www.ratemyprofessors.com/search/professors?q=${proofName}">Click me to view the professor ratings</a>`;
    element.innerHTML = detaillInfo;
    document.querySelector("#displayDetaillCourseInfo").click();
}

// BLOCK Time Section function
function tdNoSection(event){
    
}

function switchView() {
    var largeView = document.getElementById('large-view');
    var smallView = document.getElementById('small-view');

    // 切换active类来显示或隐藏视图
    largeView.classList.toggle('active');
    smallView.classList.toggle('active');

    // 更新切换视图按钮的文本
    var toggleBtn = document.getElementById('toggle-view-btn');
    toggleBtn.textContent = largeView.classList.contains('active') ? '切换到小视图' : '切换到大视图';
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
        displayTimetable("F");
        document.getElementById('fall-courses').style.display = 'block';
        document.querySelector('button[onclick="switchTerm(\'fall\')"]').classList.add('active');
    } else {
        displayTimetable("W");
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
function generateCourses(course,term,color=null) {
    var continerId = 'winter-courses';
    if (term === "fall"){
        continerId = 'fall-courses';
    }
    const coursesDiv = document.getElementById(continerId);

    const courseItem = document.createElement('div');
    courseItem.className = 'course-item';

    // 颜色
    const colorIndicator = document.createElement('div');
    colorIndicator.className = 'color-indicator';
    colorIndicator.setAttribute('data-course-code', course);
    colorIndicator.style.backgroundColor = color!=null ? color:getRandomColor(); // 设置随机颜色

    // 名称
    const courseName = document.createElement('span');
    courseName.className = 'course-name';
    courseName.textContent = course.slice(0, -1) + " " + course.slice(-1); // 设置课程名称

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