var fallCourseChoose = [];
var winterCourseChoose = [];
var yearCourseChoose = [];
var fallCredit = 0;
var winterCredit = 0;

$(document).ready(function () {
    let $select = $('.relevantCourses').selectize();
    let selectControl = $select[0].selectize;
    selectControl.on('change', function(){
        courseSelectChangeEvent(selectControl);
    });
    
    $('#select-state-selectized').one('input', function () {
        setTimeout(() => addCourseToSelect(), 1000);
    });
});
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
        let element = document.querySelector("#course-list-winter");
        element.innerHTML += `<li>${courseCode}</li>`;
        winterCourseChoose.push(courseCode);
        winterCredit += 0.5;
    }else if(sectionCode == "F"){
        
        let element = document.querySelector("#course-list-fall");
        element.innerHTML += `<li>${courseCode}</li>`;
        winterCourseChoose.push(courseCode);
        fallCredit += 0.5;
    }else{
        let element = document.querySelector("#course-list-fall");
        element.innerHTML += `<li>${courseCode}</li>`;
        
        element = document.querySelector("#course-list-winter");
        element.innerHTML += `<li>${courseCode}</li>`;
        winterCourseChoose.push(courseCode);
        fallCourseChoose.push(courseCode);

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
function addCourseToSelect() {
    $.ajax({
        type: "post",
        url: "http://localhost:8080/course-input",
        data: {
            courseInput: ""
        },
        dataType: "json",
        async: false,
        success: function (data) {
            var $select = $('#select-state').selectize();
            var selectize = $select[0].selectize;
            for (let course of data) {
                let courseCode = course["courseCode"]
                let division = getDivision(courseCode);
                let sectionCode = course["sectionCode"];
                let name = course["name"]
                let info = `${courseCode} ${sectionCode}: ${name} (${division})`;
                selectize.addOption({ value: `${courseCode}${sectionCode}`, text: info });
            }
            selectize.refreshOptions();
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
fetch("./lib/test.json").then(function (response) {

    return response.json();

}).then(function (products) {
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

    for (let product of products) {
        let day = product.time.day;
        let timeStart = convertMillisecondsToHM(product.time.start);
        let timeEnd = convertMillisecondsToHM(product.time.end);
        let counter = 1;
        let lastTime = null;
        for (var i = product.time.start; i < product.time.end; i += 1800000) {
            let tempTime = convertMillisecondsToHM(i);
            if ((tempTime != timeStart) && (tempTime in timetable) && (tempTime != lastTime)) {
                isDisplay[tempTime][day] = false;
                lastTime = tempTime;
                counter++;
            }
        }
        timetable[timeStart][day] = `<td rowspan="${counter}" onclick="tdClicked(this)">${product.course}<br>${product.section}</td>`;
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
                    output += `<td onclick="tdClicked(this)"></td>`;
                }
            }
        }
        output += `</tr>`;
    }
    p.innerHTML = output;
})

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

function tdClicked(event){
    console.log(event.innerText);
}