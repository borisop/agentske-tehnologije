package ws;

import java.util.Map;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.websocket.Session;

import org.codehaus.jackson.map.ObjectMapper;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/queue/mojQueue")
})

public class QueueMDB implements MessageListener {
	@EJB
	WSEndPoint ws;
	
	@Override
	public void onMessage(Message arg0) {
		ObjectMessage omsg = (ObjectMessage) arg0;
		String reciever = "";
		String sender = "";
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			String json = mapper.writeValueAsString(omsg.getObject());
			
			if (json.equals("\"USER_LOGGED_IN\"") || json.equals("\"USER_LOGGED_IN\"")) {
				ws.echoTextMessage(null, json);
			} else {
				reciever = omsg.getStringProperty("reciever");
				sender = omsg.getStringProperty("sender");
				
				Session sessionReciever = null;
				if (reciever != null) {
					sessionReciever = ws.sessions.get(reciever);
					ws.echoTextMessage(sessionReciever, json);
				} else {
					for (Map.Entry<String, Session> entry : ws.sessions.entrySet()) {
		        		if (!entry.getKey().equals(sender)) {
							ws.echoTextMessage(entry.getValue(), json);
						}
					}
				}
			}
		} catch (JMSException e) {
			e.printStackTrace();
		} catch(Exception e1) {
			e1.printStackTrace();
		}
	}

}
