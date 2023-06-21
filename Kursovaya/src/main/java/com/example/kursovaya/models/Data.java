package com.example.kursovaya.models;

import java.util.List;

public class Data {
    public List<Double> educations;
    public List<Double> experiences;
    public List<Double> salaries;
    public Data(List<Double> educations, List<Double> experiences, List<Double> salaries) {
        this.educations = educations;
        this.experiences = experiences;
        this.salaries = salaries;
    }
}
