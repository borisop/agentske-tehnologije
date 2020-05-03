package beans;

import java.util.List;

import javax.ejb.Remote;
import javax.ws.rs.core.Response;

import model.Message;
import model.User;

@Remote
public interface ChatRemote {
	
	public Response login(User user);
	
	public Response register(User user);
	
//	public Response loggedIn();
	public List<User> loggedIn();
	
	public Response registered();
	
	public Response logout(String username);
	
	public Response sendMessageAll(Message message);
	
	public Response sendMessageUser(Message message);
	
	public Response messages(String username);
}
