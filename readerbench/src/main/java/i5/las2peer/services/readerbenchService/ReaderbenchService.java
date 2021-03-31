package i5.las2peer.services.readerbenchService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
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

import java.util.*;
import java.util.HashSet;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.Gson;

import i5.las2peer.api.Context;
import i5.las2peer.api.security.UserAgent;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.services.readerbenchService.model.MessageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;



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
// TODO Your own service class
public class ReaderbenchService extends RESTService {
	
	private final static List<String> SUPPORTED_FUNCTIONS = Arrays.asList("textual coomplexity",
			"Sentiment");

	 private static HashMap<String, Object> ContextInfo = new HashMap<String, Object>();

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
		    HttpGet httpget = new HttpGet("http://192.168.56.1:6006/api/v1/isalive");
		
		    //Printing the method used
		    System.out.println("Request Type: "+ httpget.getMethod());
		
		   //Executing the Get request
		   HttpResponse response = httpclient.execute(httpget);
		   HttpEntity entity = response.getEntity();
		   String result = EntityUtils.toString(entity);
		   System.out.println("................"+result);
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
	
	
	/*
	@POST
	@Path("/text-similarity")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	@ApiOperation(
			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "Example method that returns a phrase containing the received input.")
	public Response textsimilarity(String categorie, String language, String corpus) {
		JSONObject j = new JSONObject();
		j.put("language", "en");
		//j.put("texts", texts);
		j.put("corpus", corpus);
		try {
			StringEntity entity = new StringEntity(j.toString());
			HttpClient httpClient = HttpClientBuilder.create().build();
	        HttpPost request = new HttpPost("http://192.168.56.1:6006/api/v1/text-similarity");
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
	*/
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
	        HttpPost request = new HttpPost("http://192.168.56.1:6006/api/v1/feedback");
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
		     JSONObject entities = (JSONObject) p.parse(bodyJson.getAsString("entities"));
		     event.put("task", "textualComplexity");

		     
		     switch (intent) {
			     case "quit":
			          chatResponse.put("text", "Okay Aufwiedersehen. 🙃");
			          chatResponse.put("closeContext", true);
			          return Response.ok(chatResponse).build();
			     case "text":
			    	 text = bodyJson.getAsString("msg");
			    	 JSONObject j = new JSONObject();
			    	 j.put("language", "en");
			    	 j.put("text", text);
			    	 try {
			    		 StringEntity entity = new StringEntity(j.toString());
			 			HttpClient httpClient = HttpClientBuilder.create().build();
			 	        HttpPost request = new HttpPost("http://192.168.56.1:6006/api/v1/textual-complexity");
			 	        request.setEntity(entity);
			 	        HttpResponse response = httpClient.execute(request);
			 	        HttpEntity entity2 = response.getEntity();
			 			String result = EntityUtils.toString(entity2);
			 			context.put("result", result);
			 			System.out.println("................result computed from readerbench................");  
			 			chatResponse.put("closeContext", false);
			 			String res = selectCategoryMsg();
			 			chatResponse.put("text",res);
				    	chatResponse.put("closeContext", false);
				    	return Response.ok().entity(chatResponse).build();
			 		}catch (IOException e) {
			 			// TODO Auto-generated catch block
			 			e.printStackTrace();
			 			throw new ChatException(
			 					e.getMessage()
			   		        );
			 		}
			 	
			    	 
			     case "kategorie":
			    	 if(entities == null) {
			    		 throw new ChatException(
			    		          "Aspekte wurde nicht erkannt. Bitte neue angeben"
			    		        );
			    		 
			    	 }
			    	 else {
			    		 if (context.getAsString("category") != null) {
			    			 context.remove("category");
			    		 }
			    		 String category = entities.getAsString("category");
			    		 context.put("category", category);
			    		 ContextInfo.put(email, context);
			    	 }
			    	 String res = selectLevelMsg();
			    	 chatResponse.put("text",res);
			    	 chatResponse.put("closeContext", false);
			    	 return Response.ok().entity(chatResponse).build();
			     case "level":
			    	 if(entities == null) {
			    		 throw new ChatException(
			    				 "Shicht wurde nicht erkannt. Bitte wieder angeben"
			    				 );
			    	 }
			    	 else {
			    		 if (context.getAsString("level") != null) {
			    			 context.remove("level");
			    		 }
			    		 String level = entities.getAsString("level");
			    	 }
			    	 JSONObject category = (JSONObject) p.parse(context.getAsString("category"));
				     JSONObject level = (JSONObject) p.parse(context.getAsString("level"));
				     String res1 = selectIndices(category, level);
				     res1+="\n";
				     res1+="Um ein weitere level für die Kategorie "+ context.getAsString("category")
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
			     	String res11 = selectCategoryMsg();
		 			chatResponse.put("text",res11);
			    	chatResponse.put("closeContext", false);
			    	return Response.ok().entity(chatResponse).build();
			     case "new_Level":
			    	 if (context.getAsString("level") != null) {
		    			 context.remove("level");
		    		 }
			    	 String res = selectLevelMsg();
			    	 chatResponse.put("text",res);
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
			 }
		     JSONObject category = (JSONObject) p.parse(context.getAsString("category"));
		     JSONObject level = (JSONObject) p.parse(context.getAsString("level"));
		     String result = selectIndices(category, level);
		     return Response.ok().entity(chatResponse).build();
		     }
		catch (ChatException e) {
		         chatResponse.appendField("text", e.getMessage());
		         chatResponse.put("closeContext", false);
		         return Response.ok().entity(chatResponse).build();
		       } catch (Exception e) {
		         e.printStackTrace();
		         chatResponse.appendField("text", "Sorry, a problem occured 🙁");
		         return Response.ok(chatResponse).build();
		       }
	}
			

	private String selectLevelMsg() {
		// TODO Auto-generated method stub
		return null;
	}

	private String selectIndices(JSONObject category, JSONObject level) {
		// TODO Auto-generated method stub
		return null;
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
    throws ChatException, SQLException {
	  	Set<String> selection = new HashSet<String>();
		selection.add("Semantische Kohaesionsindizes: lokaler und globaler Zusammenhalt ");
		selection.add("Diskursstruktur-Indizes:  Textorganisation");
		selection.add("Morphologische Indizes: Formen von Wörtern, insbesondere flektierte Formen");
		selection.add("Isochrony (Sprechrhythmus)");
		selection.add("Oberflaechenindizes: Form des Textes");
		selection.add("Syntaktische Indizes: Anordnung von Wörtern und Phrasen");
		selection.add("Wortkomplexitaetsindizes:  Komplexität von Wörtern über ihre Form hinaus");
		
		String response = "Der Text wurde bearbeitet, welche aspecte der Text würdest du überprüfen: \n";

		Iterator<String> it = selection.iterator();
		int i = 1;
		while(it.hasNext()){
			response += i + ". " + it.next() + "\n";
			i++;
		}
		
		response += "Bitte Kategorie eingeben";
    	throw new ChatException(response);
	}

	 /**
   * Retrieves an Category from a list of categories
   * @param categoryname categoryname for the menuitem function will match it to
   * @return Indicevalue as json
   * @throws IOException
   * @throws ChatException
   */
  private JSONObject extractCategoryFromIndices(
    String categoryname, JSONObject IndiceResults
  ) 
    throws IOException, ChatException {
	JSONObject indices;
    for (Object item : indices) {
      JSONObject obj = (JSONObject) item;

      if (
        obj.getAsString("category").matches("(?i).*" + categoryname + ".*") 
      ) return obj;
    }

    throw new ChatException("Could not find a results for " + categoryname + "ðŸ’�\n ");
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
