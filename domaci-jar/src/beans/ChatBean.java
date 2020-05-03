package beans;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.jms.ConnectionFactory;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import model.Host;
import model.Message;
import model.User;

@Singleton
@Path("/chat")
@Remote(ChatRemote.class)
@LocalBean
public class ChatBean implements ChatRemote, ChatLocal {
	
	private List<User> registeredUsers = new ArrayList<User>();
	
	public static List<User> loggedInUsers = new ArrayList<User>();
	
	private Map<String, List<Message>> messages = new HashMap<>();
	
	@Resource(mappedName = "java:/ConnectionFactory")
	private ConnectionFactory connectionFactory;
	@Resource(mappedName = "java:jboss/exported/jms/queue/mojQueue")
	private Queue queue;
	
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
		
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget rtarget = client.target("http://" + user.getHost().getAddress() + "/domaci-war/rest/cluster");
		HostCluster rest = rtarget.proxy(HostCluster.class);
		rest.informNodes(user);
		
		return Response.status(200).build();
	}

	@GET
	@Path("/users/registered")
	@Produces(MediaType.APPLICATION_JSON)
	public Response registered() {
		return Response.ok(registeredUsers).build();
	}
	
//	@GET
//	@Path("/users/loggedIn")
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response loggedIn() {
//		return Response.ok(loggedInUsers).build();
//	}
	@GET
	@Path("/users/loggedIn")
	@Produces(MediaType.APPLICATION_JSON)
	public List<User> loggedIn() {
		return loggedInUsers;
	}
	
	@DELETE
	@Path("/users/loggedIn/{user}")
	public Response logout(@PathParam("user") String username) {
		User user = null;
		
		for (User u: loggedInUsers) {
			if (u.getUsername().equals(username)) {
				user = new User(u);
				loggedInUsers.remove(u);
				
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget rtarget = client.target("http://" + user.getHost().getAddress() + "/domaci-war/rest/cluster");
				HostCluster rest = rtarget.proxy(HostCluster.class);
				rest.informNodes(user);
				
				return Response.ok(user).build();
			}
		}
		
		return Response.status(400).entity("User doesn't exist!").build();
	}
	
	@POST
	@Path("/messages/all")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendMessageAll(Message text) {
		Set<String> addrs = new HashSet<>();
		for (User u : loggedInUsers) {
			if (u.getUsername().equals(text.getSender().getUsername())) {
				text.getSender().setHost(new Host("", u.getHost().getAddress()));
			}
			addrs.add(u.getHost().getAddress());
		}
		addrs.remove(text.getSender().getHost().getAddress());
		
		try {
			QueueConnection connection = (QueueConnection) connectionFactory.createConnection("guest", "guest.guest.1");
			QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			QueueSender sender = session.createSender(queue);
			ObjectMessage message = session.createObjectMessage(text);
			
			message.setStringProperty("sender", text.getSender().getUsername());
			
			sender.send(message);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		Iterator<String> i = addrs.iterator();
		while(i.hasNext()) {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget rtarget = client.target("http://" + i.next() + "/domaci-war/rest/cluster");
			HostCluster rest = rtarget.proxy(HostCluster.class);
			rest.sendMessageAll(text);	
		}
		
		sendAll(text);
		
		return Response.ok(text).build();
	}
	
	@POST
	@Path("/messages/user")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendMessageUser(Message text) {
		if (checkUsernameLogin(text.getReciever().getUsername())) {
			return Response.status(400).entity("Reciever with username " + text.getReciever().getUsername() + " doesn't exist!").build();
		}
		
		for (User u : loggedInUsers) {
			if (u.getUsername().equals(text.getSender().getUsername())) {
				text.getSender().setHost(new Host("", u.getHost().getAddress()));
			} else if (u.getUsername().equals(text.getReciever().getUsername())) {
				text.getReciever().setHost(new Host("", u.getHost().getAddress()));
			}
		}
		
		if (text.getSender().getHost().getAddress().equals(text.getReciever().getHost().getAddress())) {
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
		} else {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget rtarget = client.target("http://" + text.getReciever().getHost().getAddress() + "/domaci-war/rest/cluster");
			HostCluster rest = rtarget.proxy(HostCluster.class);
			rest.sendMessageUser(text);
		}
		
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
	
	public void messageFromOtherNode(Message text) {
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
	}
	
	public void messageFromOtherNodeAll(Message text) {
		try {
			QueueConnection connection = (QueueConnection) connectionFactory.createConnection("guest", "guest.guest.1");
			QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			QueueSender sender = session.createSender(queue);
			ObjectMessage message = session.createObjectMessage(text);

			message.setStringProperty("sender", text.getSender().getUsername());
			
			sender.send(message);
		} catch(Exception e) {
			e.printStackTrace();
		}
		sendAll(text);
	}
	
	public void informOtherNodes(String msg) {
		try {
			QueueConnection connection = (QueueConnection) connectionFactory.createConnection("guest", "guest.guest.1");
			QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			QueueSender sender = session.createSender(queue);
			ObjectMessage message = session.createObjectMessage(msg);
			sender.send(message);
		} catch(Exception e) {
			e.printStackTrace();
		}
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
	
	public boolean checkUsernameLogin(String username) {
		for (User u : loggedInUsers) {
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
