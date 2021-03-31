/**
 * 
 */
package i5.las2peer.services.readerbenchService.model;

/**
 * @author Karl Zeufack
 *
 */
public class MessageInfo {
	String msg;
	String botName;
	String channel;
	String text;
	String intent;
	String email;
	String category;
	boolean contextOn;
	public MessageInfo(String msg, String botName,	String channel,	
			String text, String intent, String email, String category, boolean contextOn)
	{
		this.msg = msg;
		this.botName = botName;
		this.channel= channel;
		this.text = text;
		this.intent = intent;
		this.email = email;
		this.category = category;
		this.contextOn = contextOn;
	}
	public String msg() {
		return this.msg;
	}
	public String botName() {
		return this.botName;
	}
	public String channel() {
		return this.channel;
	}
	public String text() {
		return this.text;
	}
	public String intent() {
		return this.intent;
	}
	public String email() {
		return this.email;
	}
	public boolean contextOn() {
		return this.contextOn;
	}
	
	

}
