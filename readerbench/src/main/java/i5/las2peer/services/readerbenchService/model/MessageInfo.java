package i5.las2peer.services.readerbenchService.model;
import i5.las2peer.services.readerbenchService.chat.*;
import i5.las2peer.services.readerbenchService.nlu.*;

/**
 * @author Karl Zeufack
 *
 */
public class MessageInfo {
	ChatMessage message;
	Intent intent;
	String triggeredFunctionId;
	String botName;
	String serviceAlias;
	boolean contextWithService;

	public MessageInfo(ChatMessage message,	Intent intent,
			String triggeredFunctionId, String botName,
			String serviceAlias, boolean contextWithService)
	{
		this.message = message;
		this.intent = intent;
		this.triggeredFunctionId = triggeredFunctionId;
		this.botName = botName;
		this.serviceAlias = serviceAlias;
		this.contextWithService = contextWithService;
	}

	public ChatMessage getMessage() {
		return this.message;
	}

	public Intent getIntent() {
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
