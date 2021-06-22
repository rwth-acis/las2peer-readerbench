package i5.las2peer.services.readerbenchService;



import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.*;
import java.lang.*;
import java.io.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

import java.util.HashSet;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Blob;
import java.sql.ResultSet;

import com.google.gson.Gson;

import i5.las2peer.api.Context;
import i5.las2peer.api.security.UserAgent;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;

import i5.las2peer.api.persistency.Envelope;
import i5.las2peer.api.persistency.EnvelopeAccessDeniedException;
import i5.las2peer.api.persistency.EnvelopeNotFoundException;
import i5.las2peer.api.persistency.EnvelopeOperationFailedException;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import static java.nio.charset.StandardCharsets.*;
import java.nio.charset.Charset;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import i5.las2peer.services.readerbenchService.AssessmentContent.*;
import i5.las2peer.services.readerbenchService.chat.ChatMessage;
import i5.las2peer.services.readerbenchService.database.SQLDatabase;
import i5.las2peer.services.readerbenchService.database.SQLDatabaseType;
import i5.las2peer.services.readerbenchService.model.MessageInfo;
import i5.las2peer.services.readerbenchService.nlu.Intent;
import i5.las2peer.services.readerbenchService.chat.ChatMessage;

// TODO Describe your own service
/**
 * las2peer-Template-Service
 * 
 * This is a template for a very basic las2peer service that uses the las2peer WebConnector for RESTful access to it.
 * 
 * Note: If you plan on using Swagger you should adapt the information below in the SwaggerDefinition annotation to suit
 * your project. If you do not intend to provide a Swagger documentation of your service API, the entire Api and
 * SwaggerDefinition annotation should be removed.
 * 
 */
// TODO Adjust the following configuration
@Api
@SwaggerDefinition(
		info = @Info(
				title = "las2peer Readerbench Service",
				version = "1.0.0",
				description = "A las2peer Template Service for Readerbench purposes.",
				contact = @Contact(
						name = "Karly Zeufack",
						url = "https://las2peer.org",
						email = "karl.zeufack@rwth-aachen.de"),
				license = @License(
						name = "BSD-3",
						url = "https://github.com/Karlydiamond214/las2peer-readerbench")))
@ServicePath("/readerbench")
@ManualDeployment
public class ReaderbenchService extends RESTService {
	

	private final static List<String> SUPPORTED_FUNCTIONS = Arrays.asList("textual coomplexity",
			"Sentiment");

	private static HashMap<String, Object> ContextInfo = new HashMap<String, Object>();

	
	// Used for keeping context between assessment and non-assessment states
    // Key is the channelId
    private static HashMap<String, String> assessmentStarted = new HashMap<String, String>();
   // Used to keep track if the topics were already given for a specific user. The assessment function first gives a list on available topics and then expects an answer. 
    private static HashMap<String, Boolean> topicsProposed = new HashMap<String, Boolean>();  
    // Used to make sure that the same moodle quiz is not being started twice at the same time. You can only start a quiz once on moodle until you submit it. 
    private static HashMap<String, Boolean> topicProcessed = new HashMap<String, Boolean>();
    // Saves the current NLUAssessment object for a specific user
    private static HashMap<String, NLUAssessment> currentNLUAssessment = new HashMap<String, NLUAssessment>();
    // Saves the current Moodle Assessment for a specific user
    private static HashMap<String, MoodleQuiz> currentMoodleAssessment = new HashMap<String, MoodleQuiz>();
    // Keep track of the related channels to a bot. Needed to reset the assessments once a bot gets restarted.
	private static HashMap<String, ArrayList<String>> botChannel = new HashMap<String, ArrayList<String>>();
	
	private static ArrayList<String> Assessment = new ArrayList<String>();

	// Used to keep track  the topics that were already given and executed for a specific user. The assessment function first gives a list on available topics and then expects an answer. 
	private static ArrayList<String> proposedTopic = new ArrayList<String>();

	private static ArrayList<String> processedTopic = new ArrayList<String>();

	private String databaseName;
	private int databaseTypeInt = 1; // See SQLDatabaseType for more information
	private SQLDatabaseType databaseType;
	private String databaseHost;
	private int databasePort;
	private String databaseUser;
	private String databasePassword;
	private SQLDatabase database; // The database instance to write to.
	private String readerbenchEndpoint="http://rb-controller.ma-zeufack:32446";
	private String l2pEndpoint = "http://137.226.232.75:32445";


	public ReaderbenchService(){
		super();
		setFieldValues();
		System.out.println("Fields Values: " + this.databaseName + this.databaseHost);
		this.databaseType = SQLDatabaseType.getSQLDatabaseType(databaseTypeInt);
		System.out.println(this.databaseType +" " +  this.databaseUser +" " +  this.databasePassword+ " " + this.databaseName + " "
	+			this.databaseHost + " " +this.databasePort);
		this.database = new SQLDatabase(this.databaseType, this.databaseUser, this.databasePassword, this.databaseName,
				this.databaseHost, this.databasePort);
		try {
			Connection con = database.getDataSource().getConnection();
			con.close();
		} catch (SQLException e) {
			System.out.println("Failed to Connect: " + e.getMessage());
		}

	}
	
	/**
	 * Template of a get function.
	 * 
	 * @return Returns an HTTP response with the username as string content.
	 */
	@GET
	@Path("/get")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(
			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "REPLACE THIS WITH YOUR NOTES TO THE FUNCTION")
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	public Response getTemplate() {
		UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
		String name = userAgent.getLoginName();
		System.out.println("readerbenchEndpoint: " + this.readerbenchEndpoint);
		return Response.ok().entity(name).build();
	}


	@GET
	@Path("/getRbStatus")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(
			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "REPLACE THIS WITH YOUR NOTES TO THE FUNCTION")
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	public Response getRbStatus() {
		JSONObject j = new JSONObject();
		System.out.println("Breakpoint--------getRbStatus from Service--------------------------");
		try {
			//Creating a HttpClient object
			CloseableHttpClient httpclient = HttpClients.createDefault();
			//Creating a HttpGet object
			HttpGet httpget = new HttpGet(this.readerbenchEndpoint+"/api/v1/isalive");

			//Printing the method used
			System.out.println("Request Type: "+ httpget.getMethod());

			//Executing the Get request
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity);
			JSONObject j1 = new JSONObject();
			j1.put("text", result);
			j1.put("closeContext", true);
			return Response.ok().entity(j1).build();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path("/getAssessment")
	@Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
	@ApiOperation(
		value = "",
		notes = "")
	@ApiResponses(
		value = {@ApiResponse(
			code = HttpURLConnection.HTTP_OK,
			message = "Assessement deleted"
		)}
	)
	public Response getAssessment(){
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
		Connection con = null;
		PreparedStatement ps = null;
		Response resp = null;

		Connection con2 = null;
		PreparedStatement ps2 = null;
		Response resp2 = null;

		JSONArray Topics = new JSONArray();

		try {
			con = database.getDataSource().getConnection();
			ps = con.prepareStatement("SELECT * FROM topic1");
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				String topic_id = rs.getString("topic_id");
				JSONArray Questions = new JSONArray();

				int columns = rs.getMetaData().getColumnCount();
				JSONObject obj = new JSONObject();
		
				for (int i = 0; i < columns; i++)
					obj.put(rs.getMetaData().getColumnLabel(i + 1).toLowerCase(), rs.getObject(i + 1));
		
				
				try {
					con2 = database.getDataSource().getConnection();
					ps2 = con2.prepareStatement("SELECT * FROM question1 WHERE topic_id = ?");
					ps2.setString(1, topic_id);
					ResultSet rs2 = ps2.executeQuery();
					while(rs2.next()) {

						columns = rs.getMetaData().getColumnCount();
						JSONObject obj2 = new JSONObject();
						for (int i = 0; i < columns; i++)
							obj2.put(rs2.getMetaData().getColumnLabel(i + 1).toLowerCase(), rs2.getObject(i + 1));
			
						Questions.add(obj2);
					}
				}catch (SQLException e) {
					e.printStackTrace();
					resp = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
				} finally {
					try {
						if (ps != null)
							ps.close();
					} catch (Exception e) {
					}
					;
					try {
						if (con != null)
							con.close();
					} catch (Exception e) {
					}
					;
				}
				
				
				obj.put("question",Questions);
				Topics.add(obj);
			}
			
			
		}catch (SQLException e) {
			e.printStackTrace();
			resp = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (Exception e) {
			}
			;
			try {
				if (con != null)
					con.close();
			} catch (Exception e) {
			}
			;
		}
		return Response.ok().entity(Topics).build();
		//return Response.ok().entity("Topic data deleted.").build();
	}
	
	//TODO
	@POST
	@Path("/deleteAssessment")
	@Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
	@ApiOperation(
		value = "",
		notes = "")
	@ApiResponses(
		value = {@ApiResponse(
			code = HttpURLConnection.HTTP_OK,
			message = "Assessement deleted"
		)}
	)
	public Response deleteAssessment(String body){
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
		return Response.ok().entity("Topic data deleted.").build();
	}

	@POST
	@Path("/insertAssessment")
	@Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
	@ApiOperation(
		value = "",
		notes = "")
	@ApiResponses(
		value = {@ApiResponse(
			code = HttpURLConnection.HTTP_OK,
			message = "Assessement inserted"
		)}
	)
	public Response insertAssessment(String body){
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
		try {
			JSONObject bodyJson = (JSONObject) p.parse(body);
			System.out.println(bodyJson);
			System.out.println("+++++++++++++++++++++++++++++++++++System.out.println(bodyJson);");
			Assessment.add(bodyJson.toString());
			System.out.println("+++++++++++++++++++++++++++++++++++assessment.add(bodyJson.toString());");
			Connection con = null;
			PreparedStatement ps = null;
			Response resp = null;
			try {
				// Open database connection
				con = database.getDataSource().getConnection();
				
				// Check if data with given name already exists in database. If yes, update it. Else, insert it
				String topicName = bodyJson.getAsString("topicName");
				String time =""+ System.currentTimeMillis();
				ps = con.prepareStatement("SELECT * FROM topic WHERE topic_id = ?");
				ps.setString(1, topicName + time);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					return Response.ok("Assessment not inserted").build();
				} else {
					ps.close();
					ps = con.prepareStatement("INSERT INTO topic(topic_id, topic_name,processed ,due_date) VALUES (?, ?, ?, ?)");
					ps.setString(1,  bodyJson.getAsString("topicName")+ time);
					ps.setString(2,  bodyJson.getAsString("topicName"));
					ps.setBoolean(3, false);
					ps.setString(4, "");
					ps.executeUpdate();
				}

				
				JSONArray Questions =(JSONArray) bodyJson.get("question");
		
		
				int length = Questions.size(); 
				System.out.println("Got"+ length +"Questions");
				String[][] assessmentContent = new String[length][3];
				for(int i = 0; i < length ; i++){
					
						String question = Questions.get(i).toString();  
						JSONObject questionJson = (JSONObject) p.parse(question);   
					JSONObject complexity_Body = new JSONObject();
					complexity_Body.put("language", "de");
					complexity_Body.put("text", replaceUmlaut(questionJson.getAsString("textref")));

								System.out.println("................Started to compute expert complexity................");	
								StringEntity entity = new StringEntity(complexity_Body.toString());
								HttpClient httpClient = HttpClientBuilder.create().build();
								HttpPost request = new HttpPost(this.readerbenchEndpoint+"/api/v1/expert-complexity");
								request.setEntity(entity);
								HttpResponse res = httpClient.execute(request);
								HttpEntity entity2 = res.getEntity();
								String complexity_result = EntityUtils.toString(entity2);
								System.out.println("................expert Complexity computed from readerbench................");
								JSONObject result = (JSONObject) p.parse(complexity_result); 
								String Expert_Comp =  result.getAsString("data");
								System.out.println(Expert_Comp);
								ps.close();
								ps = con.prepareStatement("INSERT INTO question(question, topic_id, textref, complexity, numberOfPoints) VALUES (?, ?, ?, ?, ?)");
								ps.setString(1, questionJson.getAsString("question"));
								ps.setString(2, bodyJson.getAsString("topicName")+ time);
								ps.setString(3, questionJson.getAsString("textref"));
								ps.setString(4, Expert_Comp);
								ps.setString(5, "1");
								ps.executeUpdate();

					
				} 
				System.out.println("................Topic data stored.................");
				return Response.ok().entity("Topic data stored.").build();
				
			} catch (SQLException e) {
				e.printStackTrace();
				return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
			} catch (Exception e) {
				e.printStackTrace();
				return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
			} finally {
				try {
					if (ps != null)
						ps.close();
				} catch (Exception e) {
				}
				;
				try {
					if (con != null)
						con.close();
				} catch (Exception e) {
				}
				;
			}


		} catch (ParseException e) {
			e.printStackTrace();
			return Response.ok("Assessment not inserted").build();
		}
	}

	@POST
	@Path("/nluAssessmentDE")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(
			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "REPLACE THIS WITH YOUR NOTES TO THE FUNCTION")
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	public Response nluAssessmentDe(String body) {
		System.out.print("body....."+ body);
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
		try {
			JSONObject bodyJson = (JSONObject) p.parse(body);		
			System.out.println(bodyJson);
			JSONObject response = new JSONObject();
			String channel = bodyJson.getAsString("channel");
			
			try {
				ArrayList<String> channels =  botChannel.get(bodyJson.getAsString("botName"));
				channels.add(channel);
				botChannel.put(bodyJson.getAsString("botName"), channels);
			} catch (Exception e) {
				ArrayList<String> channels = new ArrayList<String>();
				channels.add(channel);
				botChannel.put(bodyJson.getAsString("botName"), channels);
			}
			if(this.assessmentStarted.get(channel) == null){
					JSONObject contentJson;
					if(this.topicsProposed.get(channel) == null) {
						String topicNames="";
						int topicNumber = 1;
						
						Connection con = null;
						PreparedStatement ps = null;
						Response resp = null;
						try {
							// Open database connection
							con = database.getDataSource().getConnection();
							
							ps = con.prepareStatement("SELECT * FROM topic");
							ResultSet rs = ps.executeQuery();
							
							int index=0;
							while(rs.next()) {
								topicNames += " • " + topicNumber + ". " + rs.getString("topic_name") + "\n";
								topicNumber++;
								index++;
								System.out.println("................Topic"+" founded.................");
							}	
							System.out.println("................Matching Topics founded  data stored.................");
						} catch (SQLException e) {
							e.printStackTrace();
							resp = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
						} finally {
							try {
								if (ps != null)
									ps.close();
							} catch (Exception e) {
							}
							;
							try {
								if (con != null)
									con.close();
							} catch (Exception e) {
							}
							;
						}
						
						if(!topicNames.equals("")) {
							response.put("text", "Wähle ein Thema indem du mit der entsprechenden Nummer oder dem entsprechenden Name antwortest:\n" + topicNames);
							response.put("closeContext", false);
							this.topicsProposed.put(channel, true);
							return Response.ok().entity(response).build();
						}
						JSONObject error = new JSONObject();
						error.put("text", "Derzeit sind keine Themen verfügbar, versuche zu einem späteren Zeitpunkt wieder!");
						error.put("closeContext", "true");
						return Response.ok().entity(error).build();
					} else {
						if(bodyJson.getAsString("intent").equals("stopAssessment")){
							response.put("text", "Okay Aufwiedersehen :)");
							response.put("closeContext", false);
							this.topicsProposed.remove(channel);
							return Response.ok().entity(response).build();
						}
						String chosenTopicNumber = bodyJson.getAsString("msg").split("\\.")[0];
						String similarNames = "";
				        ArrayList<String> similarTopicNames = new ArrayList<String>();
				        String smiliarNames = "";
						int topicCount = 1;
						Connection con = null;
						PreparedStatement ps = null;
						Response resp = null;

						try {
							// Open database connection
							con = database.getDataSource().getConnection();
							
							
							ps = con.prepareStatement("SELECT * FROM topic ");
							ResultSet rs = ps.executeQuery();
							String topicName="";
							String topicId="";
							while(rs.next()) {
								topicName =  rs.getString("topic_name");
								topicId =  rs.getString("topic_id");
								
								if(topicName.toLowerCase().equals(bodyJson.getAsString("msg").toLowerCase()) || chosenTopicNumber.equals(String.valueOf(topicCount))){
									
									System.out.println("line 641.................");
									similarTopicNames.add(topicName);
									similarNames += " • " + topicCount + ". " + topicName + "\n";
								} 
								topicCount++;
							}
							if(similarTopicNames.size() == 1) {
								System.out.println("similarTopicNames.size() is one.................");
								setUpNluAssessment2(topicName, topicId, channel,  bodyJson.getAsString("Type"), bodyJson.getAsString("modelType"));
								response.put("text", "Wir starten jetzt das Nlu Assessment über "+ topicName + " :)!\n" + this.currentNLUAssessment.get(channel).getCurrentQuestion());							
								response.put("closeContext", "false");
								this.topicsProposed.remove(channel);
								this.assessmentStarted.put(channel, "true");
								bodyJson.put("msg", similarTopicNames.get(0));
							} else if(similarTopicNames.size() > 1) {
								System.out.print("mehrere NLU.................");
								response.put("text", "Mehrere Nlu Assessments entsprechen deiner Antwort, welche von diesen möchtest du denn anfangen?\n" + similarNames);							
								response.put("closeContext", "false");
							}else if(similarTopicNames.size()< 1){
								response.put("text", "Assessment mit der name " + bodyJson.getAsString("msg")+ " wurde nicht gefunden");
								response.put("closeContext", "true");
							}
							return Response.ok().entity(response).build();
							
							
						} catch (SQLException e) {
							e.printStackTrace();
							resp = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
						} finally {
							try {
								if (ps != null)
									ps.close();
								} catch (Exception e) {
								}
								;
								try {
									if (con != null)
										con.close();
								} catch (Exception e) {
								}
								;
						}
						
					}
				
				

			} else {
				System.out.println(bodyJson.getAsString("intent"));
				return Response.ok().entity(continueAssessment2(channel, bodyJson.getAsString("intent"), bodyJson, "NLUAssessmentDe")).build();
			}		
			
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		JSONObject error = new JSONObject();
		error.put("text", "Something went wrong");
		error.put("closeContext", "true");
		return Response.ok().entity(error).build();

	}

	private void setUpNluAssessment2(String topicName, String topicId , String channel, String type, String modelType) throws ParseException{
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
		Connection con = null;
		PreparedStatement ps = null;
		Response resp = null;
		try {
			// Open database connection
			con = database.getDataSource().getConnection();
			
			ps = con.prepareStatement("SELECT * FROM question WHERE topic_id = ?");
			ps.setString(1, topicId);
			ResultSet rs = ps.executeQuery();
			int length =0;
			while(rs.next()) {
				length++;
			}
			rs = ps.executeQuery();
			// Fetch all model names in the database
			String[][] assessmentContent = new String[length][3];
			//String[][] assessmentContent = new String[length][3];
			int index=0;
			while(rs.next()) {
				assessmentContent[index][0] = rs.getString("question");
				assessmentContent[index][1] = rs.getString("textref"); 
				assessmentContent[index][2] = rs.getString("complexity");
				index++;
			}
			//Arrays.sort(assessmentContent, (a, b) -> Integer.compare(Integer.parseInt(a[0]), Integer.parseInt(b[0])));
			ArrayList<String> questions = new ArrayList<String>();
			ArrayList<String> lectureref = new ArrayList<String>();
			ArrayList<Double> numberOfPoints = new  ArrayList<Double>();
			ArrayList<Double> similarityScore = new  ArrayList<Double>();
			ArrayList<String> textlevel = new ArrayList<String>();
			ArrayList<String> answers = new ArrayList<String>();
			ArrayList<String> refComplexity = new ArrayList<String>();
			ArrayList<String> feedbackText = new ArrayList<String>();

			for(int i = 0; i < length ; i++){
				questions.add(assessmentContent[i][0]);
				lectureref.add(replaceUmlaut(assessmentContent[i][1]));
				//numberOfPoints.add(Double.parseDouble(assessmentContent[i][2]));
				numberOfPoints.add(Double.parseDouble("1"));
				similarityScore.add(0.0);
				textlevel.add("");
				answers.add("");
				refComplexity.add(assessmentContent[i][2]);
				feedbackText.add("");
			}
			NLUAssessment assessment = new NLUAssessment(topicName, topicId, "stopAssessment", questions, null, null, "help", type, lectureref, numberOfPoints, modelType, 
				similarityScore, textlevel, refComplexity, answers, feedbackText);
			this.currentNLUAssessment.put(channel, assessment);	
		} catch (SQLException e) {
			e.printStackTrace();
			resp = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (Exception e) {
			}
			;
			try {
				if (con != null)
					con.close();
			} catch (Exception e) {
			}
			;
		}
	}
	

	private JSONObject continueAssessment2(String channel, String intent, JSONObject triggeredBody, String assessmentType){
		JSONObject response = new JSONObject();
		JSONObject error = new JSONObject();
		String answer = ""; 
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
    	response.put("closeContext", "false");
        if(assessmentType.equals("NLUAssessmentDe")) {
			NLUAssessment assessment = this.currentNLUAssessment.get(channel);
				if(intent.equals(assessment.getQuitIntent())){
					answer += "Das Assessment wurde verlassen, \n Du kannst wieder von vorne beginnen. " ;
					this.currentNLUAssessment.remove(channel);
					this.assessmentStarted.put(channel, null);
					response.put("closeContext", "true");
				} else if(intent.equals(assessment.getHelpIntent())){
					answer+= assessment.getQuestionHint() + "\n";
					response.put("closeContext", "false");
				} else if(triggeredBody.getAsString("msg").equals("status") & intent.equals("status")){
					try {
						
						answer += "Ihre Ergebnisse:\n";
						int currentQuestion = 0;

						for(int i=0; i <  assessment.getAssessmentSize(); i++){
							if(assessment.getLevelList(i)==""){
								answer="Bewertungen werden berechnet...";
								response.put("closeContext", "false");
								response.put("text", answer);
        						return response;
							}
							answer += "Frage " + (currentQuestion + 1) + ": Komplexitätstufe is " + assessment.getLevelList(i) +
							", Ähnlichkeitsgrad mit der Korrektur	"+ Math.round(assessment.getSimilarityScoreList().get(i)*100.0)/100.0 + "\n";
							answer +="Empfehlungen: \n";
							answer+= assessment.getFeedbackText(i);
							currentQuestion += 1;
							System.out.println("level is............"+ assessment.getLevelList(i));
						}
						response.put("closeContext", "false");
					} catch (Exception e) {
						e.printStackTrace();
						answer+="Bewertungen werden berechnet...";
						response.put("closeContext", "false");
					}
					
				} else {
					String msg = triggeredBody.getAsString("msg");
					System.out.println("..............question number..."+ assessment.getCurrentQuestionNumber());
					System.out.println("..............assessment.getAssessmentSize()..."+ assessment.getAssessmentSize());
					assessment.setCurrentAnswer(msg);

					

					//ClientResponse SimilarityResult = client.sendRequest("POST", "readerbench/"  + "post/textual_similarity", MediaType.APPLICATION_JSON, headers);
					//Assert.assertEquals(200, SimilarityResult.getHttpCode());
					//ClientResponse ComplexityResult = client.sendRequest("POST", "readerbench/"  + "post/textual_complexity", MediaType.APPLICATION_JSON, headers);
					//Assert.assertEquals(200, ComplexityResult.getHttpCode());
					//TODO: Compute the Assessments
					
					
					if(assessment.getCurrentQuestionNumber()+1 >= assessment.getAssessmentSize()){
						
						for(int i=0; i <  assessment.getAssessmentSize(); i++){
							String ref = assessment.gettextReferenceByNumber(i);
							String ans = assessment.getAnswerByNumber(i);
							String refComplexity = assessment.getrefComplexityByNumber(i) ;
							System.out.println("................ref................");
							System.out.println(ref);
							System.out.println("................answer................");
							System.out.println(ans);
							JSONArray texts = new JSONArray(); 
							texts.add(ref);
							texts.add(ans);
							JSONObject similarity_Body = new JSONObject();
							similarity_Body.put("texts", texts);
							similarity_Body.put("language", "de");
							similarity_Body.put("corpus", "wiki");
							JSONObject complexity_Body = new JSONObject();
							complexity_Body.put("language", "de");
							complexity_Body.put("text", ans);
							JSONObject compare_Body = new JSONObject();
							compare_Body.put("language", "de");
							compare_Body.put("text", ans);
							compare_Body.put("expert", refComplexity);

							try{
								String email = triggeredBody.getAsString("email");
								JSONObject context = getContext(email, p);	
								StringEntity entity = new StringEntity(compare_Body.toString());
								HttpClient httpClient = HttpClientBuilder.create().build();
								HttpPost request = new HttpPost(this.readerbenchEndpoint+"/api/v1/complexity-compare");
								request.setEntity(entity);
								HttpResponse res = httpClient.execute(request);
								HttpEntity entity2 = res.getEntity();
								String similarity_result = EntityUtils.toString(entity2);
								context.put("compare_result", similarity_result);
								ContextInfo.put(email, context);
								System.out.println("................result SIMILARITY computed from readerbench................");
								System.out.println(context.getAsString("compare_result"));
								JSONObject result = (JSONObject) p.parse(context.getAsString("compare_result")); 
								String feedbackString ="";
								JSONObject data = (JSONObject) result.get("data");

								String level = data.getAsString("level");
								assessment.setLevelByNumber(i,level);
								JSONObject feedback = (JSONObject) data.get("feedback");
								

								JSONArray document = (JSONArray) p.parse(feedback.getAsString("document"));

								for (Object item : document) {
									JSONObject obj = (JSONObject) item;
									feedbackString  += obj.getAsString("message")+ "\n \n";
								}

								JSONArray block = (JSONArray) p.parse(feedback.getAsString("block"));
								int blockNumber = 1;
								for (Object item : block) {
									JSONArray arrayitem = (JSONArray) item;
									for (Object item1 : arrayitem) {
										JSONObject obj = (JSONObject) item1;
										feedbackString += "Absatznummer " + blockNumber +": ";
										feedbackString += obj.getAsString("message")+ "\n";
									}
									blockNumber+=1;
								}
								JSONArray sentence = (JSONArray) p.parse(feedback.getAsString("sentence"));
								int sentenceNumber = 1;
								for (Object item : sentence) {
									JSONArray arrayitem = (JSONArray) item;
									for (Object item1 : arrayitem) {
										JSONObject obj = (JSONObject) item1;
										feedbackString += "Satznummer " + sentenceNumber +": ";
										feedbackString += obj.getAsString("message")+ "\n";
									}
									sentenceNumber+=1;
								}

								assessment.setFeedbackText(feedbackString);
							}  catch (Exception e) {
								e.printStackTrace();
								System.out.println("................Problem with Compare................");
								error.put("text", "Readerbench scheint ein Problem zu haben\n Bitte wendest dich an deinem Tutor");
								error.put("closeContext", true);
								return error;
							}
							
							try {
								String email = triggeredBody.getAsString("email");
								JSONObject context = getContext(email, p);	
								StringEntity entity = new StringEntity(similarity_Body.toString());
								HttpClient httpClient = HttpClientBuilder.create().build();
								HttpPost request = new HttpPost(this.readerbenchEndpoint+"/api/v1/text-similarity");
								request.setEntity(entity);
								HttpResponse res = httpClient.execute(request);
								HttpEntity entity2 = res.getEntity();
								String similarity_result = EntityUtils.toString(entity2);
								context.put("similarity_result", similarity_result);
								ContextInfo.put(email, context);
								System.out.println("................result SIMILARITY computed from readerbench................");
								System.out.println(context.getAsString("similarity_result"));
								JSONObject result = (JSONObject) p.parse(context.getAsString("similarity_result")); 
								JSONObject data = (JSONObject) result.get("data");
								JSONArray pairs = (JSONArray) data.get("pairs");
								
								JSONObject pair = (JSONObject) pairs.get(0);
								JSONArray scores = (JSONArray) pair.get("scores");
								Double score =  Double.parseDouble( ((JSONObject) scores.get(0)).getAsString("score"));
								System.out.println("Score 0 is ...... "+ ((JSONObject) scores.get(0)).getAsString("score"));
								System.out.println("Score is ...... "+ score);
								assessment.setSimilarityByNumber(i,score);
								
							} catch (Exception e) {
								e.printStackTrace();
								System.out.println("................Problem with Similarity................");
								error.put("text", "Readerbench scheint ein Problem zu haben\n Bitte wendest dich an deinem Tutor");
								error.put("closeContext", true);
								return error;
							}			
							
						}
					
						try {
							MiniClient client = new MiniClient();
							client.setConnectorEndpoint(this.l2pEndpoint);
							HashMap<String, String> headers = new HashMap<String, String>();
							Intent intent2 = new Intent("status", "status", "status");
							Gson gson = new Gson();
							MessageInfo m = gson.fromJson(triggeredBody.toString(), MessageInfo.class);
							ChatMessage message = m.getMessage();
							message.setText("status");	
							MessageInfo messageInfo = new MessageInfo(message, intent2, "NluAssessmentDE",  "readerbot",  "readerbench", true );
							ClientResponse result = client.sendRequest("POST", "SBFManager" + "{readerbot}/trigger/intent", gson.toJson(messageInfo),
							MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, headers);
							System.out.println(result.getResponse());
						} catch (Exception e) {
							e.printStackTrace();
						}	
						
					/*try {
						
						answer += "Ihre Ergebnisse:\n";
						int currentQuestion = 0;
						for(int i=0; i <  assessment.getAssessmentSize(); i++){
							if(assessment.getLevelList(i)==""){
								answer="Bewertungen werden berechnet...";
								response.put("closeContext", "false");
								response.put("text", answer);
        						return response;
							}
							answer += "Frage " + (currentQuestion + 1) + ": Komplexitätstufe is " + assessment.getLevelList(i) +
							", Ähnlichkeitsgrad mit der Korrektur	"+  Math.round(assessment.getSimilarityScoreList().get(i)*100.0)/100.0 + "\n"; 
							currentQuestion += 1;
							System.out.println("level is............"+ assessment.getLevelList(i));
						}
						response.put("closeContext", "false");
					} catch (Exception e) {
						e.printStackTrace();
						answer+="Fehler bei dem auslesen der Ergebnisse. \n Die Übung wird verlassen.\n Entschuldigung dafür\n Auf wiedersehen :|";
						response.put("closeContext", "true");
					}*/
					} else {
						assessment.incrementCurrentQuestionNumber();
						answer += assessment.getCurrentQuestion();        
					}
				}
	        
		} else {
        	System.out.println("Assessment type: "+ assessmentType + " not known");
        }
		response.put("text", answer);
        return response;
	}

	private String selectLevelMsg() {
		// TODO Auto-generated method stub
		Set<String> selection = new HashSet<String>();
		selection.add("Dokument: Dokumentbezogene Indizes");
		selection.add("Absatz:  Indizes bezogen auf den Absatz");
		selection.add("Satz: Satzbezogene Indizes");


		String response = "Welche textlevel würdest du erst überprüfen?\n"
				+ "Du kannst dir eine aussuchen: \n";

		Iterator<String> it = selection.iterator();
		int i = 1;
		while(it.hasNext()){
			response += i + ". " + it.next() + "\n";
			i++;
		}

		response += "Bitte level eingeben";
		return response;
	}
	
	private String selectLevel(JSONObject result)throws ParseException {
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
		JSONObject data = (JSONObject) result.get("data");
		return data.getAsString("level");
	}
	/**
	 * Filter Indices on Category and level
	 * @param category categoryname for the Indices function will match it to
	 * @param level levelname for the Indices function will match it to
	 * @return Indicevalue as jsonarray
	 * @throws ParseException
	 */
	private JSONArray selectIndices(String category, String level, JSONObject result) throws ParseException {
		
		// TODO Auto-generated method stub
		System.out.println("................in select indices................");
		System.out.println("................................category = "+ category); 
		System.out.println("................................level = "+ level); 
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
		JSONObject data = (JSONObject) result.get("data");
		JSONArray complexityIndices =(JSONArray) p.parse(data.getAsString("complexityIndices"));
		JSONArray results = new JSONArray();
		for (Object item : complexityIndices) {
			JSONObject obj = (JSONObject) item;

			if (obj.getAsString("category").matches("(?i).*" + category + ".*") ) {
				JSONArray valencesIndices =(JSONArray) p.parse(obj.getAsString("valences"));
				for(Object item2 : valencesIndices) {
					JSONObject obj2 = (JSONObject) item2;
					if(obj2.getAsString("type").matches("(?i).*" + level + ".*")) {
						System.out.println("................................category obj1 = "+ obj.getAsString("category")); 
						System.out.println("................................level obj2= "+ obj2.getAsString("type")); 
						results.add(obj2);
					}
				}
			}
		}
		return results;
	}
	
	private String finalReturn(String category, String level, JSONObject result) throws ParseException {
		JSONArray results= selectIndices( category,  level,  result);
		IndiceDescription indice = new IndiceDescription();
        JSONObject jsonObject = new JSONObject(result);
        HashMap<String,Integer> hashMap = new HashMap<>();
        String res="";
        for (Map.Entry<String,String> entry : indice.getIndiceMap().entrySet()) {
        	int i=0;
        	double sum=0;
	        for(Object item : results){
	            JSONObject innerJsonObject = (JSONObject) item;
	            if(innerJsonObject.getAsString("index").matches("(?i).*" + entry.getKey() + ".*")) {
	                sum+=Double.parseDouble(innerJsonObject.getAsString("value")); 
	                i+=1;
	            }
	        }
	        if(i>=1) {
	        	res+="\n Durchschnitt der "+ entry.getValue() + ": "+sum/i;
	        }
	        
        	
        }
        return res;
		
	}

	private JSONObject getContext(String email, JSONParser p)
			throws ParseException{
		// TODO Auto-generated method stub
		Object obj = ContextInfo.get(email);
		if (obj instanceof JSONObject) {
			JSONObject context = (JSONObject) (obj);
			return context;
		}
		return new JSONObject();
	}

	private static String replaceUmlaut(String input) {
 
		//replace all lower Umlauts
		String output = input.replace("ü", "ue")
							 .replace("ö", "oe")
							 .replace("ä", "ae")
							 .replace("ß", "ss");
	
		//first replace all capital umlaute in a non-capitalized context (e.g. Übung)
		output = output.replaceAll("Ü(?=[a-zäöüß ])", "Ue")
					   .replaceAll("Ö(?=[a-zäöüß ])", "Oe")
					   .replaceAll("Ä(?=[a-zäöüß ])", "Ae");
	
		//now replace all the other capital umlaute
		output = output.replace("Ü", "UE")
					   .replace("Ö", "OE")
					   .replace("Ä", "AE");
	
		return output;
	}

	



	/** Exceptions ,with messages, that should be returned in Chat */
	protected static class ChatException extends Exception {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		protected ChatException(String message) {
			super(message);
			Context
			.get()
			.monitorEvent(MonitoringEvent.SERVICE_CUSTOM_ERROR_3, message);
		}
	}
}
