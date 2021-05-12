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

import i5.las2peer.services.readerbenchService.model.MessageInfo;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import i5.las2peer.services.readerbenchService.AssessmentContent.*;
import i5.las2peer.services.socialBotManagerService.database.SQLDatabase;
import i5.las2peer.services.socialBotManagerService.database.SQLDatabaseType;

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

	private String readerbenchEndpoint="http://rb-controller.ma-zeufack:32446";

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

	private String databaseName;
	private int databaseTypeInt = 1; // See SQLDatabaseType for more information
	private SQLDatabaseType databaseType;
	private String databaseHost;
	private int databasePort;
	private String databaseUser;
	private String databasePassword;
	private SQLDatabase database; // The database instance to write to.

	public ReaderbenchService(){
		super();
		setFieldValues();

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

	/**
	 * Template of a post function.
	 * 
	 * @param myInput The post input the user will provide.
	 * @return Returns an HTTP response with plain text string content derived from the path input param.
	 */
	@POST
	@Path("/post/{input}")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	@ApiOperation(
			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "Example method that returns a phrase containing the received input.")
	public Response postTemplate(@PathParam("input") String myInput) {
		String returnString = "";
		returnString += "Input " + myInput;
		return Response.ok().entity(returnString).build();
	}

	// TODO your own service methods, e. g. for RMI

	/**
	 * Template of a get function.
	 * 
	 * @return Returns an HTTP response with the username as string content.
	 */
	@GET
	@Path("/getSrvStatus")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(
			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "REPLACE THIS WITH YOUR NOTES TO THE FUNCTION")
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	public Response getSrvStatus() {
		JSONObject j = new JSONObject();
		j.put("text", "service alive");
		j.put("closeContext", true);
		String status = "alive---------------";
		return Response.ok().entity(j).build();
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
			HttpGet httpget = new HttpGet("http://rb-controller.ma-zeufack:32446/api/v1/isalive");

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
	@Path("/textual_similarity")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	@ApiOperation(
			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "Example method that returns a phrase containing the received input.")
	public Response textsimilarity(String body) {
		JSONObject j = new JSONObject();
		j.put("language", "en");
		//j.put("texts", texts);
		j.put("corpus", "wikibooks");
		try {
			StringEntity entity = new StringEntity(j.toString());
			HttpClient httpClient = HttpClientBuilder.create().build();
	        HttpPost request = new HttpPost("http://rb-controller.ma-zeufack:32446/api/v1/text-similarity");
	        request.setEntity(entity);
	        HttpResponse response = httpClient.execute(request);
	        HttpEntity entity2 = response.getEntity();
		    String result = EntityUtils.toString(entity2);
		    System.out.println("................"+result);
		    return Response.ok().entity(result).build();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	@POST
	@Path("/feedback")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Feedback has been preceded") })
	@ApiOperation(
			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "Example method that returns a phrase containing the received input.")
	public Response feedback(String body) {
		Gson gson = new Gson();
		MessageInfo m = gson.fromJson(body, MessageInfo.class);
		System.out.println("Got message: " + m.msg() + " From Bot" + m.botName());
		String text = m.msg();
		System.out.println("Breakpoint--------1------------------"+ body);
		JSONObject j = new JSONObject();
		j.put("language", "en");
		j.put("text", text);
		try {
			StringEntity entity = new StringEntity(j.toString());
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost("http://rb-controller.ma-zeufack:32446/api/v1/feedback");
			request.setEntity(entity);
			HttpResponse response = httpClient.execute(request);
			HttpEntity entity2 = response.getEntity();
			String result = EntityUtils.toString(entity2);
			System.out.println("................result computed from readerbench................");




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
	@Path("/textual_complexity")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	@ApiOperation(
			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "Example method that returns a phrase containing the received input.")
	public Response textualComplexity(String body) {
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
		JSONObject chatResponse = new JSONObject();
		final long start = System.currentTimeMillis();
		JSONObject event = new JSONObject();
		System.out.println("Body " + body);
		try {
			JSONObject bodyJson = (JSONObject) p.parse(body);
			String text;
			String email = bodyJson.getAsString("email");
			JSONObject context = getContext(email, p);
			String intent = bodyJson.getAsString("intent");
			System.out.println("Context " + context);
		    event.put("email", email);
			event.put("task", "textualComplexity");

			System.out.println("................"+intent+"................");
			switch (intent) {
			case "quit":
				chatResponse.put("text", "Willst du die Ergebnisse speichern um später anzusehen?");
				chatResponse.put("closeContext", true);
				return Response.ok(chatResponse).build();
			case "confirm":
				//To do: insert SQL: Insert into SQL
				chatResponse.put("text", "Deine Ergebnisse wurden gespeichert. Aufwiedersehen.");
				chatResponse.put("closeContext", true);
				return Response.ok(chatResponse).build();
			case "no":
				chatResponse.put("text", "Okay Aufwiedersehen.");
				chatResponse.put("closeContext", true);
				return Response.ok(chatResponse).build();
			case "text":
				if (context.getAsString("result") != null) {
					context.remove("result");
				}
				if (context.getAsString("category") != null) {
					context.remove("category");
				}
				if (context.getAsString("level") != null) {
					context.remove("level");
				}
				text = bodyJson.getAsString("msg");
				JSONObject j = new JSONObject();
				j.put("language", "de");
				j.put("text", text);
				try {
					StringEntity entity = new StringEntity(j.toString());
					HttpClient httpClient = HttpClientBuilder.create().build();
					HttpPost request = new HttpPost("http://rb-controller.ma-zeufack:32446/api/v1/textual-complexity");
					request.setEntity(entity);
						HttpResponse response = httpClient.execute(request);
						HttpEntity entity2 = response.getEntity();
						String result = EntityUtils.toString(entity2);
					
					context.put("result", result);
					ContextInfo.put(email, context);
					System.out.println("................result computed from readerbench................");  
					chatResponse.put("closeContext", false);
					JSONObject result2 = (JSONObject) p.parse(context.getAsString("result"));
					String res= "Der Text wurde bearbeitet, dein Text level ist folgende:\n"+
					selectLevel(result2) + "\nWir können  dir noch eine Zusammenfassung des wichtigsten indizen zeigen\n"
					+"dafür musst du eine der folgende Kategorien auswählen"+ selectCategoryMsg();
					System.out.println(res);
					chatResponse.put("closeContext", false);
					chatResponse.put("text",res);
					try {
						MiniClient client = new MiniClient();
						client.setConnectorEndpoint("http://137.226.232.185:32445");
						//client.setLogin(testAgent.getIdentifier(), testPass);
						
						// testInput is the pathParam
						ClientResponse trigger_result = client.sendRequest("POST", "SBFManager" + "post/{readerBot}/trigger/intent", "");
						Assert.assertEquals(200, trigger_result.getHttpCode());
						// "testInput" name is part of response
						Assert.assertTrue(trigger_result.getResponse().trim().contains("testInput"));
						System.out.println("Result of 'testPost': " + trigger_result.getResponse().trim());
					} catch (Exception e) {
						e.printStackTrace();
					}
					return Response.ok().entity(chatResponse).build();
				}catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new ChatException(
							e.getMessage()
							);
				}
			case "status":
				if (context.getAsString("result") != null) {
					JSONObject result22 = (JSONObject) p.parse(context.getAsString("result"));
					String res11= "Der Text wurde bearbeitet, dein Text level ist folgende:\n"+
					selectLevel(result22) + "\nWir können  dir noch eine Zusammenfassung des wichtigsten indizen zeigen\n"
					+"dafür musst du eine der folgende Kategorien auswählen"+ selectCategoryMsg();
					System.out.println(res11);
					chatResponse.put("closeContext", false);
					chatResponse.put("text",res11);
					return Response.ok().entity(chatResponse).build();
				}
				chatResponse.put("closeContext", false);
				chatResponse.put("text","Der Text wird noch analysiert...\n ");
				return Response.ok().entity(chatResponse).build();
				
			case "category":
				String category =  bodyJson.getAsString("category_option");
				if(category == null) {
					throw new ChatException(
							"Aspekte wurde nicht erkannt. Bitte neue angeben"
							);

				}
				else {
					if (context.getAsString("category") != null) {
						context.remove("category");
					}
					context.put("category", category);
					ContextInfo.put(email, context);
				}
				String res = selectLevelMsg();
				chatResponse.put("text",res);
				chatResponse.put("closeContext", false);
				return Response.ok().entity(chatResponse).build();
			case "level":
				String level =  bodyJson.getAsString("level_option");
				if(level == null) {
					throw new ChatException(
							"Shicht wurde nicht erkannt. Bitte wieder angeben"
							);
				}
				else {
					if (context.getAsString("level") != null) {
						context.remove("level");
					}
					context.put("level", level);
					ContextInfo.put(email, context);
				}
				String category1 = context.getAsString("category");
				JSONObject result = (JSONObject) p.parse(context.getAsString("result"));
				String filtered = finalReturn(category1, level, result);
				System.out.println("................................indices selected............."); 
				String res1=" \n";
				res1+=filtered+"\n Um ein weitere level für die Kategorie "+ context.getAsString("category")
				+ " auswählen schreib: neue Level\n"
				+"Um die indizen eine neue Kategorie anzuschauen, schreib: neue Kategorie\n"
				+"Zum verlassen schreib einfach verlassen";
				chatResponse.put("text",res1);
				chatResponse.put("closeContext", false);
				return Response.ok().entity(chatResponse).build();
			case "new_Category":
				if (context.getAsString("category") != null) {
					context.remove("category");
				}
				if (context.getAsString("level") != null) {
					context.remove("level");
				}
				String res2 = selectCategoryMsg();
				chatResponse.put("text",res2);
				chatResponse.put("closeContext", false);
				return Response.ok().entity(chatResponse).build();
			case "new_Level":
				if (context.getAsString("level") != null) {
					context.remove("level");
				}
				String res3 = selectLevelMsg();
				chatResponse.put("text",res3);
				chatResponse.put("closeContext", false);
				return Response.ok().entity(chatResponse).build();



				/*case "Diskursstruktur":
			          category = "DISCOURSE";
			     case "Morphologie":
			          category = "MORPHOLOGY";
			     case "Oberflaeche":
			          category = "SURFACE";
			     case "Syntax":
			          category="SYNTAX";
			     case "Wortkomplexitaet":
			          category="WORD";*/
			default:
				String res4 ="Intent könnte nicht ermitteln werden. Bitte erneue versuchen neue beginnen.";
				chatResponse.put("text",res4);
				chatResponse.put("closeContext", false);
				return Response.ok().entity(chatResponse).build();
			}

		}
		catch (ChatException e) {
			chatResponse.appendField("text", e.getMessage());
			chatResponse.put("closeContext", false);
			return Response.ok().entity(chatResponse).build();
		} catch (Exception e) {
			e.printStackTrace();
			chatResponse.appendField("text", "Sorry, ein unbekanntes Problem ist angekommen ");
			chatResponse.put("closeContext", true);
			return Response.ok(chatResponse).build();
		}
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


		} catch (ParseException e) {
			e.printStackTrace();
			return Response.ok("Assessment not inserted").build();
		}
		return Response.ok("Assessment inserted").build();
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
				// function needs assessmentContent parameter
				/*if(!(bodyJson.get("assessmentContent") instanceof JSONArray)) {
					JSONArray assessmentContent = new JSONArray();
					assessmentContent.add(bodyJson.get("assessmentContent"));
					bodyJson.put("assessmentContent", assessmentContent);
				}
				/*JSONArray jsonAssessment = (JSONArray) bodyJson.get("assessmentContent");
				ArrayList<String> assessment = new ArrayList<String>();
				if(jsonAssessment != null) {
					int len = jsonAssessment.size();
					for(int i=0; i<len ; i++){
						assessment.add(jsonAssessment.get(i).toString());
					}*/
					JSONObject contentJson;
					if(this.topicsProposed.get(channel) == null) {
						String topicNames="";
						int topicNumber = 1;
						for(String content : Assessment) {
							 contentJson = (JSONObject) p.parse(content);
							 topicNames += " • " + topicNumber + ". " + contentJson.getAsString("topicName") + "\n";
							 topicNumber++;
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
						
						String chosenTopicNumber = bodyJson.getAsString("msg").split("\\.")[0];
						String similarNames = "";
				        ArrayList<String> similarTopicNames = new ArrayList<String>();
				        String smiliarNames = "";
						int topicCount = 1;
						for(String content : Assessment) {
							 contentJson = (JSONObject) p.parse(content);
							if(contentJson.getAsString("topicName").toLowerCase().equals(bodyJson.getAsString("msg").toLowerCase()) || chosenTopicNumber.equals(String.valueOf(topicCount))){
								setUpNluAssessment2(contentJson, channel, bodyJson.getAsString("quitIntent"), bodyJson.getAsString("helpIntent"),  bodyJson.getAsString("Type"), bodyJson.getAsString("modelType"));
								this.topicsProposed.remove(channel);
								this.assessmentStarted.put(channel, "true");
								response.put("text", "Wir starten jetzt das Nlu Assessment über "+ contentJson.getAsString("topicName") + " :)!\n" + this.currentNLUAssessment.get(channel).getCurrentQuestion());							
								response.put("closeContext", "false");
								
								return Response.ok().entity(response).build(); 
							} else if(contentJson.getAsString("topicName").toLowerCase().contains(bodyJson.getAsString("msg").toLowerCase())) {
								similarTopicNames.add(contentJson.getAsString("topicName"));
								similarNames += " • " + topicCount + ". " + contentJson.getAsString("topicName") + "\n";
							}
							topicCount++;
						}
						if(similarTopicNames.size() == 1) {
							bodyJson.put("msg", similarTopicNames.get(0));
							return nluAssessmentDe(bodyJson.toString());
						} else if(similarTopicNames.size() > 1) {
							response.put("text", "Mehrere Nlu Assessments entsprechen deiner Antwort, welche von diesen möchtest du denn anfangen?\n" + similarNames);							
							response.put("closeContext", "false");
							return Response.ok().entity(response).build();
						}
						JSONObject error = new JSONObject();
						error.put("text", "Topic with name " + bodyJson.getAsString("topicName")+ " not found");
						error.put("closeContext", "true");
						return Response.ok().entity(error).build();
					}
				/*}*/
				

			} else {
				System.out.println(bodyJson.getAsString("intent"));
				return Response.ok().entity(continueAssessment(channel, bodyJson.getAsString("intent"), bodyJson, "NLUAssessmentDe")).build();
			}		
			
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		JSONObject error = new JSONObject();
		error.put("text", "Something went wrong");
		error.put("closeContext", "true");
		return Response.ok().entity(error).build();

	}

	private void setUpNluAssessment(JSONObject content , String channel, String quitIntent, String helpIntent, String type, String modelType) {
        int noNum = 0;
        JSONArray Sequence =(JSONArray) content.get("Sequence");
        JSONArray Questions =(JSONArray) content.get("Questions");
        JSONArray Intents =(JSONArray) content.get("Intents");
		JSONArray Hints =(JSONArray) content.get("Hints");
		//Adding the element for the text Assessment
		JSONArray Lectureref =(JSONArray) content.get("Textrefs");
		JSONArray QuestionWeight =(JSONArray) content.get("QuestionWeights");




        int length = Questions.size(); 
        int max = 0;
        String[][] assessmentContent = new String[length][6];
        for(int i = 0; i < length ; i++){
            if(Sequence.get(i).equals("")){
                noNum++;   
            } else if(Integer.parseInt(Sequence.get(i).toString()) > max){
                max = Integer.parseInt(Sequence.get(i).toString());    
            } else if(Integer.parseInt(Sequence.get(i).toString()) == max) {
            	Sequence.add(i, String.valueOf(max+1));
            	max++;
            }           
            assessmentContent[i][0] = Sequence.get(i).toString();
            assessmentContent[i][1] = Questions.get(i).toString();
            assessmentContent[i][2] = Intents.get(i).toString();
			assessmentContent[i][3] = Hints.get(i).toString();  
			assessmentContent[i][4] = Lectureref.get(i).toString(); 
			assessmentContent[i][5] = QuestionWeight.get(i).toString();      
        }
        
        // to fill out the blank sequence slots
        // last blank space will be at last place
        
        
        
        // change to nlu quiz object
        for(int i = length-1; i >= 0 ; i--){
            if(assessmentContent[i][0].equals("")){
            	assessmentContent[i][0] = Integer.toString(max + noNum);
            	noNum--;
            }
        }      
        Arrays.sort(assessmentContent, (a, b) -> Integer.compare(Integer.parseInt(a[0]), Integer.parseInt(b[0])));
        ArrayList<String> questions = new ArrayList<String>();
        ArrayList<String> intents = new ArrayList<String>();
		ArrayList<String> hints = new ArrayList<String>();
		ArrayList<String> lectureref = new ArrayList<String>();
		ArrayList<Double> questionWeight = new  ArrayList<Double>();
		ArrayList<Double> similarityScore = new  ArrayList<Double>();
		ArrayList<String> textlevel = new ArrayList<String>();

        for(int i = 0; i < length ; i++){
        	questions.add(assessmentContent[i][1]);
        	intents.add(assessmentContent[i][2]);
			hints.add(assessmentContent[i][3]);
			lectureref.add(assessmentContent[i][4]);
			questionWeight.add(Double.parseDouble(assessmentContent[i][5]));
			similarityScore.add(0.0);
			textlevel.add("");
			System.out.println(assessmentContent[i][0] + " " + assessmentContent[i][1] + " " + assessmentContent[i][2] + " " + assessmentContent[i][3]+ " " 
			+ assessmentContent[i][4]+ " " + assessmentContent[i][5]);
        } 
        NLUAssessment assessment = new NLUAssessment(quitIntent, questions, intents, hints, helpIntent, type, lectureref, questionWeight, modelType, similarityScore, textlevel);
        this.currentNLUAssessment.put(channel, assessment);
        this.assessmentStarted.put(channel, "true");	
  		
	}
	private void setUpNluAssessment2(JSONObject content , String channel, String quitIntent, String helpIntent, String type, String modelType) throws ParseException {
        JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
		JSONArray Question =(JSONArray) content.get("question");


        int length = Question.size(); 
        String[][] assessmentContent = new String[length][3];
        for(int i = 0; i < length ; i++){
			
				String bodyJson = Question.get(i).toString();  
				JSONObject contentJson = (JSONObject) p.parse(bodyJson);   
				assessmentContent[i][0] = contentJson.getAsString("question");
				assessmentContent[i][1] = contentJson.getAsString("textref"); 
				assessmentContent[i][2] = contentJson.getAsString("questionWeights");
			
			
        }
        
        
       
        Arrays.sort(assessmentContent, (a, b) -> Integer.compare(Integer.parseInt(a[0]), Integer.parseInt(b[0])));
        ArrayList<String> questions = new ArrayList<String>();
		ArrayList<String> lectureref = new ArrayList<String>();
		ArrayList<Double> questionWeight = new  ArrayList<Double>();
		ArrayList<Double> similarityScore = new  ArrayList<Double>();
		ArrayList<String> textlevel = new ArrayList<String>();

        for(int i = 0; i < length ; i++){
        	questions.add(assessmentContent[i][1]);
			lectureref.add(assessmentContent[i][1]);
			questionWeight.add(Double.parseDouble(assessmentContent[i][2]));
			similarityScore.add(0.0);
			textlevel.add("");
			System.out.println(assessmentContent[i][0] + " " + assessmentContent[i][1] + " " + assessmentContent[i][2]);
        } 
        NLUAssessment assessment = new NLUAssessment(quitIntent, questions, null, null, helpIntent, type, lectureref, questionWeight, modelType, similarityScore, textlevel);
        this.currentNLUAssessment.put(channel, assessment);
        this.assessmentStarted.put(channel, "true");	
  		
	}

	private JSONObject continueAssessment(String channel, String intent, JSONObject triggeredBody, String assessmentType){
		JSONObject response = new JSONObject();
		JSONObject error = new JSONObject();
    	String answer = "";    	
    	response.put("closeContext", "false");
        if(assessmentType.equals("NLUAssessment")) {
        	NLUAssessment assessment = this.currentNLUAssessment.get(channel);
	        if(intent.equals(assessment.getQuitIntent())){
	        	// Additionall check to see if the quit intent was recognized by accident (Writing "e a c" was recognized as quit once...)
	        	answer += "Assessment is over \n" + "You got " + assessment.getMarks() + "/" + assessment.getCurrentQuestionNumber() + " Questions right! \n"; 
	            if(assessment.getMarks() == assessment.getCurrentQuestionNumber()) {
	            	answer += "You got no questions wrong!";
	            } else answer +=  "You got following Questions wrong: \n" + assessment.getWrongQuestions();
	        	this.assessmentStarted.put(channel, null);
	            response.put("closeContext", "true");
	        } else if(intent.equals(assessment.getHelpIntent())){
	        	answer+= assessment.getQuestionHint() + "\n";
	        	response.put("closeContext", "false");
	        } else { 

		        if(intent.equals(assessment.getCorrectAnswerIntent())){
		            answer += "Correct Answer! \n";
		            assessment.incrementMark(1);
		        } else {
		        	answer += "Wrong answer :/ \n";
		        	assessment.addWrongQuestion();
		        }
		        assessment.incrementCurrentQuestionNumber();
		        if(assessment.getCurrentQuestionNumber() == assessment.getAssessmentSize()){
		        	if(assessment.getMarks() == assessment.getAssessmentSize()) {
		        		answer += "Assessment is over \n" + "You got " + assessment.getMarks() + "/" + assessment.getAssessmentSize() + "Questions right! \n You got no Questions wrong! \n " + assessment.getWrongQuestions();
		        	} else answer += "Assessment is over \n" + "You got " + assessment.getMarks() + "/" + assessment.getAssessmentSize() + "Questions right! \n You got following Questions wrong: \n " + assessment.getWrongQuestions();
		            this.assessmentStarted.put(channel, null);
		            response.put("closeContext", "true");
		        } else {
		            answer += assessment.getCurrentQuestion();        
		        }
	        }
	        
        } else if(assessmentType.equals("NLUAssessmentDe")) {
			NLUAssessment assessment = this.currentNLUAssessment.get(channel);
				if(intent.equals(assessment.getQuitIntent())){
					Double OverallScore = 0.0;
					for(int i=0; i <  assessment.getAssessmentSize(); i++){
						OverallScore += assessment.getSimilarityScoreList().get(i);
						System.out.println("Overallscore is............"+ OverallScore);
					}
					
					answer += "Das Assessment ist fertig \n" + "Du hast insgesamt  " + OverallScore + " % erreicht\n";
					answer+= "Du hast folgende text Komplexitäten:\n";
					int currentQuestion = 0;
					for(int i=0; i <  assessment.getAssessmentSize(); i++){
						answer += "Frage " + (currentQuestion + 1) + ": " + assessment.getLevelList().get(i) + "\n"; 
						currentQuestion += 1;
						System.out.println("level is............"+ assessment.getLevelList().get(i-1));
					}
					this.assessmentStarted.put(channel, null);
					response.put("closeContext", "true");
				} else if(intent.equals(assessment.getHelpIntent())){
					answer+= assessment.getQuestionHint() + "\n";
					response.put("closeContext", "false");
				} else {
					String msg = triggeredBody.getAsString("msg");
					
					String textRef = assessment.gettextReference();
					String modelTyp = assessment.getModelType();
					//MiniClient client = new MiniClient();
					//client.setConnectorEndpoint("");
					//System.out.println("Now connecting");
					//HashMap<String, String> headers = new HashMap<String, String>();
					JSONArray texts = new JSONArray(); 
					texts.add(textRef);
					texts.add(msg);
					JSONObject similarity_Body = new JSONObject();
					similarity_Body.put("texts", texts);
					similarity_Body.put("language", "de");
					similarity_Body.put("corpus", "wikibooks");
					JSONObject complexity_Body = new JSONObject();
					complexity_Body.put("language", "de");
					complexity_Body.put("text", msg);
					JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
					try{
						String email = triggeredBody.getAsString("email");
						JSONObject context = getContext(email, p);	
						StringEntity entity = new StringEntity(similarity_Body.toString());
						HttpClient httpClient = HttpClientBuilder.create().build();
						HttpPost request = new HttpPost("http://rb-controller.ma-zeufack:32446/api/v1/text-similarity");
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
						String score =  ((JSONObject) scores.get(0)).getAsString("score");
						System.out.println("Score 0 is ...... "+ ((JSONObject) scores.get(0)).getAsString("score"));
						System.out.println("Score is ...... "+ score);
						assessment.setSimilarity(Double.parseDouble( ((JSONObject) scores.get(0)).getAsString("score")));
						
						entity = new StringEntity(complexity_Body.toString());
						httpClient = HttpClientBuilder.create().build();
						request = new HttpPost("http://rb-controller.ma-zeufack:32446/api/v1/textual-complexity");
						request.setEntity(entity);
						res = httpClient.execute(request);
						entity2 = res.getEntity();
						String complexity_result = EntityUtils.toString(entity2);
						context.put("complexity_result", complexity_result);
						ContextInfo.put(email, context);
						System.out.println("................result Complexity computed from readerbench................");
						System.out.println(context.getAsString("complexity_result"));
						result = (JSONObject) p.parse(context.getAsString("complexity_result")); 
						p = new JSONParser(JSONParser.MODE_PERMISSIVE);
						data = (JSONObject) result.get("data");
						String level = data.getAsString("level");
						assessment.setLevel(level);

					}  catch (Exception e) {
						e.printStackTrace();
						error.put("text", "Readerbench scheint ein Problem zu haben\n Bitte wendest dich an deinem Tutor");
						error.put("closeContext", true);
						return error;
					}
					

					//ClientResponse SimilarityResult = client.sendRequest("POST", "readerbench/"  + "post/textual_similarity", MediaType.APPLICATION_JSON, headers);
					//Assert.assertEquals(200, SimilarityResult.getHttpCode());
					//ClientResponse ComplexityResult = client.sendRequest("POST", "readerbench/"  + "post/textual_complexity", MediaType.APPLICATION_JSON, headers);
					//Assert.assertEquals(200, ComplexityResult.getHttpCode());
					//TODO: Compute the Assessments
					
					assessment.incrementCurrentQuestionNumber();
					if(assessment.getCurrentQuestionNumber() == assessment.getAssessmentSize()){
						Double OverallScore = 0.0;
						for(int i=0; i <  assessment.getAssessmentSize(); i++){
							OverallScore += assessment.getSimilarityScoreList().get(i);
							System.out.println("Overallscore is............"+ assessment.getSimilarityScoreList().get(i));
						}
						
						answer += "Das Assessment ist fertig \n" + "Du hast insgesamt  " + OverallScore	 + " % erreicht\n";
						answer+= "Du hast folgende text Komplexitäten:\n";
						int currentQuestion = 0;
						for(int i=0; i <  assessment.getAssessmentSize(); i++){
							answer += "Frage " + (currentQuestion + 1) + ": " + assessment.getLevelList().get(i) + "\n"; 
							currentQuestion += 1;
							System.out.println("level is............"+ assessment.getLevelList().get(i));
						}
						
						
						this.assessmentStarted.put(channel, null);
						response.put("closeContext", "true");
					} else {
						answer += assessment.getCurrentQuestion();        
					}
				}
	        
        } else if(assessmentType.equals("moodleAssessment")) {
        	MoodleQuiz quiz = this.currentMoodleAssessment.get(channel);
        	String msg = triggeredBody.getAsString("msg");
	        if(intent.equals(quiz.getQuitIntent()) && !quiz.checkIfAnswerToQuestion(msg)) {
	        		answer += "Assessment is over \n" + "Your final mark is *" + quiz.getMarks() + "/" + (quiz.getTotalMarksUntilCurrentQuestion() - quiz.getMarkForCurrentQuestion()) + "* \n";  	
		            if(quiz.getMarks() == ((quiz.getTotalMarksUntilCurrentQuestion() - quiz.getMarkForCurrentQuestion()))) {
		            	answer += "You got no questions wrong!";
		            } else answer += "You got following Questions wrong: \n " + quiz.getWrongQuestions();
		        	
		            this.assessmentStarted.put(channel, null);
		        	
		            Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_3, quiz.createXAPIForMoodle(false).toString() + "*" + triggeredBody.getAsString("email"));
		            response.put("closeContext", "true");	
	        	
	        	
	        } else { 
	        	
	        	// differ between true false / multiple answers, one answer 
	        	// for multiple choice split with "," to have all the answers
	        	if(quiz.getQuestionType().equals("numerical") || quiz.getQuestionType().equals("shortanswer") ) {
	        		 if(quiz.getAnswer().toLowerCase().equals(msg.toLowerCase())) {
	        			answer += "Correct Answer! \n";
	        			quiz.incrementMark(quiz.getMarkForCurrentQuestion());
	        		 } else {
	        			 answer += "Wrong answer :/ \n";
	 		        	quiz.addWrongQuestion();
	        		 }
	        	} else if(quiz.getQuestionType().equals("truefalse")) {
	        		if(!("true".contains(msg.toLowerCase()) || "false".contains(msg.toLowerCase())) ) {
	        			answer += "Please answer with \"True\" or \"False\"\n";
        				JSONObject userMistake = new JSONObject();
        				userMistake.put("text", answer);
        				userMistake.put("closeContext", "false");
        				return userMistake;
        				}
	        		 if(quiz.getAnswer().toLowerCase().contains(msg.toLowerCase())) {
	        			answer += "Correct Answer! \n";
	 		            quiz.incrementMark(quiz.getMarkForCurrentQuestion());
	        		 } else {
	        			answer += "Wrong answer :/ \n";
	 		        	quiz.addWrongQuestion();
	        		 }
	        	} else if(quiz.getQuestionType().equals("multichoice")) {
	        		
	        		if(quiz.getAnswer().split(";").length <= 2) {
	        			System.out.println("A");
	        			if(msg.length() > 1) {
	        				answer += "Please only enter the letter/number corresponding to the given answers!\n";
	        				JSONObject userMistake = new JSONObject();
	        				userMistake.put("text", answer);
	        				userMistake.put("closeContext", "false");
	        				return userMistake;
	        			} else {
	        				if(!quiz.getAnswerPossibilitiesForMCQ().toLowerCase().contains(msg.toLowerCase())) {
	        					answer += "Please only enter the letter/number corresponding to the given answers!\n";
		        				JSONObject userMistake = new JSONObject();
		        				userMistake.put("text", answer);
		        				userMistake.put("closeContext", "false");
		        				return userMistake;
	        				}
		        			if(quiz.getAnswer().toLowerCase().contains(msg.toLowerCase())) {
			        			answer += "Correct Answer! \n";
			        			quiz.incrementMark(quiz.getMarkForCurrentQuestion());
			        		 } else {
			        			answer += "Wrong answer :/ \n";
			 		        	quiz.addWrongQuestion();
			        		 }
	        			}
	        		} else {
	        			String[] multipleAnswers = quiz.getAnswer().split(";");
	        			String[] userAnswers = msg.split("\\s+");
	        			double splitMark = quiz.getMarkForCurrentQuestion()/(multipleAnswers.length-1);
	        			int numberOfCorrectAnswers = 0;
        				for(int j = 0 ; j < userAnswers.length; j++ ){	
        					if(userAnswers[j].length() > 1 || !quiz.getAnswerPossibilitiesForMCQ().toLowerCase().contains(userAnswers[j].toLowerCase())) {
	        					answer += "Please only enter the letters/numbers corresponding to the given answers!\n";
		        				JSONObject userMistake = new JSONObject();
		        				userMistake.put("text", answer);
		        				userMistake.put("closeContext", "false");
		        				return userMistake;
        					}
        				}
	        			for(int i = 0 ; i < multipleAnswers.length -1 ; i++) {
	        				for(int j = 0 ; j < userAnswers.length; j++ ){
	        					if(userAnswers[j].length() > 1 ) {
	        						continue;
	        					} else if(multipleAnswers[i].toLowerCase().contains(userAnswers[j].toLowerCase())) {
	        						numberOfCorrectAnswers++;
	        						break;
	        					}
	        				}
	        			}
	        			if((multipleAnswers.length-1) == userAnswers.length ) {
	        				if(userAnswers.length == numberOfCorrectAnswers) {
	        					answer += "Correct Answer(s)! \n";
	        					quiz.incrementMark(quiz.getMarkForCurrentQuestion());
	        				} else if(userAnswers.length > numberOfCorrectAnswers) {
	        					// what if 0 points  ?  
	        					if(numberOfCorrectAnswers == 0) {
	        						answer += "Your answers were all wrong\n";
	        					} else answer += "Your answer was partially correct, you got " + numberOfCorrectAnswers + " correct answer(s) and " + (userAnswers.length-numberOfCorrectAnswers) + " wrong one(s)\n";
	        					quiz.incrementMark(splitMark*numberOfCorrectAnswers);
	        					quiz.addWrongQuestion();
	        				} else {
	        					answer += "You somehow managed to get more points than intended\n";
	        					quiz.incrementMark(quiz.getMarkForCurrentQuestion());
	        				}
	        			} else if((multipleAnswers.length-1) > userAnswers.length) {
	        				 quiz.addWrongQuestion();
	        				 if(userAnswers.length > numberOfCorrectAnswers) {  
	        					if(numberOfCorrectAnswers == 0) {
	        						answer += "Your answers were all wrong\n";
	        					} else answer += "Your answer was partially correct, you got " + numberOfCorrectAnswers + " correct answer(s) and " + (userAnswers.length-numberOfCorrectAnswers) + " wrong one(s)\n";
	        					quiz.incrementMark(splitMark*numberOfCorrectAnswers);
	        				} else if(userAnswers.length == numberOfCorrectAnswers) {
	        					answer += "Your answer was partially correct, you got " + numberOfCorrectAnswers + " correct answer(s)\n";
	        					quiz.incrementMark(numberOfCorrectAnswers*splitMark);
	        				} else {
	        					answer += "You somehow managed to get more points than intended\n";
	        					quiz.incrementMark(numberOfCorrectAnswers*splitMark);
	        				}
	        			} else if((multipleAnswers.length-1) < userAnswers.length) {
	        				quiz.addWrongQuestion();
	        				// careful here, - points if someone has too many answers
	        				 answer += "Your answer was partially correct, you got " + numberOfCorrectAnswers + " correct answer(s) and " + (userAnswers.length-numberOfCorrectAnswers) + " wrong one(s)\n";
	        				 int points = numberOfCorrectAnswers - userAnswers.length; 
	        				 if(points >= 0) {
	        					 quiz.incrementMark(numberOfCorrectAnswers*splitMark);
	        				 }
	        			}	
	        		}
	        	}
	        	if(!quiz.getFeedback().equals("")) {
	        		answer += quiz.getFeedback() + "\n";
	        	}
	        	quiz.incrementCurrentQuestionNumber();
		        if(quiz.getCurrentQuestionNumber() == quiz.getAssessmentSize()){
		        	if(quiz.getMarks() == quiz.getMaxMarks()) {
		        		answer += "Assessment is over \n" + "Your final mark is *" + quiz.getMarks() + "/" + quiz.getMaxMarks() + "* \n You got no Questions wrong! \n ";
		        	} else answer += "Assessment is over \n" + "Your final mark is *" + quiz.getMarks() + "/" + quiz.getMaxMarks() + "*  \n You got following Questions wrong: \n " + quiz.getWrongQuestions();
		            this.assessmentStarted.put(channel, null);
		            Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_3, quiz.createXAPIForMoodle(true).toString() + "*" + triggeredBody.getAsString("email"));
		            
		            response.put("closeContext", "true");
		        } else {
		            answer += quiz.getCurrentQuestion() + quiz.getPossibilities() ;        
		        }
	        }
        } else if(assessmentType == "moodleAssessmentDe"){
        	MoodleQuiz quiz = this.currentMoodleAssessment.get(channel);
        	String msg = triggeredBody.getAsString("msg");
	        if(intent.equals(quiz.getQuitIntent()) && !quiz.checkIfAnswerToQuestion(msg)) {
	        	
	        		// add check if lrs is actually available...
		        	answer += "Assessment ist fertig \n" + "Dein Endresultat ist *" + quiz.getMarks() + "/" + (quiz.getTotalMarksUntilCurrentQuestion() - quiz.getMarkForCurrentQuestion()) + "* \n";  	
		            if(quiz.getMarks() == (quiz.getTotalMarksUntilCurrentQuestion() - quiz.getMarkForCurrentQuestion())) {
		            	answer += "Du hast keine falsche Antworten!";
		            } else answer += "Du hast folgende Fragen falsch beantwortet: \n " + quiz.getWrongQuestions();
		        	this.assessmentStarted.put(channel, null);
		        	
		        	Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_3,quiz.createXAPIForMoodle(false).toString() + "*" + triggeredBody.getAsString("email"));
		            
		            response.put("closeContext", "true");
	        	
	        	
	        } else { 
	        	
	        	// differ between true false / multiple answers, one answer 
	        	// for multiple choice split with "," to have all the answers
	        	if(quiz.getQuestionType().equals("numerical") || quiz.getQuestionType().equals("shortanswer") ) {
	        		 if(quiz.getAnswer().toLowerCase().equals(msg.toLowerCase())) {
	        			answer += "Richtige Antwort! \n";
	        			quiz.incrementMark(quiz.getMarkForCurrentQuestion());
	        		 } else {
	        			 answer += "Falsche Antwort :/ \n";
	 		        	quiz.addWrongQuestion();
	        		 }
	        	} else if(quiz.getQuestionType().equals("truefalse")) {
	        		if(!("wahr".contains(msg.toLowerCase()) || "falsch".contains(msg.toLowerCase())) ) {
	        			answer += "Bitte antworte nur mit \"Wahr\" oder \"Falsch\"\n";
        				JSONObject userMistake = new JSONObject();
        				userMistake.put("text", answer);
        				userMistake.put("closeContext", "false");
        				return userMistake;
	        		}
	        		 if(quiz.getAnswer().toLowerCase().contains(msg.toLowerCase())) {
	        			answer += "Richtige Antwort! \n";
	 		            quiz.incrementMark(quiz.getMarkForCurrentQuestion());
	        		 } else {
	        			answer += "Falsche Antwort :/ \n";
	 		        	quiz.addWrongQuestion();
	        		 }
	        	} else if(quiz.getQuestionType().equals("multichoice")) {
	        		
	        		if(quiz.getAnswer().split(";").length <= 2) {
	        			if(msg.length() > 1) {
	        				answer += "Bitte antworte nur mit den vorgegebenen Buchstaben/Zahlen!\n";
	        				JSONObject userMistake = new JSONObject();
	        				userMistake.put("text", answer);
	        				userMistake.put("closeContext", "false");
	        				return userMistake;
	        			} else {
	        				System.out.println(quiz.getAnswerPossibilitiesForMCQ());
	        				if(!quiz.getAnswerPossibilitiesForMCQ().toLowerCase().contains(msg.toLowerCase())) {
	        					answer += "Bitte antworte nur mit den vorgegebenen Buchstaben/Zahlen!\n";
		        				JSONObject userMistake = new JSONObject();
		        				userMistake.put("text", answer);
		        				userMistake.put("closeContext", "false");
		        				return userMistake;
	        				}
		        			if(quiz.getAnswer().toLowerCase().contains(msg.toLowerCase())) {
			        			answer += "Richtige Antwort! \n";
			        			quiz.incrementMark(quiz.getMarkForCurrentQuestion());
			        		 } else {
			        			answer += "Falsche Antwort :/ \n";
			 		        	quiz.addWrongQuestion();
			        		 }
	        			}
	        		} else {
	        			String[] multipleAnswers = quiz.getAnswer().split(";");
	        			String[] userAnswers = msg.split("\\s+");
	        			double splitMark = quiz.getMarkForCurrentQuestion()/(multipleAnswers.length-1);
	        			int numberOfCorrectAnswers = 0;
        				for(int j = 0 ; j < userAnswers.length; j++ ){	
        					if(userAnswers[j].length() > 1 || !quiz.getAnswerPossibilitiesForMCQ().toLowerCase().contains(userAnswers[j].toLowerCase())) {
	        					answer += "Bitte antworte nur mit den vorgegebenen Buchstaben/Zahlen!\n";
		        				JSONObject userMistake = new JSONObject();
		        				userMistake.put("text", answer);
		        				userMistake.put("closeContext", "false");
		        				return userMistake;
        					}
        				}
	        			for(int i = 0 ; i < multipleAnswers.length -1 ; i++) {
	        				for(int j = 0 ; j < userAnswers.length; j++ ){
	        					if(userAnswers[j].length() > 1 ) {
	        						continue;
	        					} else if(multipleAnswers[i].toLowerCase().contains(userAnswers[j].toLowerCase())) {
	        						numberOfCorrectAnswers++;
	        						break;
	        					}
	        				}
	        			}
	        			if((multipleAnswers.length-1) == userAnswers.length ) {
	        				if(userAnswers.length == numberOfCorrectAnswers) {
	        					answer += "Richtige Antwort(en)! \n";
	        					quiz.incrementMark(quiz.getMarkForCurrentQuestion());
	        				} else if(userAnswers.length > numberOfCorrectAnswers) {
	        					// what if 0 points  ?  
	        					if(numberOfCorrectAnswers == 0) {
	        						answer += "Deine Antworten waren alle falsch\n";
	        					} else answer += "Deine Antwort war teilweise richtig, du hast " + numberOfCorrectAnswers + " richtige Antwort(en) und " + (userAnswers.length-numberOfCorrectAnswers) + " falsche\n";
	        					quiz.incrementMark(splitMark*numberOfCorrectAnswers);
	        					quiz.addWrongQuestion();
	        				} else {
	        					answer += "Du hast mehr Punkte bekommen als vorgegeben?!\n";
	        					quiz.incrementMark(quiz.getMarkForCurrentQuestion());
	        				}
	        			} else if((multipleAnswers.length-1) > userAnswers.length) {
	        				 quiz.addWrongQuestion();
	        				 if(userAnswers.length > numberOfCorrectAnswers) {  
	        					if(numberOfCorrectAnswers == 0) {
	        						answer += "Deine Antworten waren alle falsch\n";
	        					} else answer += "Deine Antwort war teilweise richtig, du hast " + numberOfCorrectAnswers + " richtige Antwort(en) und " + (userAnswers.length-numberOfCorrectAnswers) + " falsche\n";
	        					quiz.incrementMark(splitMark*numberOfCorrectAnswers);
	        				} else if(userAnswers.length == numberOfCorrectAnswers) {
	        					answer += "Deine Antwort war teilweise richtig, du hast " + numberOfCorrectAnswers + " richtige Antwort(en)\n";
	        					quiz.incrementMark(numberOfCorrectAnswers*splitMark);
	        				} else {
	        					answer += "Du hast mehr Punkte bekommen als vorgegeben?!\n";
	        					quiz.incrementMark(numberOfCorrectAnswers*splitMark);
	        				}
	        			} else if((multipleAnswers.length-1) < userAnswers.length) {
	        				quiz.addWrongQuestion();
	        				// careful here, - points if someone has too many answers
	        				 answer += "Deine Antwort war teilweise richtig, du hast " + numberOfCorrectAnswers + " richtige Antwort(en) und " + (userAnswers.length-numberOfCorrectAnswers) + " falsche\n";
	        				 int points = numberOfCorrectAnswers - userAnswers.length; 
	        				 if(points >= 0) {
	        					 quiz.incrementMark(numberOfCorrectAnswers*splitMark);
	        				 }
	        			}	
	        		}
	        	}
	        	if(!(quiz.getFeedback().equals(""))) {
	        		answer += quiz.getFeedback() + "\n";
	        	}
	        	quiz.incrementCurrentQuestionNumber();
		        if(quiz.getCurrentQuestionNumber() == quiz.getAssessmentSize()){
		        	if(quiz.getMarks() == quiz.getAssessmentSize()) {
		        		answer += "Assessment ist fertig \n" + "Dein Endresultat ist *" + quiz.getMarks() + "/" + quiz.getMaxMarks() + "* \n Du hast keine falsche Fragen \n " + quiz.getWrongQuestions();
		        	} else answer += "Assessment ist fertig \n" + "Dein Endresultat ist *" + quiz.getMarks() + "/" + quiz.getMaxMarks() + "*  \n Du hast folgende Fragen falsch beantwortet: \n " + quiz.getWrongQuestions();
		            this.assessmentStarted.put(channel, null);
		            Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_3, quiz.createXAPIForMoodle(true).toString() + "*" + triggeredBody.getAsString("email"));
		            response.put("closeContext", "true");
		        } else {
		            answer += quiz.getCurrentQuestion() + quiz.getPossibilities() ;        
		        }
	        }
        	
        } else {
        	System.out.println("Assessment type: "+ assessmentType + " not known");
        }
        response.put("text", answer);
        return response;
	}
	
	/*public Response putTopic(@PathParam("name") String name, BotModel body) {
		Connection con = null;
		PreparedStatement ps = null;
		Response resp = null;
		
		try {
			// Open database connection
			con = service.database.getDataSource().getConnection();
			
			// Write serialised model in Blob
			ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bOut);
			out.writeObject(body);
			Blob blob = con.createBlob();
			blob.setBytes(1, bOut.toByteArray());
			
			// Check if model with given name already exists in database. If yes, update it. Else, insert it
			ps = con.prepareStatement("SELECT * FROM models WHERE name = ?");
			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ps.close();
				ps = con.prepareStatement("UPDATE models SET model = ? WHERE name = ?");
				ps.setBlob(1, blob);
				ps.setString(2, name);
				ps.executeUpdate();
			} else {
				ps.close();
				ps = con.prepareStatement("INSERT INTO models(name, model) VALUES (?, ?)");
				ps.setString(1, name);
				ps.setBlob(2, blob);
				ps.executeUpdate();
			}
			
			resp = Response.ok().entity("Model stored.").build();
		} catch (SQLException e) {
			e.printStackTrace();
			resp = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		} catch (IOException e) {
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
		
		return resp;
	}*/



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
		JSONArray results = new JSONArray();;
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

	/**
	 * function which extracts the category from the catagory types. If more than one row are provided a Chatexception is thrown
	 * @param mensas Resultset containing the mensas as rows
	 * @return Object containing the name and id of the mensa
	 * @throws ChatException the error message contains a list of mensas in the set
	 */
	private String selectCategoryMsg()
	{
		Set<String> selection = new HashSet<String>();
		selection.add("Semantische Kohaesionsindizes: lokaler und globaler Zusammenhalt ");
		selection.add("Diskursstruktur-Indizes:  Textorganisation");
		selection.add("Morphologische Indizes: Formen von Wörtern, insbesondere flektierte Formen");
		selection.add("Isochrony (Sprechrhythmus)");
		selection.add("Oberflaechenindizes: Form des Textes");
		selection.add("Syntaktische Indizes: Anordnung von Wörtern und Phrasen");
		selection.add("Wortkomplexitaetsindizes:  Komplexität von Wörtern über ihre Form hinaus");

		String response = ""
				+ " welche aspecte der Text würdest du überprüfen?\n"
				+ "Du kannst dir eine aussuchen: \n";

		Iterator<String> it = selection.iterator();
		int i = 1;
		while(it.hasNext()){
			response += i + ". " + it.next() + "\n";
			i++;
		}

		response += "Bitte Kategorie eingeben";
		return response;
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
