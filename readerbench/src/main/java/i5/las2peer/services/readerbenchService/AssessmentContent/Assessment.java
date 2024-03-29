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
	private ArrayList<String> similarityScore;
	private ArrayList<String> textReference;
	private String type;
	private ArrayList<Double> numberOfPoints;
	private String modelType;
	private String topicId;
	private ArrayList<String> answers;
	private ArrayList<String> refComplexity;
	private ArrayList<String> feedbackText;
	private ArrayList<String> cnaText;
	private int currentFeedback;
	private ArrayList<String> keywordText;
	public Assessment(String quitIntent, ArrayList<String> questions) {
		this.quitIntent = quitIntent;
		this.questions = questions;
		this.marks = 0;
		this.currentQuestion = 0;
		this.currentWrongQuestions = "";
	}
	public Assessment(String topicName, String topicId, String quitIntent, ArrayList<String> questions, String type, ArrayList<String> textrefref,  ArrayList<Double> numberOfPoints, 
	String modelType, ArrayList<String> similarityScore, ArrayList<String> textlevel, ArrayList<String> refComplexity, ArrayList<String> answers, ArrayList<String> feedbackText, ArrayList<String> cnaText, ArrayList<String> keywordText) {
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
		this.currentFeedback=0;
		this.cnaText = cnaText;
		this.keywordText = keywordText;
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
	
	public int getCurrentFeedback() {
		return this.currentFeedback;
	}
	public int getCurrentQuestionNumber() {
		return this.currentQuestion;
	}
	
	public void incrementCurrentQuestionNumber() {
		this.currentQuestion++ ;
	}
	public void incrementgetCurrentFeedback() {
		this.currentFeedback++ ;
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

	
	
	public void setLevelByNumber(int number, String level){
		this.textlevel.set(number, level);
	}

	public void setSimilarity(String value){
		this.similarityScore.set(this.getCurrentQuestionNumber(), value);
	}
	public void setSimilarityByNumber(int number,String value){
		this.similarityScore.set(number, value);
	}

	

	

	public ArrayList<String> getSimilarityScoreList(){
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

	public String getFeedbackText(int i){
		return this.feedbackText.get(i);
	}

	public void setFeedbackText(String feedback){
		this.feedbackText.set(this.getCurrentQuestionNumber(), feedback);
	}
	public void setFeedbackTextByNumber(int number, String feedback){
		this.feedbackText.set(number, feedback);
	}
	
	public String getCnaText(int i){
		return this.cnaText.get(i);
	}
	public void setCnaText(String cna){
		this.cnaText.set(this.getCurrentQuestionNumber(), cna);
	}
	public void setCnaTextByNumber(int number, String cna){
		this.cnaText.set(number, cna);
	}

	public String getKeywordText(int i){
		return this.keywordText.get(i);
	}
	public void setKeywordText(String cna){
		this.keywordText.set(this.getCurrentQuestionNumber(), cna);
	}
	public void setKeywordTextByNumber(int number, String cna){
		this.keywordText.set(number, cna);
	}
	
	
	
	
}
