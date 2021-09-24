package i5.las2peer.services.readerbenchService;



import java.io.IOException;
import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.*;
import java.util.Arrays;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.*;
import java.lang.*;
import java.io.*;
import java.text.DecimalFormat;
import java.nio.file.Files;
import javax.ws.rs.Path;
import java.nio.file.Paths;

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
import i5.las2peer.services.readerbenchService.model.MessageInfoTemp;
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
	//Analysis Started
	private static HashMap<String, Boolean> analysisStarted = new HashMap<String, Boolean>();  
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
	private String assessementHandlerEndpoint = "https://mentoring.tech4comp.dbis.rwth-aachen.de";
	//private String assessementHandlerEndpoint = "http://localhost:4200";
	private String chatDomain = "https://chat.tech4comp.dbis.rwth-aachen.de";
	private String sbManagerEndpoint = "http://137.226.232.75:32445/SBFManager/bots/textgrader/trigger/intent";


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
		JSONObject response = new JSONObject();
		System.out.println("Breakpoint--------getRbStatus from Service--------------------------");
		try {
			//System.out.println("converging pdf to base64");
			try {
				byte[] pdfByte = Files.readAllBytes(
						Paths.get("reports/Vorabevaluierung.pdf"));
				String fileBody = java.util.Base64.getEncoder().encodeToString(pdfByte);
				response.put("fileBody", fileBody);
				response.put("fileType", "pdf");
				response.put("fileName", "Feedback");
				System.out.println("finished conversion from pdf to base64");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("failed conversion from pdf to base64");
			}
			response.put("closeContext", true);
			return Response.ok().entity(response).build();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path("/getReportJson/{fileName}")
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
	public Response getReportJson(@PathParam("fileName") String fileName){
		JSONObject response = new JSONObject();
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
		try{
			try{
				Object obj = p.parse(new FileReader("reports/" + fileName+".json"));
				response = (JSONObject) obj;
			}
			catch (FileNotFoundException ex)  
			{
				ex.printStackTrace();
				return Response.ok("Error: File Not found").build();
			}
		} catch (ParseException e) {
			e.printStackTrace();
			return Response.ok("Error: Unknow").build();
		}
		return Response.ok().entity(response).build();
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
			ps = con.prepareStatement("SELECT * FROM topic");
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
				ps.setString(1, topicName );
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					ps.close();
					ps = con.prepareStatement("UPDATE topic SET processed=? due_date=? WHERE topic_name=?");
					ps.setString(3,  bodyJson.getAsString("topicName"));
					ps.setBoolean(1, false);
					ps.setString(2, "");
					ps.executeUpdate();

					//Delete old questions
					ps.close();
					ps = con.prepareStatement("DELETE FROM question  WHERE topic_id=?");
					ps.setString(1, bodyJson.getAsString("topicName"));
					ps.executeUpdate();  
				} else {
					
					ps.close();
					ps = con.prepareStatement("INSERT INTO topic(topic_id, topic_name,processed ,due_date) VALUES (?, ?, ?, ?)");
					ps.setString(1,  bodyJson.getAsString("topicName"));
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
						ps.close();
						ps = con.prepareStatement("INSERT INTO question(question, topic_id, textref, numberOfPoints) VALUES ( ?, ?, ?, ?)");
						ps.setString(1, questionJson.getAsString("question"));
						ps.setString(2, bodyJson.getAsString("topicName"));
						ps.setString(3, questionJson.getAsString("textref"));
						ps.setString(4, "1");
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
					this.analysisStarted.remove(channel);
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
								topicNames += " • " + rs.getString("topic_name") + "\n";
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
							response.put("text", "Wähle ein Thema indem du mit dem entsprechenden Name des Thema antwortest:\n" + topicNames);
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
								
								if(topicName.toLowerCase().equals(bodyJson.getAsString("msg").toLowerCase()) ){
									
									setUpNluAssessment2(topicName, topicId, channel,  bodyJson.getAsString("Type"), bodyJson.getAsString("modelType"));
									response.put("text", "Wir starten jetzt das  Assessment über "+ topicName + " :)!\n" + this.currentNLUAssessment.get(channel).getCurrentQuestion());							
									response.put("closeContext", "false");
									this.topicsProposed.remove(channel);
									this.assessmentStarted.put(channel, "true");
									bodyJson.put("msg", topicName);
									return Response.ok().entity(response).build();
								} 
								topicCount++;
							}
								response.put("text", "Assessment mit der name " + bodyJson.getAsString("msg")+ " wurde nicht gefunden");
								response.put("closeContext", "false");
							
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
				return Response.ok().entity(continueAssessment2(channel, bodyJson.getAsString("intent"), bodyJson, "NLUAssessmentDe", body)).build();
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
			String[][] assessmentContent = new String[length][2];
			//String[][] assessmentContent = new String[length][3];
			int index=0;
			while(rs.next()) {
				assessmentContent[index][0] = rs.getString("question");
				assessmentContent[index][1] = rs.getString("textref"); 
				index++;
			}
			//Arrays.sort(assessmentContent, (a, b) -> Integer.compare(Integer.parseInt(a[0]), Integer.parseInt(b[0])));
			ArrayList<String> questions = new ArrayList<String>();
			ArrayList<String> lectureref = new ArrayList<String>();
			ArrayList<Double> numberOfPoints = new  ArrayList<Double>();
			ArrayList<String> similarityScore = new  ArrayList<String>();
			ArrayList<String> textlevel = new ArrayList<String>();
			ArrayList<String> answers = new ArrayList<String>();
			ArrayList<String> refComplexity = new ArrayList<String>();
			ArrayList<String> feedbackText = new ArrayList<String>();
			ArrayList<String> cnaText = new ArrayList<String>();
			ArrayList<String> keywordText = new ArrayList<String>();

			for(int i = 0; i < length ; i++){
				questions.add(assessmentContent[i][0]);
				lectureref.add(replaceUmlaut(assessmentContent[i][1]));
				//numberOfPoints.add(Double.parseDouble(assessmentContent[i][2]));
				numberOfPoints.add(Double.parseDouble("1"));
				similarityScore.add("");
				textlevel.add("");
				answers.add("");
				refComplexity.add("");
				feedbackText.add("");
				cnaText.add("");
				keywordText.add("");
			}
			NLUAssessment assessment = new NLUAssessment(topicName, topicId, "stopAssessment", questions, null, null, "help", type, lectureref, numberOfPoints, modelType, 
				similarityScore, textlevel, refComplexity, answers, feedbackText, cnaText, keywordText);
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
	

	private JSONObject continueAssessment2(String channel, String intent, JSONObject triggeredBody, String assessmentType, String bodyJsonOriginal){
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
					this.assessmentStarted.remove(channel);
					this.analysisStarted.remove(channel);
					response.put("closeContext", "true");
				} else if(intent.equals(assessment.getHelpIntent())){
					answer+= assessment.getQuestionHint() + "\n";
					response.put("closeContext", "false");
				} else if(triggeredBody.getAsString("msg").equals("status") & intent.equals("status")){
					try {
						
							
						JSONObject file = new JSONObject();
						JSONArray uebung = new JSONArray(); 
						
						for(int i = 0; i < assessment.getAssessmentSize(); i++){
								JSONObject obj = new JSONObject();
								obj.put("topic", assessment.getTopicName());
								obj.put("question", assessment.getQuestionByNumber(i));
								obj.put("channel", channel);
								obj.put("cna_result",  (JSONObject) p.parse(assessment.getCnaText(i)));
								obj.put("compare_result",(JSONObject) p.parse(assessment.getFeedbackText(i)));
								obj.put("similarity_result", (JSONObject) p.parse(assessment.getSimilarityScoreList().get(i)));
								obj.put("keyword_result",(JSONObject) p.parse(assessment.getKeywordText(i)));	
								uebung.add(obj);
								

								
								
						}
						file.put("data", uebung);
						file.put("topic",  assessment.getTopicName()); 
						
						String time =""+ System.currentTimeMillis();
						String fileName = "file_" +channel+"_"+time+".json";
						FileWriter writer = new FileWriter("reports/" + fileName);
						writer.write(file.toString());
						writer.close();
						answer += " Öffne den Link um das Feedback zu deinem Text zu sehen. Die enthält Darstellungen von Schlüsselwörter und auch eine kurze Erklärung dazu. Das Feedback kann dir helfen, deine Bearbeitung der Schreibaufgabe zu reflektieren.:\n "
						+ this.assessementHandlerEndpoint+"/report?fileName="+"file_" +channel+"_"+time;	

						answer += "\n Das Feedback bekommst du auch in dieser PDF-Datei.";
						
						
						
						System.out.println("................here +++++++++++++++++++++................");
						try {
							byte[] pdfByte = Files.readAllBytes(Paths.get("reports/"+assessment.getTopicName()+".pdf"));
							String fileBody = java.util.Base64.getEncoder().encodeToString(pdfByte);

							System.out.println("................retriving the Pdf................");

							response.put("fileBody", fileBody);
							response.put("fileType", "pdf");
							response.put("fileName", "Feedback zur "+ assessment.getTopicName());
							System.out.println("finished conversion from pdf to base64");
						} catch (Exception e) {
							e.printStackTrace();
							System.out.println("failed conversion from pdf to base64");
						}
						this.currentNLUAssessment.remove(channel);
						this.assessmentStarted.remove(channel);
						response.put("closeContext", "true");

						response.put("text", answer);
        				return response;
					} catch (Exception e) {
						e.printStackTrace();
						answer+=" Fehler beim Ermittlung der Ergebnisse...\n Bitte wendest dich an deinem Tutor";
						this.currentNLUAssessment.remove(channel);
						this.assessmentStarted.remove(channel);
						response.put("closeContext", "true");
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
						System.out.println("analysisStarted: " + this.analysisStarted.get(channel) );
						if(this.analysisStarted.get(channel) == null) {
							this.analysisStarted.put(channel, true);
							DecimalFormat df = new DecimalFormat("#.##");
							for(int i=0; i <  assessment.getAssessmentSize(); i++){		
								String ref = replaceUmlaut(assessment.gettextReferenceByNumber(i));
								String ans = replaceUmlaut(assessment.getAnswerByNumber(i));
								ref = replaceChar(ref);
								ans = replaceChar(ans);
								String refComplexity = assessment.getrefComplexityByNumber(i) ;
								System.out.println("................ref................");
								System.out.println(ref);
								System.out.println("................answer................");
								System.out.println(ans);
								JSONArray texts = new JSONArray(); 
								texts.add(ref);
								texts.add(ans);
	
								//Body for the similarity
								JSONObject similarity_Body = new JSONObject();
								similarity_Body.put("texts", texts);
								similarity_Body.put("language", "de");
								similarity_Body.put("corpus", "wiki");
								similarity_Body.put("saveAs", assessment.getTopicName().replaceAll(" ", "_")+ (i+1));
								similarity_Body.put("topicName", assessment.getTopicName().replaceAll(" ", "_"));
								similarity_Body.put("topicSize",  assessment.getAssessmentSize());
								JSONObject complexity_Body = new JSONObject();
								complexity_Body.put("language", "de");
								complexity_Body.put("text", ans);
								complexity_Body.put("saveAs", assessment.getTopicName().replaceAll(" ", "_")+ (i+1));
								complexity_Body.put("topicName", assessment.getTopicName().replaceAll(" ", "_"));
								complexity_Body.put("topicSize",  assessment.getAssessmentSize());
								JSONObject compare_Body = new JSONObject();
								compare_Body.put("language", "de");
								compare_Body.put("text", ans);
								compare_Body.put("expert", replaceUmlaut(ref));
								compare_Body.put("saveAs", assessment.getTopicName().replaceAll(" ", "_")+ (i+1));
								compare_Body.put("topicName", assessment.getTopicName().replaceAll(" ", "_"));
								compare_Body.put("topicSize",  assessment.getAssessmentSize());
								JSONObject cna_Body = new JSONObject();
								JSONObject doc1 = new JSONObject();
								doc1.put("text", ans);
								JSONObject doc2 = new JSONObject();
								doc2.put("text", ref);
								JSONArray docs = new JSONArray(); 
								docs.add(doc1);
								docs.add(doc2);
								cna_Body.put("texts", docs);
								cna_Body.put("lang", "de");
								cna_Body.put("saveAs", assessment.getTopicName().replaceAll(" ", "_")+ (i+1)+"_cna");
								cna_Body.put("topicName", assessment.getTopicName().replaceAll(" ", "_"));
								cna_Body.put("topicSize",  assessment.getAssessmentSize());
								JSONArray models = new JSONArray();
								JSONObject w2v = new JSONObject();
								w2v.put("model", "WORD2VEC"); 
								w2v.put("corpus", "wiki"); 
								models.add(w2v);
								cna_Body.put("models", models);
	
								//Body Object for the Keyword
								JSONObject keyword_Body = new JSONObject();
								keyword_Body.put("text", ans);
								keyword_Body.put("type", "student");
								keyword_Body.put("language", "de");
								keyword_Body.put("saveAs", assessment.getTopicName().replaceAll(" ", "_")+ (i+1)+"_keyword");
								keyword_Body.put("topicName", assessment.getTopicName().replaceAll(" ", "_"));
								keyword_Body.put("topicSize",  assessment.getAssessmentSize());
								JSONObject keyword_Body_exprt = new JSONObject();
								keyword_Body_exprt.put("text", ref);
								keyword_Body_exprt.put("type", "expert");
								keyword_Body_exprt.put("language", "de");
								keyword_Body_exprt.put("saveAs", assessment.getTopicName().replaceAll(" ", "_")+ (i+1)+"_expert_keyword");
								keyword_Body_exprt.put("topicName", assessment.getTopicName().replaceAll(" ", "_"));
								keyword_Body_exprt.put("topicSize",  assessment.getAssessmentSize());
	
	
								try{
									String email = triggeredBody.getAsString("email");
									JSONObject context = getContext(email, p);	
									StringEntity entity = new StringEntity(cna_Body.toString());
									HttpClient httpClient = HttpClientBuilder.create().build();
									HttpPost request = new HttpPost(this.readerbenchEndpoint+"/api/v1/cna-graph");
									request.setEntity(entity);
									HttpResponse res = httpClient.execute(request);
									HttpEntity entity2 = res.getEntity();
									String cna_result = EntityUtils.toString(entity2);
									context.put("cna_result", cna_result);
									ContextInfo.put(email, context);
									System.out.println("................result cna computed from readerbench................");
									System.out.println(cna_result);
									assessment.setCnaTextByNumber(i,cna_result);
									/*
									JSONObject result = (JSONObject) p.parse(context.getAsString("cna_result")); 
									String cnaString ="Ähnlichkeit unter CNA(Cohesion Network Graph):\n";
									JSONObject data = (JSONObject) result.get("data");

									

									JSONArray edges = (JSONArray) p.parse(data.getAsString("edges"));

									for (Object item : edges) {
										JSONObject obj = (JSONObject) item;
										System.out.println("Source: "+ obj.getAsString("source")+ "Target: "+ obj.getAsString("target"));
										if(obj.getAsString("source").toLowerCase().contains("document") && obj.getAsString("target").toLowerCase().contains("document")  ){
											System.out.println("Source: "+ obj.getAsString("types"));
											JSONArray types = (JSONArray) p.parse(obj.getAsString("types"));
											cnaString+="Überlappungstyp | Score\n";
											
											for (Object item1 : types) {
												JSONObject obj1 = (JSONObject) item1;
												double str1 = Double.parseDouble(obj1.getAsString("weight")); 
												cnaString+=obj1.getAsString("name")+"| "+df.format(str1)+ "\n";
												/*if(obj1.getAsString("name")=="LEXICAL_OVERLAP: CONTENT_OVERLAP"){
													
													cnaString+="Lexical: Content| "+df.format(str1);
												}
												if(obj1.getAsString("name")=="LEXICAL_OVERLAP: TOPIC_OVERLAP"){
													cnaString+="Lexical: Topic| "+df.format(str1);
												}
												if(obj1.getAsString("name")=="LEXICAL_OVERLAP: ARGUMENT_OVERLAP"){
													cnaString+="Lexical: Argument| "+df.format(str1);
												}
												if(obj1.getAsString("name")=="SEMANTIC: WORD2VEC(wiki)"){
													cnaString+="SEMANTIC:  WORD2VEC(wiki)| "+df.format(str1);
												}*//*
												
											}
											break;
										}                          
									}
									assessment.setCnaTextByNumber(i,cnaString);
									*/
								}  catch (Exception e) {
									e.printStackTrace();
									System.out.println("................Problem with cna................");
									error.put("text", "Readerbench scheint ein Problem zu haben\n Bitte wendest dich an deinem Tutor");
									error.put("closeContext", true);
									return error;
								}
	
								try{
									String email = triggeredBody.getAsString("email");
									JSONObject context = getContext(email, p);	
									StringEntity entity = new StringEntity(compare_Body.toString());
									HttpClient httpClient = HttpClientBuilder.create().build();
									HttpPost request = new HttpPost(this.readerbenchEndpoint+"/api/v1/complexity-compare");
									request.setEntity(entity);
									HttpResponse res = httpClient.execute(request);
									HttpEntity entity2 = res.getEntity();
									String compare_result = EntityUtils.toString(entity2);
									context.put("compare_result", compare_result);
									ContextInfo.put(email, context);
									System.out.println("................result compare computed from readerbench................");
									System.out.println(compare_result);
									assessment.setFeedbackTextByNumber(i,compare_result);
									/*
									System.out.println(context.getAsString("compare_result"));
									JSONObject result = (JSONObject) p.parse(context.getAsString("compare_result")); 
									String feedbackString ="";
									feedbackString+="Text-Indizes | Dein Score | Musterlösung | Feedback\n";
									JSONObject data = (JSONObject) result.get("data");

									String level = data.getAsString("level");
									assessment.setLevelByNumber(i,level);
									JSONObject feedback = (JSONObject) data.get("feedback");
									

									JSONArray document = (JSONArray) p.parse(feedback.getAsString("document"));

									for (Object item : document) {
										JSONObject obj = (JSONObject) item;
										feedbackString  += obj.getAsString("name")+" | "+df.format(Double.parseDouble(obj.getAsString("metric")))+" | "+df.format(Double.parseDouble(obj.getAsString("expert_metric")))+" | "+obj.getAsString("description") +"\n";
									}
									assessment.setFeedbackTextByNumber(i,feedbackString);*/
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
									assessment.setSimilarityByNumber(i,similarity_result);
									/*
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
									*/
									
								} catch (Exception e) {
									e.printStackTrace();
									System.out.println("................Problem with Similarity................");
									error.put("text", "Readerbench scheint ein Problem zu haben\n Bitte wendest dich an deinem Tutor");
									error.put("closeContext", true);
									return error;
								}
								try {
									String email = triggeredBody.getAsString("email");
									JSONObject context = getContext(email, p);	
									StringEntity entity = new StringEntity(keyword_Body.toString());
									HttpClient httpClient = HttpClientBuilder.create().build();
									HttpPost request = new HttpPost(this.readerbenchEndpoint+"/api/v1/keywords");
									request.setEntity(entity);
									HttpResponse res = httpClient.execute(request);
									HttpEntity entity2 = res.getEntity();
									String keyword_result = EntityUtils.toString(entity2);
									context.put("keyword_result", keyword_result);
									ContextInfo.put(email, context);
									System.out.println(keyword_result);
									JSONObject result_std = (JSONObject) p.parse(context.getAsString("keyword_result")); 

									entity = new StringEntity(keyword_Body_exprt.toString());
									httpClient = HttpClientBuilder.create().build();
									request = new HttpPost(this.readerbenchEndpoint+"/api/v1/keywords");
									request.setEntity(entity);
									res = httpClient.execute(request);
									entity2 = res.getEntity();
									String keyword_result_exprt = EntityUtils.toString(entity2);
									context.put("keyword_result_exprt", context.getAsString("keyword_result_exprt"));
									ContextInfo.put(email, context);

									System.out.println("................keyword SIMILARITY computed from readerbench................");
									System.out.println(keyword_result_exprt);
									JSONObject result_exprt = (JSONObject) p.parse(keyword_result_exprt);

									JSONObject result = new JSONObject();
									result.put("student", result_std);
									result.put("expert", result_exprt);
									assessment.setKeywordTextByNumber(i,result.toString());
	
								} catch (Exception e) {
									e.printStackTrace();
									System.out.println("................Problem with keyword_result_exprt................");
									error.put("text", "Readerbench scheint ein Problem zu haben\n Bitte wendest dich an deinem Tutor");
									error.put("closeContext", true);
									return error;
								}
								try {
									JSONObject pdf_Body = new JSONObject();
									pdf_Body.put("topicName", assessment.getTopicName().replaceAll(" ", "_"));
									pdf_Body.put("topicSize",  assessment.getAssessmentSize());
									StringEntity entityString = new StringEntity(pdf_Body.toString());
									HttpClient httpClient = HttpClientBuilder.create().build();
									HttpPost request = new HttpPost(this.readerbenchEndpoint+"/api/v1/getPdf");
									request.setEntity(entityString);
									request.setHeader("Content-type", "application/pdf");

									HttpResponse httpResponse = httpClient.execute(request);
									HttpEntity entity = httpResponse.getEntity();
									//byte[] entityBytes = EntityUtils.toByteArray(httpResponse.getEntity());
									//Files.write(Paths.get("test.pdf"), entityBytes);
									//String fileBody = java.util.Base64.getEncoder().encodeToString(entityBytes);
									String filePath = "reports/"+assessment.getTopicName()+".pdf";
									FileOutputStream outstream  = new FileOutputStream(new File(filePath));
									entity.writeTo(outstream);
								} catch (Exception e) {
									e.printStackTrace();
									System.out.println("................Problem while retriving the Pdf................");
									error.put("text", "Readerbench scheint ein Problem zu haben, und kann nicht das pdf generieren\n Bitte wendest dich an deinem Tutor");
									error.put("closeContext", true);
									return error;
								}	
									
							}
							
							if(this.analysisStarted.get(channel) == null) {
								return null;
							}
							else{								
								try {
									String BodyString= "{"+
									"\"message\": {"+
											"\"channel\": \""+channel+"\","+
											"\"user\": \""+triggeredBody.getAsString("user")+"\","+
											"\"role\": 0,"+
											"\"email\": \""+triggeredBody.getAsString("email")+"\","+
											"\"text\": \"status\","+
											"\"domain\": \""+chatDomain+"\" "+
										"},"+
										" \"intent\": {"+
											"\"intentKeyword\": \"status\","+
											"\"confidence\": 1.0,"+
											"\"entities\": {"+
												"\"status\": {"+
													"\"entityName\": \"status\","+
													"\"value\": \"status\","+
													"\"confidence\": 1.0"+
												"}"+
											"}"+
										"},"+
										"\"botName\": \"textgrader\","+
										"\"serviceAlias\": \"Gruppe1\","+
										"\"contextWithService\": true,"+
										"\"recognizedEntities\": [],"+
										"\"triggeredFunctionId\": \"4ca1264ede58703622a9ccdd\""+
									"}";
									StringEntity entity = new StringEntity(BodyString);
									HttpClient httpClient = HttpClientBuilder.create().build();
									HttpPost request = new HttpPost(sbManagerEndpoint);
									request.setEntity(entity);
									request.setHeader("Content-type", "application/json");
									HttpResponse res = httpClient.execute(request);
									HttpEntity entity2 = res.getEntity();
									String triggerresult = EntityUtils.toString(entity2);
									System.out.println("triggerresults "+ triggerresult);
								} catch (Exception e) {
									e.printStackTrace();
								}
							
							}
							
							response.put("text", "Bewertung ist fertig");
							response.put("closeContext", "false");
							return response;
					
						}
						else{
							response.put("text", "Danke für deine Abgabe "+ triggeredBody.getAsString("user") +". Ich leite sie an das Analysesystem \"Readerbench\" weiter und gebe dir gleich deine Rückmeldung. Das dürfte nur ein paar Minuten dauern.");
							response.put("closeContext", "false");
							return response;
						}
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
					   .replace("Ä", "AE")
					   .replace("ß", "ss");
	
		return output;
	}

	private static String replaceChar(String input) {
 
		//replace all lower Umlauts
		String output = input.replace(" _ ", " ")
							.replace("„", "\"")
							.replace("“", "\"");
	
	
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
