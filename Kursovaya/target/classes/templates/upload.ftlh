<!doctype html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <link rel="stylesheet" href="/css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <title>Загрузка файла</title>
</head>
<body>
    <main id="content">
        <div class="info_block">
            <h1 class="info_title">Корреляционно-регрессионный анализ</h1>
            <small>Анализ взаимосвязи между уровнем образования и заработной платой</small>
        </div>
        <div class="upload_block">
            <form class="upload_form" action="/upload" method="post" enctype="multipart/form-data">
                <h1 class="upload_title">Загрузите или перетащите CSV файл</h1>
                <input type="file" name="file" accept=".csv" required>
                <input class="analyse_button" type="submit" value="Провести анализ">
                <i id="error" class="fa-solid fa-circle-exclamation" title=""></i>
            </form>
        </div>
    </main>
    <script>
        let error = document.getElementById("error");
        let file = document.getElementsByName("file")[0];

        <#if errorMessage??>
            error.setAttribute("title", "${errorMessage}");
            error.style.display = "block";
        </#if>

        file.addEventListener("change", function(e) {
            if (e.target.files[0].size > 1048576) {
                error.setAttribute("title", "Размер файла превышает 1 Мб");
                file.value = null;
                e.target.files = null;
                error.style.display = "block";
            } else {
                <#if errorMessage??>
                    error.setAttribute("title", "${errorMessage}");
                </#if>
                error.style.display = "none";
            }
        });
    </script>
    <script src="https://kit.fontawesome.com/3303120963.js" crossorigin="anonymous"></script>
</body>
</html>