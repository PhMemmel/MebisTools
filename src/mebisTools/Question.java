package mebisTools;

import java.util.HashMap;
import java.util.Map;

public class Question {

    private String questionText;
    private String questionName;
    private Map<String, String> answers;

    Question(String questionName, String questionText) {
        answers = new HashMap<String,String>();
        this.questionName = questionName;
        this.questionText = questionText;
    }

    void addAnswer(String answerText, String fraction) {
        answers.put(answerText, fraction);
    }

    Map<String, String> getAnswers() {
        return answers;
    }

    String getQuestionText () {
        return questionText;
    }

    String getQuestionName () {
        return questionName;
    }
}
