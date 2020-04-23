package ws;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.websocket.Session;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/queue/mojQueue")
})

public class QueueMDB implements MessageListener {
	@EJB
	WSEndPoint ws;
	
	@Override
	public void onMessage(Message arg0) {
		// TODO Auto-generated method stub
		TextMessage tmsg = (TextMessage) arg0;
		String reciever = "";
		String sender = "";
		
		try {
			reciever = tmsg.getStringProperty("reciever");
			sender = tmsg.getStringProperty("sender");
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
		try {
			Session sessionReciever = null;
			if (reciever != null) {
				sessionReciever = ws.sessions.get(reciever);
				ws.echoTextMessage(sessionReciever, tmsg.getText());
			}
			Session sessionSender = ws.sessions.get(sender);
			ws.echoTextMessage(sessionSender, tmsg.getText());
		} catch(JMSException e) {
			e.printStackTrace();
		}
	}

}
