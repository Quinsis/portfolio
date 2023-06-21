package com.example.kursovaya.services;

import com.example.kursovaya.models.Data;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AnalysisService {
    private static Map<Integer, Double> tList = new HashMap<>();

    public AnalysisService() {
        tList.put(1, 12.706);
        tList.put(2, 4.3027);
        tList.put(3, 3.1825);
        tList.put(4, 2.7765);
        tList.put(5, 2.5706);
        tList.put(6, 2.4469);
        tList.put(7, 2.3646);
        tList.put(8, 2.3060);
        tList.put(9, 2.2622);
        tList.put(10, 2.2281);
        tList.put(11, 2.2010);
        tList.put(12, 2.1788);
        tList.put(13, 2.1604);
        tList.put(14, 2.1447);
        tList.put(15, 2.1314);
        tList.put(16, 2.1199);
        tList.put(17, 2.1098);
        tList.put(18, 2.1009);
        tList.put(19, 2.0930);
        tList.put(20, 2.0859);
        tList.put(21, 2.0796);
        tList.put(22, 2.0738);
        tList.put(23, 2.0686);
        tList.put(24, 2.0638);
        tList.put(25, 2.0595);
        tList.put(26, 2.0555);
        tList.put(27, 2.0518);
        tList.put(28, 2.0484);
        tList.put(29, 2.0452);
    }

    // Прием файла от пользователя
    public List<String> handleFile(MultipartFile file) throws IOException {
        String line;
        List<String> result = new ArrayList<>();
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
        while ((line = fileReader.readLine()) != null) {
            result.add(line);
        }
        return result;
    }

    // Обработка полученных данных в переменные
    public Object getDataFromFile(List<String> handleResults) {
        Map<String, List<Double>> data = new HashMap<>();

        List<String> params = new ArrayList<>();
        String paramsLine = handleResults.get(0).replaceAll(" ", "");
        Pattern patternParams = Pattern.compile("\\w+[,|;]\\w+[,|;]\\w+");
        Matcher matcher = patternParams.matcher(paramsLine);

        if (matcher.find()) {
            String group = matcher.group(0);
            if (group.equals(paramsLine)) {
                params = List.of(paramsLine.split(",|;"));
            }
        }
        if (params.size() == 0) return "Не удалось определить переменные в файле. Строка №1 Значение: (" + paramsLine + ")";

        params.forEach(param -> data.put(param, new ArrayList<>()));

        int resultsSize = handleResults.size();
        int paramsSize = params.size();

        if (resultsSize - 1 < 3) {
            return "Файл содержит малое количество записей. Требуется минимум 3 наблюдения.";
        }

        for (int i = 1; i < resultsSize; i++) {
            String valuesLine = handleResults.get(i).replaceAll(" ", "");;
            Pattern patternValues = Pattern.compile("[-+]?\\d+([,|;][-+]?\\d+([.]\\d+)?)+([,|;][-+]?\\d+([.]\\d+)?)");
            matcher = patternValues.matcher(valuesLine);

            List<String> strings = new ArrayList<>();
            if (matcher.find()) {
                String group = matcher.group(0);
                if (group.equals(valuesLine)) {
                    // Находим числа
                    strings = List.of(valuesLine.split(",|;"));
                }
                if (strings.size() == 0) return "Не удалось определить переменные в файле. Строка №" + (i + 1) + " Значение: (" + valuesLine + ")";

                List<Double> values = new ArrayList<>();
                try {
                    strings.forEach(string -> values.add(Double.valueOf(string)));
                } catch (NumberFormatException e) {
                    return "Файл содержит неверный формат данных: Строка №" + (i + 1) + " Значение: (" + valuesLine + "). Строка должна содержать числовые данные.";
                }
                for (int j = 0; j < values.size(); j++) {
                    if (values.get(j) < 0) {
                        return "Файл содержит строку с отрицательным значением: Строка №" + (i + 1) + " Значение: (" + valuesLine + ")";
                    }
                }

                for (int j = 0; j < paramsSize; j++) {
                    data.get(params.get(j)).add(values.get(j));
                }
            } else {
                return "Не удалось определить переменные в файле. Строка №" + (i + 1) + " Значение: (" + valuesLine + ")";
            }
        }
        return data;
    }

    public Object getDataAsDoubles(Map<String, List<Double>> data) {
        List<Double> educationIds = data.get("Education");
        List<Double> experienceIds = data.get("Experience");
        List<Double> salaryValues = data.get("Salary");

        Data doubles = new Data(educationIds, experienceIds, salaryValues);

        if (educationIds == null) {
            return "Файл не содержит информацию об уровне образования";
        } else if (experienceIds == null) {
            return "Файл не содержит информацию о стаже работы";
        } else if (salaryValues == null) {
            return "Файл не содержит информацию о зарплате";
        }

        for (int i = 0; i < doubles.educations.size() - 1; i++) {
            for (int j = i + 1; j < doubles.educations.size(); j++) {
                if (doubles.educations.get(i) > doubles.educations.get(j)) {
                    Collections.swap(doubles.educations, i, j);
                    Collections.swap(doubles.experiences, i, j);
                    Collections.swap(doubles.salaries, i, j);
                }
            }
        }

        return doubles;
    }

    // Статистическая значимость корреляции
    public double significance(double correlation, int observations) {
        double a = correlation * Math.sqrt(observations - 2);
        double b = Math.sqrt(1 - Math.pow(correlation, 2));
        return a / b;
    }

    // Данные по средней зарплате
    public Object getAverageSalaries(Map<String, List<Double>> data) {
        Map<Double, Double> averageSalaries = new HashMap<>();

        List<Double> educationIds = data.get("Education");
        List<Double> salaryValues = data.get("Salary");

        if (educationIds == null) {
            return "Файл не содержит информацию об уровне образования";
        } else if (salaryValues == null) {
            return "Файл не содержит информацию о зарплате";
        }

        // Заполняем массив зарплатами по каждому из уровней образования
        Map<Double, List<Double>> salaries = new HashMap<>();
        for (int i = 0; i < educationIds.size(); i++) {
            if (!salaries.containsKey(educationIds.get(i))) {
                salaries.put(educationIds.get(i), new ArrayList<>());
            }
            salaries.get(educationIds.get(i)).add(salaryValues.get(i));
        }

        for (Map.Entry<Double, List<Double>> entry : salaries.entrySet()) {
            double averageSalary = 0;
            List<Double> values = entry.getValue();
            for (int i = 0; i < values.size(); i++) {
                averageSalary += values.get(i);
            }
            averageSalary /= values.size();
            averageSalaries.put(entry.getKey(), averageSalary);
        }

        List<Double> keys = new ArrayList(averageSalaries.keySet());
        Collections.sort(keys);

        // создаем пустой `LinkedHashMap` с порядком вставки`
        Map<Double, Double> linkedHashMap = new LinkedHashMap<>();

        for (Double key: keys) {
            linkedHashMap.put(key, averageSalaries.get(key));
        }

        return linkedHashMap;
    }

    // Коэффициент корреляции
    public double correlation(List<Double> x, List<Double> y) {
        double avgX = 0, avgY = 0;

        // Высчитываем средние значения x и y
        for (int i = 0; i < x.size(); i++) {
            avgX += x.get(i);
            avgY += y.get(i);
        }

        avgX /= x.size();
        avgY /= y.size();

        double sumXY = 0;
        for (int i = 0; i < x.size(); i++) {
            sumXY += x.get(i) * y.get(i);
        }
        sumXY /= x.size();

        // Высчитываем среднеквадратическое отклонение
        double sigmaX = standardDeviation(x, avgX);
        double sigmaY = standardDeviation(y, avgY);

        // Высчитывает линейный коэффициент корреляции
        double a = sumXY - (avgX * avgY);
        double b = sigmaX * sigmaY;
        return a / b;
    }

    // Стандартное отклонение
    private double standardDeviation(List<Double> values, double avgValues) {
        int valuesSize = values.size();
        double sum = 0;
        for (int i = 0; i < valuesSize; i++) {
            sum += Math.pow(values.get(i) - avgValues, 2);
        }
        sum /= valuesSize;
        return Math.sqrt(sum);
    }

    // Параметры уравнения линейное регрессии
    public Map<String, Double> getRegressionOdds(List<Double> x, List<Double> y) {
        Map<String, Double> odds = new HashMap<>();
        double avgX = 0, avgY = 0;

        // Высчитываем средние значения x и y
        for (int i = 0; i < x.size(); i++) {
            avgX += x.get(i);
            avgY += y.get(i);
        }

        avgX /= x.size();
        avgY /= y.size();

        // Находим сумму квадратов x
        double sumX2 = 0;
        for (int i = 0; i < x.size(); i++) {
            sumX2 += x.get(i) * x.get(i);
        }

        // Находим сумму произведений x и y
        double sumXY = 0;
        for (int i = 0; i < x.size(); i++) {
            sumXY += x.get(i) * y.get(i);
        }

        // Находим коэффициенты линейной регрессии
        double b = (avgY * sumX2 - avgX * sumXY) / (sumX2 - x.size() * avgX * avgX);
        double k = (sumXY - x.size() * avgX * avgY) / (sumX2 - x.size() * avgX * avgX);

        odds.put("b", b);
        odds.put("k", k);

        return odds;
    }

    // Формула уравнения линейное регрессии
    public String equationLinearRegression(Map<String, Double> odds) {
        if (odds.get("b") > 0) {
            return "y = " + odds.get("k").intValue() + " * x + " + odds.get("b").intValue();
        } else {
            return "y = " + odds.get("k").intValue() + " * x - " + Math.abs(odds.get("b").intValue());
        }
    }

    // Выводы
    public String getConclusion(double correlation, int observationsCount, String equationLinearRegression, double approximate, double significance, int conclusionType) {
        String conclusion = "<b>[1]</b> Коэффициент корреляции равен <b id=\"value\">" + String.format("%.3f", correlation) + "</b><br><br>";
        conclusion += "<b>[2]</b> Уравнение линейное регрессии: <b id=\"value\">" + equationLinearRegression + "</b><br><br>";

        String type1 = "", type2 = "";
        if (conclusionType == 1) {
            type1 = "уровнем образования и зарплатой";
            type2 = "уровень образования, тем выше зарплата";
        } else if (conclusionType == 2){
            type1 = "стажем работы и зарплатой";
            type2 = "стаж работы, тем выше зарплата";
        }

        conclusion += "<b>[3]</b> ";
        if (correlation >= 0.9) {
            conclusion += "Наблюдается очень высокая положительная связь между " + type1 + ". ";
        } else if (correlation > 0.7) {
            conclusion += "Наблюдается высокая положительная связь между " + type1 + ". ";
        } else if (correlation > 0.5) {
            conclusion += "Наблюдается умеренная положительная связь между " + type1 + ". ";
        } else if (correlation > 0.2) {
            conclusion += "Наблюдается слабая положительная связь между " + type1 + ". ";
        } else if (correlation > 0) {
            conclusion += "Наблюдается очень слабая положительная связь между " + type1 + ".";
        }

        if (correlation > 0) {
            conclusion += "<br><br><b>[4]</b> В среднем, чем выше " + type2 + ".";
        } else if (correlation <= -0.9) {
            conclusion += "Наблюдается очень высокая обратная связь между " + type1 + ". ";
        } else if (correlation < -0.7) {
            conclusion += "Наблюдается высокая обратная связь между " + type1 + ". ";
        } else if (correlation < -0.5) {
            conclusion += "Наблюдается умеренная обратная связь между " + type1 + ". ";
        } else if (correlation < -0.2) {
            conclusion += "Наблюдается слабая обратная связь между " + type1 + ". ";
        } else if (correlation < 0) {
            conclusion += "Наблюдается очень слабая обратная связь между " + type1 + ". ";
        }

        if (correlation < 0) {
            conclusion += "<br><br><b>[4]</b> В среднем, чем выше " + type2 + ".";
        }

        conclusion += "<br><br><b>[5]</b> Количество наблюдений равно <b id=\"value\">" + observationsCount + "</b>";
        conclusion += "<br><br><b>[6]</b> Средняя ошибка коэффициента корреляции: <b id=\"value\">" + String.format("%.1f", CIS(correlation, observationsCount) * 100) + "%</b>";
        conclusion += "<br><br><b>[7]</b> Относительная ошибка аппроксимации: <b id=\"value\">" + String.format("%.1f", approximate * 100) + "%</b>";
        conclusion += "<br><br><b>[8]</b> Статистическая значимость корреляции: <b id=\"value\">" + String.format("%.3f", significance) + "</b>";
        conclusion += "<br><br><b>[9]</b> Значение t-критерия для уровня значимости <b id=\"value\">" + 0.05 + "</b> и степени свободы <b id=\"value\">" + (observationsCount - 2) + "</b> равно <b id=\"value\">";
        double t;

        if ((observationsCount - 2) > 120) t = 1.96;
        else if ((observationsCount - 2) == 120)  t = 1.9799;
        else if ((observationsCount - 2) >= 60)  t = 2.0003;
        else if ((observationsCount - 2) >= 40)  t = 2.0211;
        else if ((observationsCount - 2) >= 30)  t = 2.0423;
        else t = tList.get(observationsCount - 2);

        conclusion += t + "</b>. Следовательно, выявленная корреляционная связь ";
        if (significance > t) {
            conclusion += "<b>статистически значима</b>.";
        } else {
            conclusion += "<b>статистически незначима</b>.";
        }

        return conclusion;
    }

    // Средняя ошибка коэффициента корреляции
    public double CIS(double correlationCoefficient, int observationsCount) {
        if (observationsCount > 50) {
            double a = 1.0 - Math.pow(correlationCoefficient, 2);
            double b = Math.sqrt(observationsCount);
            return a / b;
        } else {
            return Math.sqrt(1 - Math.pow(correlationCoefficient, 2)) / Math.sqrt(observationsCount - 2);
        }
    }

    // Относительная ошибка аппроксимации
    public double approximate(List<Double> x, List<Double> y, Map<String, Double> odds) {
        List<Double> predictY = new ArrayList<>();
        for (int i = 0; i < x.size(); i++) {
            predictY.add(x.get(i) * odds.get("k") + odds.get("b"));
        }

        List<Double> deviationY = new ArrayList<>();
        for (int i = 0; i < y.size(); i++) {
            deviationY.add(Math.abs(y.get(i)  - predictY.get(i)) / y.get(i));
        }

        double deviationSum = 0;
        for (int i = 0; i < deviationY.size(); i++) {
            deviationSum += deviationY.get(i);
        }

        return deviationSum / y.size();
    }
}
