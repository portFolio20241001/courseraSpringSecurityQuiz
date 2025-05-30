package com.app.quiz.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;

@Data
public class Quiz {
    private Integer id;
    private String questionText;
    private ArrayList<String> options; // Keep options as ArrayList<String>
    private String correctAnswer;
}