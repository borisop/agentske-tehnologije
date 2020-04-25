package beans;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.jms.ConnectionFactory;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import model.Message;
import model.User;

@Singleton
@Path("/chat")
@LocalBean
public class ChatBean implements ChatRemote, ChatLocal {
	
	private List<User> registeredUsers = new ArrayList<User>();
	
	private List<User> loggedInUsers = new ArrayList<User>();
	
	private Map<String, List<Message>> messages = new HashMap<>();
	
	
	@Resource(mappedName = "java:/ConnectionFactory")
	private ConnectionFactory connectionFactory;
	@Resource(mappedName = "java:jboss/exported/jms/queue/mojQueue")
	private Queue queue;
	
	@GET
	@Path("/test")
	@Produces(MediaType.TEXT_PLAIN)
	public String test() {
		return "OK";
	}
	
	@POST
	@Path("/post/{text}")
	@Produces(MediaType.TEXT_PLAIN)
	public String post(@PathParam("text") String text) {
		System.out.println("Received message: " + text);
		
		try {
			QueueConnection connection = (QueueConnection) connectionFactory.createConnection("guest", "guest.guest.1");
			QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			QueueSender sender = session.createSender(queue);
			TextMessage message = session.createTextMessage();
			message.setText(text);
			sender.send(message);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return "OK";
	}
	
	@POST
	@Path("/users/register")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response register(User user) {
		if (!validateInput(user)) {
			return Response.status(400).entity("Invalid input for username/password!").build();
		}
		if (!checkUsername(user.getUsername())) {
			return Response.status(400).entity("Username is already taken!").build();
		}
		registeredUsers.add(user);
		
		return Response.status(200).build();
	}

	@POST
	@Path("/users/login")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(User user) {
		if (!validateInput(user)) {
			return Response.status(400).entity("Invalid input for username/password!").build();
		}
		if (!validateUser(user)) {
			return Response.status(400).entity("Incorrect username/password!").build();
		}
		if (!checkLogged(user.getUsername())) {
			return Response.status(400).entity("User is already logged in!").build();
		}
		
		loggedInUsers.add(user);
		
		return Response.status(200).build();
	}

	@GET
	@Path("/users/registered")
	@Produces(MediaType.APPLICATION_JSON)
	public Response registered() {
		return Response.ok(registeredUsers).build();
	}
	
	@GET
	@Path("/users/loggedIn")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loggedIn() {
		return Response.ok(loggedInUsers).build();
	}
	
	@DELETE
	@Path("/users/loggedIn/{user}")
	public Response logout(@PathParam("user") String username) {
		User user = null;
		
		for (User u: loggedInUsers) {
			if (u.getUsername().equals(username)) {
				user = new User(u);
				loggedInUsers.remove(u);

				return Response.ok(user).build();
			}
		}
		
		return Response.status(400).entity("User doesn't exist!").build();
	}
	
	@POST
	@Path("/messages/all")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendMessageAll(Message text) {
		System.out.println("Received message: " + text.getContent());
		
		try {
			QueueConnection connection = (QueueConnection) connectionFactory.createConnection("guest", "guest.guest.1");
			QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			QueueSender sender = session.createSender(queue);
			ObjectMessage message = session.createObjectMessage(text);
			sender.send(message);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		sendAll(text);
		
		return Response.ok(text).build();
	}
	
	@POST
	@Path("/messages/user")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendMessageUser(Message text) {
		System.out.println("Received message: " + text.getContent());
		System.out.println("Receiver: " + text.getReciever().getUsername());
		
		if (checkUsername(text.getReciever().getUsername())) {
			return Response.status(400).entity("Reciever with username " + text.getReciever().getUsername() + " doesn't exist!").build();
		}
		
		try {
			QueueConnection connection = (QueueConnection) connectionFactory.createConnection("guest", "guest.guest.1");
			QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			QueueSender sender = session.createSender(queue);
			ObjectMessage message = session.createObjectMessage(text);

			message.setStringProperty("reciever", text.getReciever().getUsername());
			message.setStringProperty("sender", text.getSender().getUsername());
			
			sender.send(message);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		sendMessage(text);
		
		return Response.ok(text).build();
	}
	
	@GET
	@Path("/messages/{user}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response messages(@PathParam("user")String user) {
		if (checkUsername(user)) {
			return Response.status(400).entity("User doesn't exist!").build();
		}
		
		return Response.ok(messages.get(user)).build();
	}
	
	
	public boolean validateInput(User user) {
		if (user.getUsername().equals("") || user.getPassword().equals("")) {
			return false;
		}
		
		return true;
	}
	
	public boolean validateUser(User user) {
		for (User u: registeredUsers) {
			if (u.getUsername().equals(user.getUsername())) {
				if (u.getPassword().equals(user.getPassword())) {
					return true;
				}
				
				return false;
			}
		}
		
		return false;
	}
	
	public boolean checkUsername(String username) {
		for (User u : registeredUsers) {
			if (u.getUsername().equals(username)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean checkLogged(String username) {
		for (User u : loggedInUsers) {
			if (u.getUsername().equals(username)) {
				return false;
			}
		}
		return true;
	}
	
	public void sendMessage(Message msg) {
		String sender = msg.getSender().getUsername();
		String reciever = msg.getReciever().getUsername();
		
		if (!messages.containsKey(sender)) {
			List<Message> msgs = new ArrayList<Message>();
			msgs.add(msg);
			messages.put(sender, msgs);
		} else {
			List<Message> msgs = messages.get(sender);
			msgs.add(msg);
			messages.put(sender, msgs);
		}
		
		if (!messages.containsKey(reciever)) {
			List<Message> msgs = new ArrayList<Message>();
			msgs.add(msg);
			messages.put(reciever, msgs);
		} else {
			List<Message> msgs = messages.get(reciever);
			msgs.add(msg);
			messages.put(reciever, msgs);
		}
	}
	
	public void sendAll(Message msg) {
		for (User u: loggedInUsers) {
			if (!messages.containsKey(u.getUsername())) {
				List<Message> msgs = new ArrayList<Message>();
				msgs.add(msg);
				messages.put(u.getUsername(), msgs);
			} else {
				List<Message> msgs = messages.get(u.getUsername());
				msgs.add(msg);
				messages.put(u.getUsername(), msgs);
			}
		}
	}
	
}
