package i5.las2peer.services.readerbenchService.AssessmentContent;

import java.util.ArrayList;

public class NLUAssessment extends Assessment {
	
	private String helpIntent;
	private ArrayList<String> intents;
	private ArrayList<String> hints;
	private String type;
	public NLUAssessment(String quitIntent, ArrayList<String> questions, ArrayList<String> intents, ArrayList<String> hints,
	 String helpIntent, String type, ArrayList<String> textref, ArrayList<Double> questionWeight, String modelType,  ArrayList<Double> similarityScore, ArrayList<String> textlevel) {
		super(quitIntent, questions, type, textref, questionWeight, modelType, similarityScore, textlevel );
		this.helpIntent = helpIntent;
		this.intents = intents;
		this.hints = hints;
	}
	
	public String getCorrectAnswerIntent() {
		return this.intents.get(this.getCurrentQuestionNumber());
	}
	
	public String getQuestionHint() {
		return this.hints.get(this.getCurrentQuestionNumber());
	}
	
	public String getHelpIntent() {
		return this.helpIntent;
	}
	
}
