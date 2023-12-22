fetch("./lib/test.json").then(function (response) {

    return response.json();

}).then(function (products) {
    var timetable = {};
    for (var i = 9; i <= 21; i++) {
        let dayTemplate = {
            1:" ",
            2:" ",
            3:" ",
            4:" ",
            5:" ",
            6:" ",
            7:" "
        }    
        if (i < 10) {
            timetable["0" + i + ":00"] = dayTemplate;
        } else {
            timetable["" + i + ":00"] = dayTemplate;
        }
    }

    for (let product of products) {
        let dayTemplate = {
            1:" ",
            2:" ",
            3:" ",
            4:" ",
            5:" ",
            6:" ",
            7:" "
        }    
        let day = product.time.day;
        let timeStart = convertMillisecondsToHM(product.time.start);
        let timeEnd = convertMillisecondsToHM(product.time.end);
        
        for (var i = product.time.start; i < product.time.end; i += 6000) {
            let tempTime = convertMillisecondsToHM(i);
            if (tempTime in timetable) {
                timetable[tempTime][day] = product.course + "\n" + product.section;
            }
        }
        if (!(timeStart in timetable)) {
            timetable[timeStart] = dayTemplate;
            timetable[timeStart][day] = product.course+ "\n" + product.section;
        }
    }

    let p = document.querySelector("#output");
    let output = "";
    let times = Object.keys(timetable).sort();
    for(let time of times){
        output += `<tr><th>${time}</th>`;
        for(let j = 1; j <= 7; j++){
            output += `<td>${timetable[time][j]}</td>`;
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