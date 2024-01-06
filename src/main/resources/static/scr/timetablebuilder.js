// var allTimeTable = [[timetableFall,timetableWinter]];

var allTimeTables = {};
var timeTableIndex = 0;
var fallCourseChoose = [];
var winterCourseChoose = [];
var yearCourseChoose = [];
var fallCredit = 0;
var winterCredit = 0;
var lastInput = "";


var $select = $('.relevantCourses').selectize(); // 输入-下滑选择框生成
var selectControl = $select[0].selectize;

$(document).ready(function(){
    dragInit();
})
$(document).ready(function () { 
    document.getElementById('large-view').classList.add('active');
});
var times = [];
$(document).ready(function (){
    for (var i = 9; i <= 21; i++) {
        if (i < 10) {          
            times.push("0" + i + ":00");
            times.push("0" + i + ":30");
        } else {
            times.push("" + i + ":00");
            times.push("" + i + ":30");
        }
    }
});
$(document).ready(function (){
    saveTimetable([[[],[]]]);
    switchTerm("fall",1);
})
$(document).ready(function () { 
    selectControl.on('change', function(){ 
        courseSelectChangeEvent(selectControl); // 将用户选择的可成加入进List和HTML
        switchButton("generate");
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
    }else{ // 年课
        fallCourseChoose.push(courseCode);
        var color = getRandomColor();
        // let color = null;
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
    switchButton("toggle");
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
                switchTerm("fall",1);
            }else{
                switchTerm("winter",1);
            }
            displaySmallVeiw();
        },
        error: function () {
            alert("Error, something went wrong pleace contact admin!");
        }
    });
}

function displaySmallVeiw(){
    let smallVeiw = document.querySelector("#small-view");
    let output = "";
    let len = Object.keys(allTimeTables).length;
    output += `<div class= "row">`;
    for (let i = 1; i <= len; i++){
        if ((i-1) != 0 && (i-1) % 3 == 0){
            output += `</div>`;
            output += `<div class= "row">`;
        }
        output += `<div class="timetable-thumbnail" id="thumbnail-${i}" style="zoom:50%">`;
        output += `<div class="thumbnail-header">Timetable #${i}</div>`;
        output += `<div class="thumbnail-content">`;

        output += `<button class="semester" onclick="switchTerm('fall',${i});switchView()">`;
        output += `<div class="semesterName">Fall</div>`;
        output += `<table class="table table-bordered" id="fallTable${i}"></table>`;
        output += '</button>';
        output += `<button class="semester" onclick="switchTerm('winter',${i});switchView()">`;
        output += `<div class="semesterName">Winter</div>`;
        output += `<table class="table table-bordered" id="winterTable${i}"></table>`;
        output += `</button>`;

        output += `</div>`;
        output += `</div>`;
        
    }
    output += `</div>`;
    smallVeiw.innerHTML = output;
    displaySmallVeiwTimetable();
}

function displaySmallVeiwTimetable (index) {
    let terms = ['fall','winter'];
    let len = Object.keys(allTimeTables).length;
    for (let i = 1; i <= len; i++){
        for (let term of terms){
            let table = document.querySelector("#" + term + "Table" + (i));
            // console.log("#" + term + "Table" + (i));
            let output = "";
            let timetable = allTimeTables[i][term]; //获取课表
            
            for (let time of times) {
                output += `<tr style="height:0.000001px">`;
                for (let j = 1; j <= 5; j++) {
                    if (timetable[time][j] !== ""){
                        let info = timetable[time][j];
                        output += `<td style="background-color: ${info["color"]};"></td>`;
                    }else{
                        output += `<td></td>`
                    }
                }
                output += `</tr>`;
            }
            // 将元素加入至HTML
            table.innerHTML = output;
            // mergeCells(table); // 合并单元格
        }
    }
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
    let index = 1;
    for (let generatedTimetable of allGeneratedTimetable){
        //初始化timetable模板
        let timetableFall = Object.assign({},{});
        let timetableWinter = Object.assign({},{});
        initTimetableTemplat(timetableFall, timetableWinter);
        // 将fall课程元素加入至fall timetable模板
        for (let courseInfo of generatedTimetable[0]) {
            let color = getCourseColor(courseInfo.course, "F"); // 获取课程颜色
            for(let sectionInfo of courseInfo.timeAndPlaceList){
                let day = sectionInfo.day;
                let info = {
                    "course": courseInfo.course,
                    "section": courseInfo.section,
                    "prof": courseInfo.prof,
                    "location": (sectionInfo.location != "" ? sectionInfo.location : "TBA"),
                    "color": color
                }
                for (var i = sectionInfo.start; i < sectionInfo.end; i += 1800000) {
                    let tempTime = convertMillisecondsToHM(i);
                    if(tempTime in timetableFall){
                        timetableFall[tempTime][day] = info;
                    }
                }
            }
        }
        // 将winter课程元素加入至winter timetable模板
        for (let courseInfo of generatedTimetable[1]) {
            let color = getCourseColor(courseInfo.course,"S"); // 获取课程颜色
            for(let sectionInfo of courseInfo.timeAndPlaceList){
                let day = sectionInfo.day;
                let info = {
                    "course": courseInfo.course,
                    "section": courseInfo.section,
                    "prof": courseInfo.prof,
                    "location": (sectionInfo.location != "" ? sectionInfo.location : "TBA"),
                    "color": color
                }
                for (var i = sectionInfo.start; i < sectionInfo.end; i += 1800000) {
                    let tempTime = convertMillisecondsToHM(i);
                    if(tempTime in timetableWinter){
                        timetableWinter[tempTime][day] = info;
                    }
                }
            }
        }
        allTimeTables[index] = {"fall": timetableFall, "winter": timetableWinter};
        index++;
    }
    // displaySmallVeiw();
}

function initTimetableTemplat(timetableFall, timetableWinter){
    // 初始化timetable
    let dayTemplate = {
        1: "",
        2: "",
        3: "",
        4: "",
        5: ""
    };
    // 生成课表模板
    for (var i = 9; i <= 21; i++) {
        if (i < 10) {
            timetableFall["0" + i + ":00"] = Object.assign({}, dayTemplate);
            timetableWinter["0" + i + ":00"] = Object.assign({}, dayTemplate);
            timetableFall["0" + i + ":30"] = Object.assign({}, dayTemplate);
            timetableWinter["0" + i + ":30"] = Object.assign({}, dayTemplate);
        } else {
            timetableFall["" + i + ":00"] = Object.assign({}, dayTemplate);
            timetableWinter["" + i + ":00"] = Object.assign({}, dayTemplate);
            timetableFall["" + i + ":30"] = Object.assign({}, dayTemplate);
            timetableWinter["" + i + ":30"] = Object.assign({}, dayTemplate);
        }
    }
}

// index表的number
function displayTimetable (term, index) {
    // 将元素加入至HTML
    let table = document.querySelector("#timetable-output");
    let output = "";
    let timetable = allTimeTables[index][term]; //获取课表
    
    for (let time of times) {
        let tLen = time.length;
        output += (time[tLen - 2] == 0 ? `<tr><th>${time}</th>` : `<tr><th></th>`);
        for (let j = 1; j <= 5; j++) {
            if (timetable[time][j] !== ""){
                let info = timetable[time][j];
                output += `<td class = "${time}|${term}" id = "${j}" onclick="tdHaveSection(this)" style="background-color: ${info["color"]};">${info["course"]}<br>${info["section"]}</td>`;
            }else{
                output += `<td class = "${time}|${term}"id = "${j}" onclick="tdNoSection(this)"></td>`
            }
        }
        output += `</tr>`;
    }
    table.innerHTML = output;

    mergeCells(table); // 合并单元格
}

function mergeCells(table) {
    let remove = [];
    for(let i = 1; i <= 5; i++){
        let headerCell = null;
        for (let row of table.rows) {
            const firstCell = row.cells[i];
            if (firstCell === null || firstCell.innerText === ""){
                continue;
            }
            if (headerCell === null || firstCell.innerText !== headerCell.innerText) {
                headerCell = firstCell;
            } else {
                headerCell.rowSpan++;
                remove.push(firstCell);
            }
        }
    }
    for (let r of remove){
        r.remove();
    }
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
    let day = event.id; // 获取一周的哪一天
    let className = event.className.split("|"); //获取时间
    let time = className[0];
    let term = className[1];
    let timetable = allTimeTables[1][term];
    let info = timetable[time][day];

    //获取课程详细信息
    let course = info["course"];
    let section = info["section"];
    let location = info["location"];
    let profName = info["prof"];
    let detaillInfo = `<p>Course: ${course}<br>Section: ${section}<br>Location: ${location}</p>`;
    console.log(section);
    if(profName !== "" && section.includes("LEC")){
        if (profName.includes(",")){
            let profNameList = profName.split(',');
            for(let prof of profNameList){
                detaillInfo += `<p>Prof. ${prof}: <a href="https://www.ratemyprofessors.com/search/professors?q=${prof}" target="_blank">Click me to view the professor ratings</a></p>`;
            }
        }else{
            detaillInfo += `<p>Prof. ${profName}: <a href="https://www.ratemyprofessors.com/search/professors?q=${profName}" target="_blank">Click me to view the professor ratings</a></p>`;
        }
    }

    // 将信息添加进弹窗并激活弹窗
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
    toggleBtn.textContent = largeView.classList.contains('active') ? 'MORE OPTION' : 'BACK';
}

function switchButton(name) {
    var generateBtn = document.getElementById('generate-schedule-btn');
    var toggleBTn = document.getElementById('toggle-view-btn');

    // 切换active类来显示或隐藏视图
    if ((name === "toggle" && !toggleBTn.className.includes("active")) ||
        (name === "generate" && !generateBtn.className.includes("active"))){
        generateBtn.classList.toggle('active');
        toggleBTn.classList.toggle('active');
    }
    
    
}
  

function switchTerm(term,index=1) {
    console.log(term);
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
        displayTimetable(term, index);
        document.getElementById('fall-courses').style.display = 'block';
        document.querySelector(`button[onclick="switchTerm(\'fall\')"]`).classList.add('active');
    } else {
        displayTimetable(term, index);
        document.getElementById('winter-courses').style.display = 'block';
        document.querySelector(`button[onclick="switchTerm(\'winter\')"]`).classList.add('active');
    }
}

// 随机颜色
var usedColor = []
function getRandomColor() {
    let color = 360 * Math.random();
    while(true){
        let checkStop = true;
        for(let used of usedColor){
            if(Math.abs(color - used) < 0.5){
                checkStop = false;
                break;
            }
        }
        if(checkStop){
            break;
        }
        color = 360 * Math.random();
    }
    usedColor.push(color);
    return "hsla(" + ~~(color) + "," +"70%," + "80%,1)";
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
    courseItem.id = course;

    // 颜色
    const colorIndicator = document.createElement('div');
    colorIndicator.className = 'color-indicator';
    colorIndicator.setAttribute('data-course-code', course);
    
    colorIndicator.style.backgroundColor = (color != null ? color : getRandomColor()); // 设置随机颜色

    // 名称
    const courseName = document.createElement('span');
    courseName.className = 'course-name';
    courseName.textContent = course.slice(0, -1) + " " + course.slice(-1); // 设置课程名称

    // 编辑按钮
    // const editBtn = document.createElement('button');
    // editBtn.className = 'edit-btn';
    // editBtn.textContent = '✏️';
    // TODO: 添加编辑按钮的事件监听

    // 删除按钮
    const deleteBtn = document.createElement('button');
    deleteBtn.className = 'delete-btn';
    deleteBtn.id = course;
    deleteBtn.textContent = '🗑️';
    deleteBtn.setAttribute("onclick","deleteCourse(this)");
    // TODO: 添加删除按钮的事件监听

    // 将所有元素添加到课程元素中
    courseItem.appendChild(colorIndicator);
    courseItem.appendChild(courseName);
    // courseItem.appendChild(editBtn);
    courseItem.appendChild(deleteBtn);

    coursesDiv.appendChild(courseItem);
}


function deleteCourse(deleteBtn){
    let deleteId = deleteBtn.id;
    
    for (let i = 0; i < fallCourseChoose.length; i++){
        if (fallCourseChoose[i] === deleteId){
            fallCourseChoose.splice(i,1);
            fallCredit -= 0.5;
        }
    }
    for (let i = 0; i < winterCourseChoose.length; i++){
        if (winterCourseChoose[i] === deleteId){
            winterCourseChoose.splice(i,1);
            winterCredit -= 0.5;
        }
    }
    while (true){
        const element = document.getElementById(deleteId); 
        if (element){
            element.remove(); 
        }else{
            break;
        }
    }
    
}

// let dragSrcEl = null;

// function handleDragStart(e) {
//     dragSrcEl = this;
//     e.dataTransfer.effectAllowed = 'move';
//     e.dataTransfer.setData('text/html', this.outerHTML);

//     // 保存当前的选项状态
//     this.storedInputs = [];
//     let inputs = this.querySelectorAll('input[type="radio"]');
//     inputs.forEach(input => {
//         this.storedInputs.push({
//             id: input.id,
//             checked: input.checked
//         });
//     });
// }

// function handleDragOver(e) {
//     //默认不允许放置
//     if (e.preventDefault) {
//         e.preventDefault();
//     }
//     // 设置放置效果
//     e.dataTransfer.dropEffect = 'move';
//     return false;
// }

// function handleDragEnter(e) {
//     // 当某被拖拽的元素进入另一元素时调用
//     this.classList.add('over');
// }

// function handleDragLeave(e) {
//     // 当被拖拽的元素离开某元素时调用
//     this.classList.remove('over');
// }

// function handleDrop(e) {
//     if (e.stopPropagation) {
//         e.stopPropagation(); // 阻止事件冒泡
//     }

//     if (dragSrcEl !== this) {
//         // 执行放置操作
//         this.parentNode.removeChild(dragSrcEl);
//         const dropHTML = e.dataTransfer.getData('text/html');
//         this.insertAdjacentHTML('beforebegin', dropHTML);
//         const droppedElem = this.previousSibling;
//         addDnDHandlers(droppedElem);

//         // 恢复选项状态
//         droppedElem.storedInputs.forEach(inputInfo => {
//             const input = droppedElem.querySelector(`#${inputInfo.id}`);
//             if (input) {
//                 input.checked = inputInfo.checked;
//             }
//         });
//     }

//     return false;
// }

// function handleDragEnd(e) {
//     // 拖拽结束时调用
//     this.classList.remove('over');
//     this.classList.remove('dragging');
// }

// function addDnDHandlers(elem) {
//     elem.addEventListener('dragstart', handleDragStart, false);
//     elem.addEventListener('dragover', handleDragOver, false); // 已定义的其他处理函数
//     elem.addEventListener('drop', handleDrop, false);
//     elem.addEventListener('dragend', handleDragEnd, false); // 已定义的其他处理函数

//     // 存储每个偏好设置的当前选中状态
//     let inputs = elem.querySelectorAll('input[type="radio"]');
//     elem.storedInputs = [];
//     inputs.forEach(input => {
//         elem.storedInputs.push({
//             id: input.id,
//             checked: input.checked
//         });
//     });
// }

// 获取所有偏好设置项
const prefs = document.querySelectorAll('.preference');
prefs.forEach(addDnDHandlers);

function dragInit(){
    let list = document.querySelector('.preference-list');
    let currentLi;
    list.addEventListener('dragstart',(e)=>{
        e.dataTransfer.effectAllowed = 'move';
        currentLi = e.target;
        setTimeout(()=>{
            currentLi.classList.add('moving');
        })
    })

    list.addEventListener('dragenter',(e)=>{
        e.preventDefault();
        if(e.target === currentLi||e.target === list){
            return;
        }
        let liArray = Array.from(list.childNodes);
        let currentIndex = liArray.indexOf(currentLi);
        let targetindex = liArray.indexOf(e.target);

        if(currentIndex<targetindex){

            list.insertBefore(currentLi,e.target.nextElementSibling);
        }else{

            list.insertBefore(currentLi,e.target);
        }
    })
    list.addEventListener('dragover',(e)=>{
        e.preventDefault();
    })
    list.addEventListener('dragend',(e)=>{
        currentLi.classList.remove('moving');
    })
}

dragInit();