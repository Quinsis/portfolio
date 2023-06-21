package com.example.kursovaya.controllers;

import com.example.kursovaya.models.Data;
import com.example.kursovaya.services.AnalysisService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DownloadController {
    private final AnalysisService analysisService;

    @GetMapping("/upload")
    public ModelAndView uploadFile(Model model) {
        model.addAttribute("errorMessage", null);
        return new ModelAndView("upload");
    }

    @PostMapping("/upload")
    @ExceptionHandler(FileSizeLimitExceededException.class)
    public ModelAndView uploadFileHandler(@RequestParam("file") MultipartFile file, Model model, HttpSession session) throws IOException {
        Object data = analysisService.getDataFromFile(analysisService.handleFile(file));
        if (data instanceof Map) {
            model.addAttribute("data", data);

            // Импортируем данные для графика со средней зарплатой
            Object averageSalaries = analysisService.getAverageSalaries((Map<String, List<Double>>) data);
            if (averageSalaries instanceof Map) {
                session.setAttribute("averageSalaries", averageSalaries);
            } else {
                model.addAttribute("errorMessage", averageSalaries.toString());
            }

            // Импортируем данные для графика с корреляцией и регрессией
            Object doubles = analysisService.getDataAsDoubles((Map<String, List<Double>>) data);
            if (doubles instanceof Data) {
                double correlationEducationSalary = analysisService.correlation(((Data) doubles).educations, ((Data) doubles).salaries);
                double correlationExperienceSalary = analysisService.correlation(((Data) doubles).experiences, ((Data) doubles).salaries);

                session.setAttribute("doubles", doubles);
                session.setAttribute("correlationEducationSalary", correlationEducationSalary);
                session.setAttribute("correlationExperienceSalary", correlationExperienceSalary);

                Map<String, Double> regressionOddsEducationSalary = analysisService.getRegressionOdds(((Data) doubles).educations, ((Data) doubles).salaries);
                String equationLinearRegressionEducationSalary = analysisService.equationLinearRegression(regressionOddsEducationSalary);

                Map<String, Double> regressionOddsExperienceSalary = analysisService.getRegressionOdds(((Data) doubles).experiences, ((Data) doubles).salaries);
                String equationLinearRegressionExperienceSalary = analysisService.equationLinearRegression(regressionOddsExperienceSalary);

                session.setAttribute("regressionOddsEducationSalary", regressionOddsEducationSalary);
                session.setAttribute("equationLinearRegressionEducationSalary", equationLinearRegressionEducationSalary);
                session.setAttribute("approximateEducationSalary", analysisService.approximate(((Data) doubles).educations, ((Data) doubles).salaries, regressionOddsEducationSalary));
                session.setAttribute("significanceEducationSalary", analysisService.significance(correlationEducationSalary, ((Data) doubles).educations.size()));

                session.setAttribute("regressionOddsExperienceSalary", regressionOddsExperienceSalary);
                session.setAttribute("equationLinearRegressionExperienceSalary", equationLinearRegressionExperienceSalary);
                session.setAttribute("approximateExperienceSalary", analysisService.approximate(((Data) doubles).experiences, ((Data) doubles).salaries, regressionOddsExperienceSalary));
                session.setAttribute("significanceExperienceSalary", analysisService.significance(correlationExperienceSalary, ((Data) doubles).experiences.size()));
            } else {
                model.addAttribute("errorMessage", doubles.toString());
            }

            if (model.getAttribute("errorMessage") != null) {
                model.addAttribute("title", "Загрузка файла");
                return new ModelAndView("upload");
            } else {
                return new ModelAndView("redirect:/dashboard");
            }
        } else {
            model.addAttribute("errorMessage", data.toString());
            model.addAttribute("title", "Загрузка файла");
            return new ModelAndView("upload");
        }
    }

    @GetMapping("/dashboard")
    public ModelAndView dashboard(Model model, HttpSession session) {
        Map<Double, Double> averageSalaries = (Map<Double, Double>) session.getAttribute("averageSalaries");
        Data doubles = (Data) session.getAttribute("doubles");

        double correlationEducationSalary = (double) session.getAttribute("correlationEducationSalary");
        Map<String, Double> regressionOddsEducationSalary = (Map<String, Double>) session.getAttribute("regressionOddsEducationSalary");
        String equationLinearRegressionEducationSalary = (String) session.getAttribute("equationLinearRegressionEducationSalary");
        double approximateEducationSalary = (double) session.getAttribute("approximateEducationSalary");
        double significanceEducationSalary = (double) session.getAttribute("significanceEducationSalary");

        double correlationExperienceSalary = (double) session.getAttribute("correlationExperienceSalary");
        Map<String, Double> regressionOddsExperienceSalary = (Map<String, Double>) session.getAttribute("regressionOddsExperienceSalary");
        String equationLinearRegressionExperienceSalary = (String) session.getAttribute("equationLinearRegressionExperienceSalary");
        double approximateExperienceSalary = (double) session.getAttribute("approximateExperienceSalary");
        double significanceExperienceSalary = (double) session.getAttribute("significanceExperienceSalary");

        double kEducationSalary = regressionOddsEducationSalary.get("k").intValue();
        double bEducationSalary = regressionOddsEducationSalary.get("b").intValue();

        double kExperienceSalary = regressionOddsExperienceSalary.get("k").intValue();
        double bExperienceSalary = regressionOddsExperienceSalary.get("b").intValue();

        String xValuesEducationSalary = "";
        String yValuesEducationSalary = "";

        String xValuesExperienceSalary = "";
        String yValuesExperienceSalary = "";

        String averageSalariesLabels = "";
        String averageSalariesValues = "";

        for (Map.Entry<Double, Double> entry : averageSalaries.entrySet()) {
            averageSalariesLabels += entry.getKey() + ",";
            averageSalariesValues += entry.getValue() + ",";
        }
        averageSalariesLabels = averageSalariesLabels.substring(0, averageSalariesLabels.length() - 1);
        averageSalariesValues = averageSalariesValues.substring(0, averageSalariesValues.length() - 1);

        for (int i = 0; i < doubles.educations.size(); i++) {
            xValuesEducationSalary += doubles.educations.get(i) + ",";
            yValuesEducationSalary += doubles.salaries.get(i) + ",";
        }
        for (int i = 0; i < doubles.experiences.size(); i++) {
            xValuesExperienceSalary += doubles.experiences.get(i) + ",";
            yValuesExperienceSalary += doubles.salaries.get(i) + ",";
        }

        xValuesEducationSalary = xValuesEducationSalary.substring(0, xValuesEducationSalary.length() - 1);
        yValuesEducationSalary = yValuesEducationSalary.substring(0, yValuesEducationSalary.length() - 1);

        xValuesExperienceSalary = xValuesExperienceSalary.substring(0, xValuesExperienceSalary.length() - 1);
        yValuesExperienceSalary = yValuesExperienceSalary.substring(0, yValuesExperienceSalary.length() - 1);

        model.addAttribute("averageSalariesLabels", averageSalariesLabels);
        model.addAttribute("averageSalariesValues", averageSalariesValues);

        model.addAttribute("correlationEducationSalary", correlationEducationSalary);
        model.addAttribute("oddsEducationSalary", bEducationSalary + "," + kEducationSalary);
        model.addAttribute("equationLinearRegressionExperienceSalary", equationLinearRegressionEducationSalary);
        model.addAttribute("xValuesEducationSalary", xValuesEducationSalary);
        model.addAttribute("yValuesEducationSalary", yValuesEducationSalary);
        model.addAttribute("conclusionEducationSalary", analysisService.getConclusion(
                correlationEducationSalary,
                doubles.educations.size(),
                equationLinearRegressionEducationSalary,
                approximateEducationSalary,
                significanceEducationSalary, 1)
        );

        model.addAttribute("correlationExperienceSalary", correlationExperienceSalary);
        model.addAttribute("oddsExperienceSalary", bExperienceSalary + "," + kExperienceSalary);
        model.addAttribute("equationLinearRegressionExperienceSalary", equationLinearRegressionExperienceSalary);
        model.addAttribute("xValuesExperienceSalary", xValuesExperienceSalary);
        model.addAttribute("yValuesExperienceSalary", yValuesExperienceSalary);
        model.addAttribute("conclusionExperienceSalary", analysisService.getConclusion(
                correlationExperienceSalary,
                doubles.experiences.size(),
                equationLinearRegressionExperienceSalary,
                approximateExperienceSalary,
                significanceExperienceSalary, 2)
        );
        return new ModelAndView("dashboard");
    }
}
