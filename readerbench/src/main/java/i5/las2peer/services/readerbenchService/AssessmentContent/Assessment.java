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
	private ArrayList<String> answers;
	private ArrayList<String> refComplexity;
	private ArrayList<String> feedbackText;

	public Assessment(String quitIntent, ArrayList<String> questions) {
		this.quitIntent = quitIntent;
		this.questions = questions;
		this.marks = 0;
		this.currentQuestion = 0;
		this.currentWrongQuestions = "";
	}
	public Assessment(String topicName, String topicId, String quitIntent, ArrayList<String> questions, String type, ArrayList<String> textrefref,  ArrayList<Double> numberOfPoints, 
	String modelType, ArrayList<Double> similarityScore, ArrayList<String> textlevel, ArrayList<String> refComplexity, ArrayList<String> answers, ArrayList<String> feedbackText) {
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
		this.answers = answers;
		this.refComplexity = refComplexity;
		this.feedbackText = feedbackText;
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
	public String getQuestionByNumber(int number) {
		return this.questions.get(number);
	}
	public String getAnswerByNumber(int number) {
		return this.answers.get(number);
	}
	public String getrefComplexityByNumber(int number) {
		return this.refComplexity.get(number);
	}
	public String getCurrentAnswer() {
		return this.questions.get(this.getCurrentQuestionNumber());
	}

	public void setCurrentAnswer(String answer) {
		this.answers.set(this.getCurrentQuestionNumber(), answer);
	}
	public Double getCurrentNumberOfPoints() {
		return this.numberOfPoints.get(this.getCurrentQuestionNumber());
	}

	public void setLevel(String level){
		this.textlevel.set(this.getCurrentQuestionNumber(), level);
	}

	public String getFeedbackText(int i){
		return this.feedbackText.get(i);
	}
	public void setFeedbackText(String feedback){
		this.feedbackText.set(this.getCurrentQuestionNumber(), feedback);
	}
	public void setLevelByNumber(int number, String level){
		this.textlevel.set(number, level);
	}

	public void setSimilarity(double value){
		System.out.println("This CurrentQuestionNumber is..........."+ this.getCurrentQuestionNumber());
		System.out.println("This NumberOfPoints is..........."+ this.getCurrentQuestionNumber());
		this.similarityScore.set(this.getCurrentQuestionNumber(), value);
	}
	public void setSimilarityByNumber(int number,double value){
		this.similarityScore.set(number, value);
	}


	

	public ArrayList<Double> getSimilarityScoreList(){
		return this.similarityScore;
	}
	public String getLevelList(int i){
		return this.textlevel.get(i);
	}
	public String gettextReference(){
		return this.textReference.get(this.getCurrentQuestionNumber());
	}
	public String gettextReferenceByNumber(int number){
		return this.textReference.get(number);
	}
	public String getModelType(){
		return this.modelType;
	}
	
	
	
	
}
