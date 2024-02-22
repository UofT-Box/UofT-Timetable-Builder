//******* Global Variables and Initializations *************//
var allTimeTables = {};
var timeTableIndex = 0;
var fallCourseChoose = [];
var winterCourseChoose = [];
var yearCourseChoose = [];
var fallCredit = 0.0;
var winterCredit = 0.0;
var lastInput = "";
var fallTotalTimetableSize = 0;
var winetTotalTimetableSize = 0;
var lockedCoursesFall = [];
var lockedCoursesWinter = [];
var building1 = "";
var building2 = "";
var building_list = {};

//************ UI Element Initialization ****************//
var $select = $(".relevantCourses").selectize(); // 输入-下滑选择框生成
var selectControl = $select[0].selectize;

$(document).ready(function () {
  fetch("./lib/locations.json")
    .then((response) => {
      return response.json();
    })
    .then((data) => {
      console.log(data);
      building_list = data;
    });
});

$(document).ready(function () {
  dragInit();
  dragInitMobile();
});
$(document).ready(function () {
  document.getElementById("large-view").classList.add("active");
});

var times = [];
$(document).ready(function () {
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
$(document).ready(function () {
  saveTimetable();
  switchTerm("fall", 1);
});
$(document).ready(function () {
  selectControl.on("change", function () {
    courseSelectChangeEvent(selectControl); // 将用户选择的可成加入进List和HTML
    switchButton("generate");
  });
});

$(document).ready(function () {
  // 用户输入后将符合要求的可成加入进下滑选择中
  $("#select-state-selectized").on("input", function () {
    let input = document.querySelector("#select-state-selectized").value;
    if (input.length == 3 && input != lastInput) {
      selectControl.clearOptions();
      selectControl.refreshOptions();
      addCourseToSelect(input);
      lastInput = input;
    }
  });
});
$(document).ready(function () {
  checkboxMonitor();
});

//******* Event Bindings and Handlers *************//
function courseSelectChangeEvent(selectControl) {
  let courseCode = selectControl.getValue(); // 获取用户的选择
  if (courseCode == "") {
    //判断用户选择是否为空
    return;
  }

  let sectionCode = getSectionCode(courseCode); // 获取Section code
  if (!canAdd(courseCode, sectionCode)) {
    // 判断是否可以添加
    return;
  }

  addSelectedCourse(courseCode, sectionCode); // 添加课程

  selectControl.clear();
  return;
}
function addSelectedCourse(courseCode, sectionCode) {
  if (sectionCode === "S") {
    winterCourseChoose.push(courseCode);
    generateCourses(courseCode, "winter");
    winterCredit += 0.5;
    switchTerm("winter");
  } else if (sectionCode === "F") {
    fallCourseChoose.push(courseCode);
    generateCourses(courseCode, "fall");
    fallCredit += 0.5;
    switchTerm("fall");
  } else {
    // 年课
    fallCourseChoose.push(courseCode);
    var color = getRandomColor();
    // let color = null;
    generateCourses(courseCode, "fall", color);
    winterCourseChoose.push(courseCode);
    generateCourses(courseCode, "winter", color);
    fallCredit += 0.5;
    winterCredit += 0.5;
    switchTerm("fall");
  }
}
function getSectionCode(courseCode) {
  let sectionCodeSubString = courseCode.substring(courseCode.length - 3);
  let sectionCode;
  if (sectionCodeSubString.includes("F")) {
    sectionCode = "F";
  } else if (sectionCodeSubString.includes("S")) {
    sectionCode = "S";
  } else {
    sectionCode = "Y";
  }
  return sectionCode;
}
function canAdd(courseCode, sectionCode) {
  if (
    fallCourseChoose.includes(courseCode) ||
    winterCourseChoose.includes(courseCode) ||
    yearCourseChoose.includes(courseCode)
  ) {
    alert("You have already added this course");
    return false;
  }
  if (sectionCode === "Y" && winterCredit >= 3.0 && fallCredit >= 3.0) {
    alert(
      "Failed adding the course.\nYou have reached the maximum number of credits you can earn in a both semester"
    );
    return false;
  } else if (sectionCode === "S" && winterCredit >= 3.0) {
    alert(
      "Failed adding the course.\nYou have reached the maximum number of credits you can earn in a Winter semester"
    );
    return false;
  } else if (sectionCode === "F" && fallCredit >= 3.0) {
    alert(
      "Failed adding the course.\nYou have reached the maximum number of credits you can earn in a Fall semester"
    );
    return false;
  }
  return true;
}
function addCourseToSelect(input) {
  let origin = window.location.origin;
  $.ajax({
    type: "post",
    url: `${origin}/course-input`,
    data: {
      courseInput: input,
    },
    dataType: "json",
    async: false,
    success: function (data) {
      for (let course of data) {
        let courseCode = course["courseCode"];
        let division = getDivision(courseCode);
        let sectionCode = course["sectionCode"];
        let name = course["name"];
        let info = `${courseCode} ${sectionCode}: ${name} (${division})`;
        selectControl.addOption({
          value: `${courseCode}${sectionCode}`,
          text: info,
        });
      }
      selectControl.refreshOptions();
    },
    error: function () {
      alert("Error, something went wrong pleace contact admin!");
    },
  });
}
function getDistance() {
  let origin = window.location.origin;
  $.ajax({
    type: "post",
    url: `${origin}/get-distance`,
    data: {
      origin: building1,
      destination: building2,
    },
    dataType: "json",
    async: false,
    success: function (data) {
      if (data == -1) {
        // 查看是否找到
        return;
      }
      let distance = data; // 距离
    },
    error: function () {
      alert("Error, something went wrong pleace contact admin!");
    },
  });
}
function getDivision(courseCode) {
  let len = courseCode.length;
  switch (courseCode[len - 1]) {
    case "1":
      return "UTSG";
    case "3":
      return "UTSC";
    case "5":
      return "UTM";
    default:
      return null;
  }
}

function addCourseToTimetable() {
  switchButton("toggle");
  let weigthDict = getWeight();

  let timeWeight = weigthDict["timeWeight"];
  let daySpend = weigthDict["daySpend"] * Number(getRadioInput("days-spent"));
  let classInterval =
    weigthDict["classInterval"] * Number(getRadioInput("class-interval"));
  let origin = window.location.origin;
  $.ajax({
    type: "post",
    url: `${origin}/generateTimetable`,
    contentType: "application/json",
    data: JSON.stringify({
      fallCourseList: fallCourseChoose,
      winterCourseList: winterCourseChoose,
      preferredTimeIndex: Number(getRadioInput("start-time")),
      preferredTimeWeight: timeWeight,
      balanceWeight: daySpend,
      breakTimeWeight: classInterval,
      lockedCoursesFall: lockedCoursesFall,
      lockedCoursesWinter: lockedCoursesWinter,
    }),
    dataType: "json",
    async: false,
    success: function (data) {
      //小视角加载
      saveTimetable(data);
      if (fallCourseChoose.length != 0 || winterCourseChoose.length == 0) {
        switchTerm("fall", 1);
      } else {
        switchTerm("winter", 1);
      }
      displaySmallVeiw();
      hideLoading();
      let generateBtn = document.querySelector("#generate-schedule-btn");
      generateBtn.disabled = false;
    },
    error: function () {
      alert("Error, something went wrong pleace contact admin!");
    },
  });
}

function displaySmallVeiw() {
  let smallVeiw = document.querySelector("#small-view");
  smallVeiw.innerHTML = "";
  let output = "";
  let len = Object.keys(allTimeTables).length;
  output += `<div class= "row">`;
  for (let i = 1; i <= len; i++) {
    if (i - 1 != 0 && (i - 1) % 3 == 0) {
      output += `</div>`;
      output += `<div class= "row">`;
    }
    output += `<div class="timetable-thumbnail" id="thumbnail-${i}" style="zoom:50%">`;
    output += `<div class="thumbnail-header">Timetable #${i}</div>`;
    output += `<div class="thumbnail-content">`;

    output += `<button class="semester" onclick="switchTerm('fall',${i});switchView()">`;
    output += `<div class="semesterName">Fall</div>`;
    output += `<table class="table table-bordered" id="fallTable${i}"></table>`;
    output += "</button>";
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

function displaySmallVeiwTimetable(index) {
  let terms = ["fall", "winter"];
  let len = Object.keys(allTimeTables).length;
  for (let i = 1; i <= len; i++) {
    for (let term of terms) {
      let table = document.querySelector("#" + term + "Table" + i);
      let output = "";
      let timetable = allTimeTables[i][term]; //获取课表

      for (let time of times) {
        output += `<tr>`;
        for (let j = 1; j <= 5; j++) {
          if (timetable[time][j] !== "") {
            let info = timetable[time][j];
            output += `<td id="${info["section"]}${info["color"]}" style="background-color: ${info["color"]};"></td>`;
          } else {
            output += `<td></td>`;
          }
        }
        output += `</tr>`;
      }
      // 将元素加入至HTML
      table.innerHTML = output;
      mergeSmaillViewCells(table); // 合并单元格
    }
  }
}
function mergeSmaillViewCells(table) {
  let remove = [];
  for (let i = 0; i <= 4; i++) {
    let headerCell = null;
    for (let row of table.rows) {
      const firstCell = row.cells[i];
      if (firstCell === null || firstCell.id === "") {
        continue;
      }
      if (headerCell === null || firstCell.id !== headerCell.id) {
        headerCell = firstCell;
      } else {
        headerCell.rowSpan++;
        remove.push(firstCell);
      }
    }
  }
  for (let r of remove) {
    r.remove();
  }
}
function getCourseColor(courseCode, session) {
  // 获取特定学期的颜色指示器
  var colorIndicator = document.querySelector(
    '.color-indicator[data-course-code="' + courseCode + session + '"]'
  );
  // 如果没有找到，尝试获取年课的颜色指示器
  if (!colorIndicator) {
    colorIndicator = document.querySelector(
      '.color-indicator[data-course-code="' + courseCode + 'Y"]'
    );
  }
  // 如果找到了元素，返回它的背景颜色，否则返回白色
  return colorIndicator ? colorIndicator.style.backgroundColor : "#FFFFFF";
}

function saveTimetable(data = null) {
  allTimeTables = {};
  let index = 1;
  let allGeneratedTimetable = [[[], []]];
  if (data != null) {
    allGeneratedTimetable = data["result"];
    fallTotalTimetableSize = data["totalFallTimetableSize"];
    winetTotalTimetableSize = data["totalWinterTimetableSize"];
  }

  for (let generatedTimetable of allGeneratedTimetable) {
    //初始化timetable模板
    let timetableFall = Object.assign({}, {});
    let timetableWinter = Object.assign({}, {});
    initTimetableTemplat(timetableFall, timetableWinter);
    // 将fall课程元素加入至fall timetable模板
    for (let courseInfo of generatedTimetable[0]) {
      let color = getCourseColor(courseInfo.course, "F"); // 获取课程颜色
      for (let sectionInfo of courseInfo.timeAndPlaceList) {
        let day = sectionInfo.day;
        let info = {
          course: courseInfo.course,
          section: courseInfo.section,
          prof: courseInfo.prof,
          location: sectionInfo.location != "" ? sectionInfo.location : "TBA",
          color: color,
        };
        for (var i = sectionInfo.start; i < sectionInfo.end; i += 1800000) {
          let tempTime = convertMillisecondsToHM(i);
          if (tempTime in timetableFall) {
            timetableFall[tempTime][day] = info;
          }
        }
      }
    }
    // 将winter课程元素加入至winter timetable模板
    for (let courseInfo of generatedTimetable[1]) {
      let color = getCourseColor(courseInfo.course, "S"); // 获取课程颜色
      for (let sectionInfo of courseInfo.timeAndPlaceList) {
        let day = sectionInfo.day;
        let info = {
          course: courseInfo.course,
          section: courseInfo.section,
          prof: courseInfo.prof,
          location: sectionInfo.location != "" ? sectionInfo.location : "TBA",
          color: color,
        };
        for (var i = sectionInfo.start; i < sectionInfo.end; i += 1800000) {
          let tempTime = convertMillisecondsToHM(i);
          if (tempTime in timetableWinter) {
            timetableWinter[tempTime][day] = info;
          }
        }
      }
    }
    allTimeTables[index] = { fall: timetableFall, winter: timetableWinter };
    index++;
  }
  // displaySmallVeiw();
}

function initTimetableTemplat(timetableFall, timetableWinter) {
  // 初始化timetable
  let dayTemplate = {
    1: "",
    2: "",
    3: "",
    4: "",
    5: "",
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

function displayTimetableHead(term, index) {
  var daysOfWeek = ["MON", "TUE", "WED", "THU", "FRI"];

  let timetable = allTimeTables[index][term];
  var googleMapLinks = {};
  // 初始化googleMapLinks结构
  daysOfWeek.forEach((day, index_day) => {
    let locations = [];
    let preCourse = "";
    let preSection = "";
    for (let time of times) {
      let info = timetable[time][index_day + 1];
      if (
        info &&
        info.location &&
        (preCourse != info["course"] || preSection != info["section"])
      ) {
        if (info.location in building_list) {
          let location_name = building_list[info.location];
          locations.push(location_name);
        }
      }
      preCourse = info["course"];
      preSection = info["section"];
    }

    if (locations.length > 0) {
      let baseUrl = "https://www.google.com/maps/dir/";
      let query = locations
        .map((location) => encodeURIComponent(location))
        .join("/");
      googleMapLinks[day] = baseUrl + query;
    } else {
      googleMapLinks[day] = null; // 当天没有地点的情况
    }
  });

  // 举例使用googleMapLinks
  console.log(googleMapLinks);

  // 假设thead的ID是timetable-head
  var thead = document.querySelector("#timetable-head");
  if (thead) {
    var thElements = thead.querySelectorAll("th");
    for (let i = 1; i < thElements.length; i++) {
      let day = daysOfWeek[i - 1];
      let th = thElements[i];
      th.innerHTML = "";
      if (googleMapLinks[day] != null) {
        let a = document.createElement("a");
        a.href = googleMapLinks[day];
        a.textContent = day;
        a.target = "_blank";
        th.appendChild(a);
      } else {
        th.append(day);
      }
    }
  }
}

function displayTimetable(term, index) {
  displayTimetableHead(term, index);
  let table = document.querySelector("#timetable-output");
  let output = "";
  let timetable = allTimeTables[index][term]; //获取课表

  for (let time of times) {
    let tLen = time.length;
    output += time[tLen - 2] == 0 ? `<tr><th>${time}</th>` : `<tr><th></th>`;
    for (let j = 1; j <= 5; j++) {
      if (timetable[time][j] !== "") {
        let info = timetable[time][j];
        var courseSectionKey = info["course"] + "<br>" + info["section"];
        var isLockedFall = lockedCoursesFall.includes(courseSectionKey);
        var isLockedWinter = lockedCoursesWinter.includes(courseSectionKey);
        console.log(isLockedFall || isLockedWinter);
        var lockIconClass =
          isLockedFall || isLockedWinter
            ? "fa fa-lock large-icon"
            : "fa fa-lock-open large-icon";
        var styleSet =
          isLockedFall || isLockedWinter
            ? "brightness(70%)"
            : "brightness(100%)";

        console.log(styleSet);
        output += `<td class="timetableTd" id="${time}|${term}|${j}" onclick="tdHaveSection(this)" style="background-color: ${info["color"]}; position: relative; filter: ${styleSet};">
        <b>${info["course"]}</b><br>
        <b>${info["section"]}</b>
        <span class="lock-icon" style="position: absolute; top: 0; right: 0;" onclick="lockSection(event, this);">
        <i class="${lockIconClass}"></i>
        </span>   
      </td>`;
      } else {
        output += `<td class = "" id = "${time}|${term}|${j}" onclick="tdNoSection(this)"></td>`;
      }
    }
    output += `</tr>`;
  }
  table.innerHTML = output;

  mergeCells(table);
}

function lockSection(event, span) {
  switchButton("generate");
  event.stopPropagation(); // 阻止事件冒泡到 <td> 元素

  var icon = span.querySelector("i");
  var parentTd = span.closest(".timetableTd");
  var courseInfo =
    parentTd.querySelector("b").textContent +
    "<br>" +
    parentTd.querySelectorAll("b")[1].textContent;
  var term = parentTd.getAttribute("id").split("|")[1];

  var course = parentTd.querySelector("b").innerHTML;
  var section = parentTd.querySelectorAll("b")[1].innerHTML;
  var sameCourseCells = document.querySelectorAll(
    ".timetableTd b:not(:first-child):not(:first-child):not(:first-child):not(:first-child):not(:first-child):not(:first-child):not(:first-child):not(:first-child):not(:first-child):not(:first-child):not(:first-child):not(:first-child):not(:first-child)"
  ); // 课程和节次信息在第2个和第3个 <b> 元素中

  var courseInfoForCheck = `<b>${course}</b><br><b>${section}</b>`;

  if (icon.classList.contains("fa-lock")) {
    parentTd.style.filter = "brightness(100%)";
    icon.classList.remove("fa-lock");
    icon.classList.add("fa-lock-open", "small-icon");

    if (term === "fall") {
      var index = lockedCoursesFall.indexOf(courseInfo);
      if (index !== -1) {
        lockedCoursesFall.splice(index, 1);
      }
    } else if (term === "winter") {
      var index = lockedCoursesWinter.indexOf(courseInfo);
      if (index !== -1) {
        lockedCoursesWinter.splice(index, 1);
      }
    }

    sameCourseCells.forEach(function (cell) {
      var cellCourse = cell.parentElement.querySelector("b").innerHTML;
      var cellSection = cell.parentElement.querySelectorAll("b")[1].innerHTML;
      var cellCourseInfo = `<b>${cellCourse}</b><br><b>${cellSection}</b>`;
      var cellIcon = cell.parentElement.querySelector(".lock-icon i");
      if (
        cellIcon.classList.contains("fa-lock") &&
        cellCourseInfo === courseInfoForCheck
      ) {
        cell.parentElement.style.filter = "brightness(100%)";
        cellIcon.classList.remove("fa-lock");
        cellIcon.classList.add("fa-lock-open", "small-icon");
      }
    });
  } else if (icon.classList.contains("fa-lock-open")) {
    parentTd.style.filter = "brightness(70%)";
    icon.classList.remove("fa-lock-open");
    icon.classList.add("fa-lock", "large-icon");

    if (term === "fall") {
      lockedCoursesFall.push(courseInfo);
    } else if (term === "winter") {
      lockedCoursesWinter.push(courseInfo);
    }

    sameCourseCells.forEach(function (cell) {
      var cellCourse = cell.parentElement.querySelector("b").innerHTML;
      var cellSection = cell.parentElement.querySelectorAll("b")[1].innerHTML;
      var cellCourseInfo = `<b>${cellCourse}</b><br><b>${cellSection}</b>`;
      var cellIcon = cell.parentElement.querySelector(".lock-icon i");
      if (
        cellIcon.classList.contains("fa-lock-open") &&
        cellCourseInfo === courseInfoForCheck
      ) {
        cell.parentElement.style.filter = "brightness(70%)";
        cellIcon.classList.remove("fa-lock-open");
        cellIcon.classList.add("fa-lock", "large-icon");
      }
    });
  }
  console.log(lockedCoursesFall);
  console.log(lockedCoursesWinter);
}

function mergeCells(table) {
  let remove = [];
  for (let i = 1; i <= 5; i++) {
    let headerCell = null;
    for (let row of table.rows) {
      const firstCell = row.cells[i];
      if (firstCell === null || firstCell.innerText === "") {
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
  for (let r of remove) {
    r.remove();
  }
}

function convertMillisecondsToHM(milliseconds) {
  var hours = parseInt(
    (milliseconds % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
  );
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
function tdHaveSection(event) {
  let element = document.querySelector("#courseDetaillInfo");
  let idName = event.id.split("|");
  let time = idName[0];
  let term = idName[1];
  let day = idName[2];
  let timetable = allTimeTables[1][term];
  let info = timetable[time][day];

  //获取课程详细信息
  let course = info["course"];
  let section = info["section"];
  let location = info["location"];
  let profName = info["prof"];
  let detaillInfo = `<p><b>Course:</b> ${course}<br><b>Section:</b> ${section}<br><b>Location:</b> ${location}</p>`;

  if (profName !== "" && section.includes("LEC")) {
    if (profName.includes(",")) {
      let profNameList = profName.split(",");
      for (let prof of profNameList) {
        detaillInfo += `<p>Prof. ${prof}: <a href="https://www.ratemyprofessors.com/search/professors?q=${prof}" target="_blank">Click me to view the professor ratings</a></p>`;
      }
    } else {
      detaillInfo += `<p>Prof. ${profName}: <a href="https://www.ratemyprofessors.com/search/professors?q=${profName}" target="_blank">Click me to view the professor ratings</a></p>`;
    }
  }

  // 将信息添加进弹窗并激活弹窗
  element.innerHTML = detaillInfo;
  document.querySelector("#displayDetaillCourseInfo").click();
}

// BLOCK Time Section function
function tdNoSection(event) {}

// *************************** switchs ************************************************//
function switchView() {
  var largeView = document.getElementById("large-view");
  var smallView = document.getElementById("small-view");

  // 切换active类来显示或隐藏视图
  largeView.classList.toggle("active");
  smallView.classList.toggle("active");

  // 更新切换视图按钮的文本
  var toggleBtn = document.getElementById("toggle-view-btn");
  toggleBtn.textContent = largeView.classList.contains("active")
    ? "MORE OPTION"
    : "BACK";
}

function switchButton(name) {
  var generateBtn = document.getElementById("generate-schedule-btn");
  var toggleBTn = document.getElementById("toggle-view-btn");

  // 切换active类来显示或隐藏视图
  if (
    (name === "toggle" && !toggleBTn.className.includes("active")) ||
    (name === "generate" && !generateBtn.className.includes("active"))
  ) {
    generateBtn.classList.toggle("active");
    toggleBTn.classList.toggle("active");
  }
}

function switchTerm(term, index = 1) {
  // 获取所有的 term 按钮和课程列表
  var termButtons = document.querySelectorAll(".term-btn");
  var coursesSections = document.querySelectorAll(".courses-section-content");

  // 移除所有按钮的 active 类，并隐藏所有课程列表
  termButtons.forEach(function (btn) {
    btn.classList.remove("active");
  });
  coursesSections.forEach(function (section) {
    section.style.display = "none";
  });

  // 根据选择的学期显示课程列表，并激活相应的按钮
  if (term === "fall") {
    displayTimetable(term, index);
    document.getElementById("fall-courses").style.display = "block";
    document
      .querySelector(`button[onclick="switchTerm(\'fall\')"]`)
      .classList.add("active");
  } else {
    displayTimetable(term, index);
    document.getElementById("winter-courses").style.display = "block";
    document
      .querySelector(`button[onclick="switchTerm(\'winter\')"]`)
      .classList.add("active");
  }
  switchCridit(term);
}

function switchCridit(term) {
  let cridit = document.querySelectorAll(".cridit");
  let output = "";

  if (term === "fall") {
    output = `Cridits: ${fallCredit.toFixed(2)} / 3.00`;
  } else {
    output = `Cridits: ${winterCredit.toFixed(2)} / 3.00`;
  }
  cridit.forEach((cridit) => {
    cridit.innerText = output;
  });
}

//************************** sidebar *******************************/
// 随机颜色
var usedColor = [];
function getRandomColor() {
  let color = 360 * Math.random();
  while (true) {
    let checkStop = true;
    for (let used of usedColor) {
      if (Math.abs(color - used) < 1.0) {
        checkStop = false;
        break;
      }
    }
    if (checkStop) {
      break;
    }
    color = 360 * Math.random();
  }
  usedColor.push(color);
  return "hsla(" + ~~color + "," + "70%," + "80%,1)";
}

// 生成课程元素
function generateCourses(course, term, color = null) {
  var continerId = "winter-courses";
  if (term === "fall") {
    continerId = "fall-courses";
  }
  const coursesDiv = document.getElementById(continerId);

  const courseItem = document.createElement("div");
  courseItem.className = "course-item";
  courseItem.id = course;

  // 颜色
  const colorIndicator = document.createElement("div");
  colorIndicator.className = "color-indicator";
  colorIndicator.setAttribute("data-course-code", course);

  colorIndicator.style.backgroundColor =
    color != null ? color : getRandomColor(); // 设置随机颜色

  // 名称
  const courseName = document.createElement("span");
  courseName.className = "course-name";
  courseName.textContent = course.slice(0, -1) + " " + course.slice(-1); // 设置课程名称

  // 编辑按钮
  // const editBtn = document.createElement('button');
  // editBtn.className = 'edit-btn';
  // editBtn.textContent = '✏️';
  // TODO: 添加编辑按钮的事件监听

  // 删除按钮
  const deleteBtn = document.createElement("button");
  deleteBtn.className = "delete-btn";
  deleteBtn.id = course;
  deleteBtn.textContent = "🗑️";
  deleteBtn.setAttribute("onclick", "deleteCourse(this)");

  // 将所有元素添加到课程元素中
  courseItem.appendChild(colorIndicator);
  courseItem.appendChild(courseName);
  // courseItem.appendChild(editBtn);
  courseItem.appendChild(deleteBtn);

  coursesDiv.appendChild(courseItem);
}

function deleteCourse(deleteBtn) {
  let deleteId = deleteBtn.id;

  for (let i = 0; i < fallCourseChoose.length; i++) {
    if (fallCourseChoose[i] === deleteId) {
      fallCourseChoose.splice(i, 1);
      fallCredit -= 0.5;
      switchCridit("fall");
    }
  }
  for (let i = 0; i < winterCourseChoose.length; i++) {
    if (winterCourseChoose[i] === deleteId) {
      winterCourseChoose.splice(i, 1);
      winterCredit -= 0.5;
      switchCridit("winter");
    }
  }
  while (true) {
    const element = document.getElementById(deleteId);
    if (element) {
      element.remove();
    } else {
      break;
    }
  }
  switchButton("generate");
}

//********************************drag btns**************/
function dragInit() {
  let list = document.querySelector(".preference-list");
  let currentLi;
  list.addEventListener("dragstart", (e) => {
    e.dataTransfer.effectAllowed = "move";
    currentLi = e.target;
    setTimeout(() => {
      currentLi.classList.add("moving");
    });
  });

  list.addEventListener("dragenter", (e) => {
    e.preventDefault();
    if (e.target === currentLi || e.target === list) {
      return;
    }
    let liArray = Array.from(list.childNodes);
    let currentIndex = liArray.indexOf(currentLi);
    let targetindex = liArray.indexOf(e.target);

    if (currentLi.id === "weigth" && e.target.id === "weigth") {
      switchButton("generate");
      if (currentIndex < targetindex) {
        list.insertBefore(currentLi, e.target.nextElementSibling);
      } else {
        list.insertBefore(currentLi, e.target);
      }
    }
  });
  list.addEventListener("dragover", (e) => {
    e.preventDefault();
  });
  list.addEventListener("dragend", (e) => {
    currentLi.classList.remove("moving");
  });
}

function dragInitMobile() {
  let list = document.querySelector(".preference-list");
  let currentLi = null;
  list.addEventListener(
    "touchstart",
    function (e) {
      // 阻止默认滑动
      currentTarget = e.target;
      if (
        currentTarget.parentNode.className !== "options" &&
        currentTarget.className !== "preference-list"
      ) {
        e.preventDefault();
        currentLi = currentTarget;
        currentLi.classList.add("moving");
      }
    },
    false
  );

  list.addEventListener(
    "touchmove",
    function (e) {
      if (!currentLi) return;

      let touchLocation = e.targetTouches[0];
      let targetElement = document.elementFromPoint(
        touchLocation.clientX,
        touchLocation.clientY
      );

      // 确保目标元素是列表项并且不是当前拖动的列表项
      if (
        targetElement &&
        targetElement !== currentLi &&
        targetElement.parentNode === list
      ) {
        moveListItem(targetElement);
      }
    },
    false
  );

  list.addEventListener(
    "touchend",
    function (e) {
      if (!currentLi) return;
      currentLi.classList.remove("moving");
      currentLi = null;
    },
    false
  );

  function moveListItem(targetElement) {
    let targetIndex = Array.from(list.children).indexOf(targetElement);
    let currentIndex = Array.from(list.children).indexOf(currentLi);
    if (currentLi.id !== "weigth") {
      currentLi = currentLi.parentNode;
    }
    if (currentLi !== null && currentLi.id === "weigth") {
      if (targetElement !== null && targetElement.id !== "weigth") {
        targetElement = targetElement.parentNode;
      }
      if (targetElement !== null && targetElement.id === "weigth") {
        switchButton("generate");
        if (currentIndex < targetIndex) {
          list.insertBefore(currentLi, targetElement.nextSibling);
        } else if (currentIndex > targetIndex) {
          list.insertBefore(currentLi, targetElement);
        }
      }
    }
  }
}

function getRadioInput(radioName) {
  var item = null;
  var obj = document.getElementsByName(radioName);
  for (var i = 0; i < obj.length; i++) {
    //遍历Radio
    if (obj[i].checked) {
      item = obj[i].value;
    }
  }
  return item;
}

function getWeight() {
  let weigth = {
    timeWeight: null,
    daySpend: null,
    classInterval: null,
  };
  let parentDiv = document.getElementById("weigth-parent");
  // 获取所有子元素
  var children = parentDiv.children;

  // 遍历并打印每个子元素及其顺序
  let idx = 4000;
  for (let i = 0; i < children.length; i++) {
    if (children[i].id === "weigth") {
      let preferenceChildren = children[i].innerText;

      if (preferenceChildren.includes("Start time")) {
        weigth["timeWeight"] = idx;
        idx -= 1000;
      } else if (preferenceChildren.includes("Class Interval")) {
        weigth["classInterval"] = idx;
        idx -= 1000;
      } else if (preferenceChildren.includes("Days Spent")) {
        weigth["daySpend"] = idx;
        idx -= 1000;
      } else if (preferenceChildren.includes("Reasonable Walking Time")) {
        weigth["walkTime"] = idx;
        idx -= 1000;
      }

      if (idx <= 0) {
        break;
      }
    }
  }
  return weigth;
}

function checkboxMonitor() {
  var radios = document.querySelectorAll("input[type=radio]");

  // 为每个单选按钮添加change事件监听器
  radios.forEach(function (radio) {
    radio.addEventListener("change", function () {
      if (this.checked) {
        switchButton("generate");
      }
    });
  });
}

function showLoading() {
  document.getElementById("loading-overlay").style.display = "flex";
}

function hideLoading() {
  document.getElementById("loading-overlay").style.display = "none";
}

function fetchData() {
  let generateBtn = document.querySelector("#generate-schedule-btn");
  generateBtn.disabled = true;
  showLoading();
  setTimeout(addCourseToTimetable, 1);
}
