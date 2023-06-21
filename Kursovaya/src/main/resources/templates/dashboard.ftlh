<!doctype html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta id="viewport" name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <link rel="stylesheet" href="/css/style.css">
    <link rel="stylesheet" href="/css/dashboard.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <title>Панель инструментов</title>
</head>
<body>
    <div id="content">
        <div class="navbar">
            <div class="header">
                <h1 class="title">Dashboard</h1>
                <a id="exit" href="upload"><i class="fa-solid fa-arrow-right-from-bracket"></i></a>
            </div>
            <small>Графики</small>
            <ul class="chartsList">
                <li id="averageSalaries" class="chartType">Средняя зарплата</li>
                <li id="correlationEducationSalary" class="chartType">Корреляция и регрессия #1</li>
                <li id="correlationExperienceSalary" class="chartType">Корреляция и регрессия #2</li>
            </ul>
            <div class="form">
                <a onclick="saveAsPDF()" class="exportButton">Экспорт данных</a>
            </div>
            <div id="burger" class="burger">
                <span></span>
                <span></span>
                <span></span>
            </div>
        </div>
        <div id="charts" class="charts">
            <canvas width="100%" height="70vh" id="averageSalariesChart"></canvas>
            <canvas width="100%" height="70vh" id="correlationRegressionChartEducationSalary"></canvas>
            <canvas width="100%" height="70vh" id="correlationRegressionChartExperienceSalary"></canvas>
            <div id="about">
                <h1 id="about_title">Выводы анализа</h1>
                <p id="about_text"></p>
            </div>
            <div id="more" class="more unchecked">
                <span id="more_span"><small>Подробнее</small><i class="fa-solid fa-chevron-down"></i></span>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/1.8.2/jquery.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/html2canvas/0.4.1/html2canvas.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jspdf/1.4.1/jspdf.min.js"></script>
    <script type="text/javascript" th:inline="javascript">
        // Переменные для переключения скрытых панелей
        let moreToggle = false;
        let navbarToggle = false;

        // Переменные для работы с графиками
        let chartTypes = document.getElementsByClassName("chartType");
        let charts = document.getElementsByTagName("canvas");

        // Включаем график средней зарплаты
        switchChart("averageSalaries");
        moreOff();

        // Отключение экранирования сообщения выводов и инициализация #about_text
        var docEducationSalary = new DOMParser().parseFromString("${conclusionEducationSalary}", "text/html");
        var docExperienceSalary = new DOMParser().parseFromString("${conclusionExperienceSalary}", "text/html");
        document.getElementById("about_text").innerHTML = docEducationSalary.documentElement.innerText;

        // Контексты для создания графиков
        const ctxAverage = document.getElementById('averageSalariesChart');
        const ctxCorrelationRegressionEducationSalary = document.getElementById('correlationRegressionChartEducationSalary');
        const ctxCorrelationRegressionExperienceSalary = document.getElementById('correlationRegressionChartExperienceSalary');

        // Заполнение массива объектов названий и значений для графика со средней зарплатой
        let labelsForAverageSalaries = "${averageSalariesLabels}".split(",");
        let valuesForAverageSalaries = "${averageSalariesValues}".split(",");

        // Заполнение массива объектов названий и значений для графика с корреляцией и регрессией EducationSalary
        let valuesOddsEducationSalary = "${oddsEducationSalary}".split(",");
        let valuesXEducationSalary = "${xValuesEducationSalary}".split(",");
        let valuesYEducationSalary = "${yValuesEducationSalary}".split(",");

        // Заполнение массива объектов названий и значений для графика с корреляцией и регрессией ExperienceSalary
        let valuesOddsExperienceSalary = "${oddsExperienceSalary}".split(",");
        let valuesXExperienceSalary = "${xValuesExperienceSalary}".split(",");
        let valuesYExperienceSalary = "${yValuesExperienceSalary}".split(",");

        // Определение начальной точки линии корреляции EducationSalary
        let maxXEducationSalary = -Infinity;
        for (let i = 0; i < valuesXEducationSalary.length; i++) {
            if (valuesXEducationSalary[i] > maxXEducationSalary) {
                maxXEducationSalary = valuesXEducationSalary[i];
            }
        }

        // Определение начальной точки линии корреляции ExperienceSalary
        let maxXExperienceSalary = -Infinity;
        for (let i = 0; i < valuesXExperienceSalary.length; i++) {
            if (valuesXExperienceSalary[i] > maxXExperienceSalary) {
                maxXExperienceSalary = valuesXExperienceSalary[i];
            }
        }

        // Заполнение массива объектов значений для графика с корреляцией и регрессией EducationSalary
        let dataDoublesEducationSalary = new Array(valuesXEducationSalary.length);
        for (let i = 0; i < valuesXEducationSalary.length; i++) {
            let pair = {x: valuesXEducationSalary[i], y: valuesYEducationSalary[i]};
            dataDoublesEducationSalary[i] = pair;
        }

        var lineDataEducationSalary = [2];
        let bEducationSalary = valuesOddsEducationSalary[0];
        let kEducationSalary = valuesOddsEducationSalary[1];
        lineDataEducationSalary[0] = {x: valuesXEducationSalary[0], y: valuesXEducationSalary[0] * +kEducationSalary + +bEducationSalary};
        lineDataEducationSalary[1] = {x: maxXEducationSalary, y: +maxXEducationSalary * +kEducationSalary + +bEducationSalary};
        var dataForCorrelationEducationSalary = {
            datasets: [
                {
                    label: 'Наблюдения',
                    data: dataDoublesEducationSalary,
                    backgroundColor: 'rgb(255, 99, 132, 0.4)',
                    borderWidth: 1
                },
                {
                    label: 'Линейная регрессия',
                    data: lineDataEducationSalary,
                    backgroundColor: 'rgba(54, 162, 235, 0.2)',
                    borderColor: 'rgba(54, 162, 235, 1)',
                    borderWidth: 1,
                    showLine: true
                }
            ]
        };

        // Заполнение массива объектов значений для графика с корреляцией и регрессией ExperienceSalary
        let dataDoublesExperienceSalary = new Array(valuesXExperienceSalary.length);
        for (let i = 0; i < valuesXExperienceSalary.length; i++) {
            let pair = {x: valuesXExperienceSalary[i], y: valuesYExperienceSalary[i]};
            dataDoublesExperienceSalary[i] = pair;
        }

        var lineDataExperienceSalary = [2];
        let bExperienceSalary = valuesOddsExperienceSalary[0];
        let kExperienceSalary = valuesOddsExperienceSalary[1];
        lineDataExperienceSalary[0] = {x: 0, y: +bExperienceSalary};
        lineDataExperienceSalary[1] = {x: 30, y: +30 * +kExperienceSalary + +bExperienceSalary};
        var dataForCorrelationExperienceSalary = {
            datasets: [
                {
                    label: 'Наблюдения',
                    data: dataDoublesExperienceSalary,
                    backgroundColor: 'rgb(255, 99, 132, 0.4)',
                    borderWidth: 1
                },
                {
                    label: 'Линейная регрессия',
                    data: lineDataExperienceSalary,
                    backgroundColor: 'rgba(54, 162, 235, 0.2)',
                    borderColor: 'rgba(54, 162, 235, 1)',
                    borderWidth: 1,
                    showLine: true
                }
            ]
        };

        // Создание графика средней зарплаты
        var averageChart = new Chart(ctxAverage, {
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
                        ticks: {
                            // Замена чисел на названия
                            callback: function(value) {
                                const titles = ['Среднее общее',
                                    'Среднее профессиональное',
                                    'Бакалавриат',
                                    'Специалитет / магистратура'
                                ];
                                if (value > 3 || value < 0) return "Не указано";
                                else return titles[value];
                            }
                        },
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

        // Создание графика корреляции и регрессии EducationSalary
        var correlationChartEducationSalary = new Chart(ctxCorrelationRegressionEducationSalary, {
            type: 'scatter', // Тип диаграммы - рассеяние
            data: dataForCorrelationEducationSalary,
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: {
                        ticks: {
                            // Замена чисел на названия
                            callback: function(value) {
                                const titles = ['Среднее общее',
                                    'Среднее профессиональное',
                                    'Бакалавриат',
                                    'Специалитет / магистратура'
                                ];
                                if (value > 7 || value < 4) return "Не указано";
                                else return titles[value - 4];
                            }
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
                                return 'Зарплата: ' + context.raw.y; // Формат подсказки для каждой точки
                            }
                        }
                    }
                }
            }
        });

        // Создание графика корреляции и регрессии ExperienceSalary
        var correlationChartExperienceSalary = new Chart(ctxCorrelationRegressionExperienceSalary, {
            type: 'scatter', // Тип диаграммы - рассеяние
            data: dataForCorrelationExperienceSalary,
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: {
                        beginAtZero: true,
                        type: 'linear', // Тип оси y - линейный
                        title: {
                            display: true,
                            text: 'Стаж работы, года' // Заголовок оси y
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
                                return 'Зарплата: ' + context.raw.y; // Формат подсказки для каждой точки
                            }
                        }
                    }
                }
            }
        });

        // Переключение бургер-меню
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

        // Функция переключение графиков
        function switchChart(chartName) {
            let requiredIndex;
            for (let i = 0; i < chartTypes.length; i++) {
                if (chartTypes[i].id.endsWith(chartName)) {
                    requiredIndex = i;
                }
            }
            if (requiredIndex === null) return;


            chartTypes[requiredIndex].classList.add("activeType");
            for (let i = 0; i < requiredIndex; i++) {
                chartTypes[i].classList.remove("activeType");
            }
            for (let i = requiredIndex + 1; i < chartTypes.length; i++) {
                chartTypes[i].classList.remove("activeType");
            }
            charts[requiredIndex].style.setProperty("display", "flex", "important");

            for (let i = 0; i < requiredIndex; i++) {
                charts[i].style.setProperty("display", "none", "important");
            }
            for (let i = requiredIndex + 1; i < chartTypes.length; i++) {
                charts[i].style.setProperty("display", "none", "important");
            }

            if (chartTypes[requiredIndex].id.endsWith("correlationEducationSalary")) {
                document.getElementById("more").style.setProperty("display", "flex", "important");
                document.getElementById("about_text").innerHTML = docEducationSalary.documentElement.innerText;
            } else if (chartTypes[requiredIndex].id.endsWith("correlationExperienceSalary")) {
                document.getElementById("more").style.setProperty("display", "flex", "important");
                document.getElementById("about_text").innerHTML = docExperienceSalary.documentElement.innerText;
            } else {
                document.getElementById("more").style.setProperty("display", "none", "important");
                document.getElementById("about").style.setProperty("display", "none", "important");
                document.getElementById("more").classList.remove("checked");
                document.getElementById("more").classList.add("unchecked");
                moreToggle = false;
                moreOff();
            }
        }

        // Обработка события переключения графика
        for (let i = 0; i < chartTypes.length; i++) {
            chartTypes[i].addEventListener("click", function () {
                switchChart(chartTypes[i].id);
            })
        }

        // Сохранение ПДФ
        function saveAsPDF () {
            var indexes = [];
            for (let i = 0; i < charts.length; i++) {
                if (charts[i].style.display === "none") {
                    charts[i].style.display = "block";
                    indexes.push(i);
                }
            }

            var doc = new jsPDF({userUnit: 600});
            var imgWidth;
            var imgHeight;

            for (var i = 0; i < charts.length; i++) {
                var img = charts[i].toDataURL("image/png");
                var pdfWidth = 190;
                var pdfHeight = 380;
                var canvasWidth = charts[i].width;
                var canvasHeight = charts[i].height;
                var widthRatio = pdfWidth / canvasWidth;
                var heightRatio = pdfHeight / canvasHeight;
                var ratio = Math.min(widthRatio, heightRatio);
                imgWidth = canvasWidth * ratio;
                imgHeight = canvasHeight * ratio;
                doc.addImage(img, "PNG", 10, 10, imgWidth, imgHeight);
                doc.addPage();
            }

            var about = document.getElementById("about");
            // добавляем image в doc канваса созданного из div с id about
            html2canvas(about).then(function(canvas) {
                // преобразуем canvas в изображение формата PNG
                var img = canvas.toDataURL("image/png");
                var pdfWidth = 190;
                var pdfHeight = 380;
                var canvasWidth = about.offsetWidth;
                var canvasHeight = about.offsetHeight;
                var widthRatio = pdfWidth / canvasWidth;
                var heightRatio = pdfHeight / canvasHeight;
                var ratio = Math.min(widthRatio, heightRatio);
                imgWidth = canvasWidth * ratio;
                imgHeight = canvasHeight * ratio;
                // добавляем изображение в doc с указанием формата, координат и размеров
                doc.addImage(img, "PNG", 10, 10, 180, imgHeight);
                // сохраняем pdf как файл
                doc.save("Анализ данных.pdf");
            });

            indexes.forEach(index => charts[index].style.display = "none");
        }

        function moreOn() {
            moreToggle = true;
            document.getElementById("more_span").setAttribute("disabled", "");
            document.getElementById("about").style.display = "flex";
            document.getElementById("about").scrollIntoView({behavior: "smooth"});
            document.getElementById("more").classList.add("checked");
            document.getElementById("more").classList.remove("unchecked");

            document.getElementsByClassName("exportButton")[0].classList.remove("disabled");
            document.getElementsByClassName("exportButton")[0].setAttribute("onclick", "saveAsPDF()");

            document.getElementById("more_span").innerHTML = "<small>Скрыть</small><i class=\"fa-solid fa-chevron-down\"></i>";

            // Блокировка нажатия
            setTimeout(function () {
                document.getElementById("more_span").removeAttribute("disabled");
            },  document.getElementById("about").offsetHeight);
        }
        function moreOff() {
            moreToggle = false;

            let indexOfActiveChart;
            for (let i = 0; i < chartTypes.length; i++) {
                if (chartTypes[i].classList.contains("activeType")) {
                    indexOfActiveChart = i;
                    break;
                }
            }
            document.getElementById("more_span").setAttribute("disabled", "");
            document.getElementsByTagName("canvas")[indexOfActiveChart].scrollIntoView({behavior: "smooth"});
            document.getElementById("more").classList.add("unchecked");
            document.getElementById("more").classList.remove("checked");

            document.getElementsByClassName("exportButton")[0].classList.add("disabled");
            document.getElementsByClassName("exportButton")[0].removeAttribute("onclick");

            document.getElementById("more_span").innerHTML = "<small>Подробнее</small><i class=\"fa-solid fa-chevron-down\"></i>";

            // Блокировка нажатия
            setTimeout(function () {
                document.getElementById("about").style.display = "none";
                document.getElementById("more_span").removeAttribute("disabled");
            },  document.getElementById("about").offsetTop - document.getElementsByTagName("canvas")[indexOfActiveChart].offsetTop);
        }

        // Обработка показа выводов анализа
        document.getElementById("more").addEventListener("click", function () {
            if (!moreToggle && !document.getElementById("more_span").hasAttribute("disabled")) moreOn();
            else if (!document.getElementById("more_span").hasAttribute("disabled")) moreOff();
        })
    </script>
</body>
</html>