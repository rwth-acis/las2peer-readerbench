package i5.las2peer.services.readerbenchService.model;
import i5.las2peer.services.readerbenchService.chat.*;
import i5.las2peer.services.readerbenchService.nlu.*;

/**
 * @author Karl Zeufack
 *
 */
public class MessageInfoTemp {
	String message;
	String intent;
	String triggeredFunctionId;
	String botName;
	String serviceAlias;
	boolean contextWithService;

	
	public MessageInfoTemp(String message,	String intent,
			String triggeredFunctionId, String botName,
			String serviceAlias, boolean contextWithService)
	{
		this.message = message;
		this.triggeredFunctionId = triggeredFunctionId;
        this.botName = botName;
        this.intent = intent;
		this.serviceAlias = serviceAlias;
		this.contextWithService = contextWithService;
	}

	public String getMessage() {
		return this.message;
	}

	public String getIntent() {
		return this.intent;
	}

	public String getTriggeredFunctionId() {
		return this.triggeredFunctionId;
	}

	public String getBotName() {
		return this.botName;
	}

	public String getServiceAlias() {
		return this.serviceAlias;
	}
	
	public boolean contextActive() {
		return this.contextWithService;
	}
	
	

}
