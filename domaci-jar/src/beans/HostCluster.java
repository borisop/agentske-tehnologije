package beans;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import model.Message;
import model.User;

public interface HostCluster {
	@POST
	@Path("/register")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> newNode(String nodeName);
	
	@POST
	@Path("/node")
	@Consumes(MediaType.APPLICATION_JSON)
	public void addNode(String node);
	
	@POST
	@Path("/node/users/loggedIn")
	@Produces(MediaType.APPLICATION_JSON)
	public List<User> nodesUsers();
	
	@POST
	@Path("/users/loggedIn")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<User> allUsers(String node);
	
	@DELETE
	@Path("/node/{alias}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void removeNode(@PathParam("alias") String alias);
	
	@GET
	@Path("/node")
	public String getNode();
	
	@POST
	@Path("/nodes/inform")
	@Consumes(MediaType.APPLICATION_JSON)
	public void informNodes(User user);
	
	@POST
	@Path("/users/update")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateUsers(User user);
	
	@POST
	@Path("/messages/user")
	@Consumes(MediaType.APPLICATION_JSON)
	public void sendMessageUser(Message message);

	@POST
	@Path("/messages/all")
	@Consumes(MediaType.APPLICATION_JSON)
	public void sendMessageAll(Message message);
	
}
