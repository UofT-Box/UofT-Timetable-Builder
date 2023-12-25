fetch("./lib/test.json").then(function (response) {

    return response.json();

}).then(function (products) {
    var timetable = {};
    var isDisplay = {};
    let dayTemplate = {
        1:true,
        2:true,
        3:true,
        4:true,
        5:true,
        6:true,
        7:true
    }; 
    for (var i = 9; i <= 21; i++) { 
        if (i < 10) {
            timetable["0" + i + ":00"] = Object.assign({},dayTemplate);
            isDisplay["0" + i + ":00"] = Object.assign({},dayTemplate);

            timetable["0" + i + ":30"] = Object.assign({},dayTemplate);
            isDisplay["0" + i + ":30"] = Object.assign({},dayTemplate);
        } else {
            timetable["" + i + ":00"] = Object.assign({},dayTemplate);
            isDisplay["" + i + ":00"] = Object.assign({},dayTemplate);

            timetable["" + i + ":30"] = Object.assign({},dayTemplate);
            isDisplay["" + i + ":30"] = Object.assign({},dayTemplate);
        }
    }
    
    for (let product of products) {
        let day = product.time.day;
        let timeStart = convertMillisecondsToHM(product.time.start);
        let timeEnd = convertMillisecondsToHM(product.time.end);
        let counter = 1;
        let lastTime = null;
        // if (!(timeStart in timetable)) {
        //     timetable[timeStart] = Object.assign({},dayTemplate);
        //     isDisplay[timeStart] = Object.assign({},dayTemplate);
        // }
        for (var i = product.time.start; i < product.time.end; i += 1800000) {
            let tempTime = convertMillisecondsToHM(i);
            if ((tempTime != timeStart) && (tempTime in timetable) && (tempTime != lastTime)) {
                // if (tempTime == "19:00" && day == 4){
                //     console.log(`<td rowspan="${counter}">${product.course + product.section}</td>`);
                // }
                isDisplay[tempTime][day] = false;
                lastTime = tempTime;
                counter++;
            }
        }
        // if (!(timeEnd in timetable)) {
        //     timetable[timeEnd] = Object.assign({},dayTemplate);
        //     isDisplay[timeEnd] = Object.assign({},dayTemplate);
        // }
        timetable[timeStart][day] = `<td rowspan="${counter}">${product.course + product.section}</td>`;
        // if (day == 5){
        //     console.log(`<td rowspan="${counter}">${product.course + product.section}</td>`);
        // }
    }
    console.log(isDisplay)
    let p = document.querySelector("#output");
    let output = "";
    let times = Object.keys(timetable).sort();
    for(let time of times){
        output += `<tr><th>${time}</th>`;
        for(let j = 1; j <= 7; j++){
            if(isDisplay[time][j] == true){
                if (timetable[time][j] != true){
                    output += timetable[time][j];

                }else{
                    output += `<td></td>`;
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