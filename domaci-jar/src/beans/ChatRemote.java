package beans;

import javax.ejb.Remote;
import javax.ws.rs.core.Response;

import model.Message;
import model.User;

@Remote
public interface ChatRemote {
	public String post(String text);
	
	public Response login(User user);
	
	public Response register(User user);
	
	public Response loggedIn();
	
	public Response registered();
	
	public Response logout(String username);
	
	public Response sendMessageAll(Message message);
	
	public Response sendMessageUser(Message message);
	
	public Response messages(String username);
}
