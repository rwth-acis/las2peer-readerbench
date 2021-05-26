package i5.las2peer.services.readerbenchService.AssessmentContent;

import java.util.ArrayList;

public abstract class Assessment {
	private String topicName;
	private String quitIntent;
	private double marks;
	private int currentQuestion;
	private String currentWrongQuestions;
	private ArrayList<String> questions; 
	private ArrayList<String> textlevel;
	private ArrayList<Double> similarityScore;
	private ArrayList<String> textReference;
	private String type;
	private ArrayList<Double> numberOfPoints;
	private String modelType;
	private String topicId;
	
	public Assessment(String quitIntent, ArrayList<String> questions) {
		this.quitIntent = quitIntent;
		this.questions = questions;
		this.marks = 0;
		this.currentQuestion = 0;
		this.currentWrongQuestions = "";
	}
	public Assessment(String topicName, String topicId, String quitIntent, ArrayList<String> questions, String type, ArrayList<String> textrefref,  ArrayList<Double> numberOfPoints, 
	String modelType, ArrayList<Double> similarityScore, ArrayList<String> textlevel) {
		this.topicName = topicName;
		this.quitIntent = quitIntent;
		this.questions = questions;
		this.marks = 0;
		this.currentQuestion = 0;
		this.currentWrongQuestions = "";
		this.similarityScore = similarityScore;
		this.textlevel = textlevel;
		this.textReference = textrefref;
		this.type = type;
		this.numberOfPoints =numberOfPoints;
		this.modelType = modelType;
		this.topicId = topicId;
	}

	
	public String getType(){
		return this.type;
	}

	public String gettopicId(){
		return this.topicId;
	}

	public String getTopicName(){
		return this.topicName;
	}

	public String getQuitIntent() {
		return this.quitIntent;
	}
	
	public double getMarks() {
		return this.marks; 
	}
	
	public void incrementMark(double value) {
    	this.marks += (Math.round(value*100.0)/100.0);
    } 
	
	public int getCurrentQuestionNumber() {
		return this.currentQuestion;
	}
	
	public void incrementCurrentQuestionNumber() {
		this.currentQuestion++ ;
	}
	
	public int getAssessmentSize() {
		return this.questions.size();
	} 
	
	public String getWrongQuestions() {
		return this.currentWrongQuestions;	
	}
	
	public void addWrongQuestion(){
	    	this.currentWrongQuestions += questions.get(getCurrentQuestionNumber()) + "\n" ;
	} 
	
	public String getCurrentQuestion() {
		return this.questions.get(this.getCurrentQuestionNumber());
	}
	public Double getCurrentNumberOfPoints() {
		return this.numberOfPoints.get(this.getCurrentQuestionNumber());
	}

	public void setLevel(String level){
		this.textlevel.set(this.getCurrentQuestionNumber(), level);
	}

	public void setSimilarity(double value){
		this.similarityScore.set(this.getCurrentQuestionNumber(), value*this.numberOfPoints.get(this.getCurrentQuestionNumber()));
	}

	

	public ArrayList<Double> getSimilarityScoreList(){
		return this.similarityScore;
	}
	public ArrayList<String> getLevelList(){
		return this.textlevel;
	}
	public String gettextReference(){
		return textReference.get(this.getCurrentQuestionNumber());
	}

	public String getModelType(){
		return this.modelType;
	}
	
	
	
	
}
