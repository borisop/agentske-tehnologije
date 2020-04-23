package ws;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;


@Singleton
@ServerEndpoint("/ws/{username}")
@LocalBean
public class WSEndPoint {
//	static List<Session> sessions = new ArrayList<Session>();
	public static Map<String, Session> sessions = new HashMap<>();
	
	@OnOpen
	public void onOpen(Session session, @PathParam("username")String username) {
		if (!sessions.containsValue(session)) {
			sessions.put(username, session);
		}
	}
	
	@OnMessage
	public void echoTextMessage(Session reciever, String msg) {
		try {
			if (reciever == null) {
				for (Map.Entry<String, Session> entry : sessions.entrySet()) {
	        		entry.getValue().getBasicRemote().sendText(msg);
				}
			} else {
				reciever.getBasicRemote().sendText(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@OnClose
	public void close(Session session, @PathParam("username")String username) {
		sessions.remove(username);
	}
	
	@OnError
	public void error(Session session, Throwable t, @PathParam("username")String username) {
		sessions.remove(username);
		t.printStackTrace();
	}
}
