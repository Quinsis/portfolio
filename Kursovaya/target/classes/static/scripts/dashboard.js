const ctxAverage = document.getElementById('averageSalariesChart');
const ctxCorrelationRegression = document.getElementById('correlationRegressionChart');

let labelsForAverageSalaries = "${averageSalariesLabels}".split(",");
let valuesForAverageSalaries = "${averageSalariesValues}".split(",");
let valuesOdds = "${odds}".split(",");

let valuesX = "${xValues}".split(",");
let valuesY = "${yValues}".split(",");

let maxX = -Infinity;
for (let i = 0; i < valuesX.length; i++) {
    if (valuesX[i] > maxX) {
        maxX = valuesX[i];
    }
}

let dataDoubles = new Array(valuesX.length);
for (let i = 0; i < valuesX.length; i++) {
    let pair = {x: valuesX[i], y: valuesY[i]};
    dataDoubles[i] = pair;
}

var lineData = [2];
let b = valuesOdds[0];
let k = valuesOdds[1];

lineData[0] = {x: valuesX[0], y: valuesX[0] * +k + +b};
lineData[1] = {x: maxX, y: +maxX * +k + +b};

var dataForCorrelation = {
    datasets: [{
        label: 'Корреляция: ${correlation}',
        data: dataDoubles,
        backgroundColor: 'rgb(255, 99, 132, 0.7)',
        borderWidth: 1
    }, {
        label: 'Регрессия: ${equationLinearRegression}',
        data: lineData,
        backgroundColor: 'rgba(54, 162, 235, 0.2)',
        borderColor: 'rgba(54, 162, 235, 1)',
        borderWidth: 1,
        showLine: true
    }]
};

new Chart(ctxAverage, {
    type: 'bar',
    data: {
        labels: labelsForAverageSalaries,
        datasets: [{
            label: 'Средняя зарплата',
            data: valuesForAverageSalaries,
            backgroundColor: [
                'rgba(255, 99, 132, 0.6)',
                'rgba(255, 159, 64, 0.6)',
                'rgba(255, 205, 86, 0.6)',
                'rgba(75, 192, 192, 0.6)',
                'rgba(54, 162, 235, 0.6)',
                'rgba(153, 102, 255, 0.6)',
                'rgba(201, 203, 207, 0.6)'
            ],
            borderWidth: 1
        }]
    },
    options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                display: false
            }
        },
        scales: {
            x: {
                position: 'bottom',
                title: {
                    display: true,
                    text: 'Образование'
                }
            },
            y: {
                title: {
                    display: true,
                    text: 'Зарплата, руб.'
                }
            }
        }
    }
});
new Chart(ctxCorrelationRegression, {
    type: 'scatter', // Тип диаграммы - рассеяние
    data: dataForCorrelation,
    options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
            x: {
                ticks: {
                    // Заменить числа на названия
                    callback: function(value) {
                        const titles = ['Среднее общее',
                            'Среднее профессиональное',
                            'Бакалавриат',
                            'Специалитет / магистратура'
                        ];
                        if (value > 7 || value < 4) return "Не указано";
                        else return titles[value - 4];
                    }
                },
                type: 'linear', // Тип оси x - линейный
                title: {
                    display: true,
                    text: 'Образование' // Заголовок оси x
                }
            },
            y: {
                beginAtZero: true,
                type: 'linear', // Тип оси y - линейный
                title: {
                    display: true,
                    text: 'Зарплата, руб.' // Заголовок оси y
                }
            }
        },
        plugins: {
            tooltip: {
                callbacks: {
                    label: function(context) {
                        return '(' + context.raw.x + ', ' + context.raw.y + ')'; // Формат подсказки для каждой точки
                    }
                }
            }
        }
    }
});

var navbarToggle = false;
document.getElementById("burger").addEventListener("click", function() {
    if (!navbarToggle) {
        document.getElementsByClassName("chartsList")[0].style.left = "0";
        document.getElementsByTagName("small")[0].style.left = "0";
        document.getElementsByClassName("form")[0].style.left = "0";
        document.getElementsByClassName("charts")[0].style.opacity = "0";
        document.getElementsByTagName("html")[0].style.overflowY = "hidden";
        navbarToggle = true;
    } else {
        document.getElementsByClassName("chartsList")[0].style.left = "-999px";
        document.getElementsByTagName("small")[0].style.left = "-999px";
        document.getElementsByClassName("form")[0].style.left = "-999px";
        document.getElementsByClassName("charts")[0].style.opacity = "1";
        document.getElementsByTagName("html")[0].style.overflowY = "auto";
        navbarToggle = false;
    }
})

let chartTypes = document.getElementsByClassName("chartType");
let charts = document.getElementsByTagName("canvas");

if (charts.length > 0) charts[0].style.display = "block";
if (chartTypes.length > 0) chartTypes[0].classList.add("activeType");

for (let i = 1; i < chartTypes.length; i++) {
    charts[i].style.display = "none";
}

for (let i = 0; i < chartTypes.length; i++) {
    chartTypes[i].addEventListener("click", function () {
        chartTypes[i].classList.add("activeType");
        for (let j = 0; j < i; j++) {
            chartTypes[j].classList.remove("activeType");
        }
        for (let j = i + 1; j < chartTypes.length; j++) {
            chartTypes[j].classList.remove("activeType");
        }
        charts[i].style.display = "block";
        for (let j = 0; j < i; j++) {
            charts[j].style.display = "none";
        }
        for (let j = i + 1; j < chartTypes.length; j++) {
            charts[j].style.display = "none";
        }
    })
}


function saveAsPDF () {
    document.getElementsByClassName("charts")[0].style.flexDirection = "column";
    document.getElementsByClassName("charts")[0].style.gap = "50px";
    var indexes = [];
    for (let i = 0; i < charts.length; i++) {
        if (charts[i].style.display === "none") {
            charts[i].style.display = "block";
            indexes.push(i);
            console.log(i);
        }
    }

    var doc = new jsPDF();
    for (var i = 0; i < charts.length; i++) {
        var img = charts[i].toDataURL("image/png");
        var pdfWidth = 190;
        var pdfHeight = 380;
        var canvasWidth = charts[i].width;
        var canvasHeight = charts[i].height;
        var widthRatio = pdfWidth / canvasWidth;
        var heightRatio = pdfHeight / canvasHeight;
        var ratio = Math.min(widthRatio, heightRatio);
        var imgWidth = canvasWidth * ratio;
        var imgHeight = canvasHeight * ratio;

        doc.addImage(img, "PNG", 10, 10, imgWidth, imgHeight);

        if (i < charts.length - 1) {
            doc.addPage();
        }
    }
    doc.save("Анализ данных.pdf");

    indexes.forEach(index => charts[index].style.display = "none");
    document.getElementsByClassName("charts")[0].style.flexDirection = "row";
    document.getElementsByClassName("charts")[0].style.gap = "0";
}