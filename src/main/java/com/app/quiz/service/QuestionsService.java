package com.app.quiz.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.app.quiz.model.Quiz;

@Service
public class QuestionsService {

	private final Map<Integer, Quiz> questions = new HashMap<>();
	private int nextId = 1;

    public ArrayList<Quiz> getQuizzesList() {
		ArrayList<Quiz> valueList = new ArrayList<>(questions.values());
		return valueList;
	}

	public Quiz getQuizById(int id) {
		return questions.get(Integer.valueOf(id));
	}

	public int getNextId() {
        return nextId++;
    }

	public boolean addQuiz(Quiz quiz) {
		Integer quizId = quiz.getId();

		if (questions.containsKey(quizId)) {
			return false;
		} else {
			questions.put(quizId, quiz);
			return true;
		}
	}

	public boolean editQuiz(Quiz quiz) {
		Integer quizId = quiz.getId();

		if (questions.containsKey(quizId)) {
			questions.put(quizId, quiz);
			return true;
		} else {
			return false;
		}
	}

	public boolean deleteQuiz(int id) {
		Integer quizId = Integer.valueOf(id);
		if (questions.containsKey(quizId)) {
			questions.remove(quizId);
			return true;
		} else {
			return false;
		}
	}

	public int submitQuiz(ArrayList<Quiz> list) {
		int result = 0;
		for (Quiz quiz: list){			
			Quiz quizInList = questions.get(quiz.getId());
			if(quizInList.getCorrectAnswer().equals(quiz.getCorrectAnswer())) {
				result++;
			}
		}
		return result;
	}
}