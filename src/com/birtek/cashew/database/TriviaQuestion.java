package com.birtek.cashew.database;

import java.util.ArrayList;

public record TriviaQuestion(int id, String question, ArrayList<String> answers, int difficulty, String imageURL) {
}
