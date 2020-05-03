package beans;

import java.io.File;
import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrentAccessTimeoutException;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.ServiceUnavailableException;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.vfs.VirtualFile;

import model.Message;
import model.User;

@Singleton
@Startup
@Remote(HostCluster.class)
@Path("/cluster")
public class HostClusterBean implements HostCluster {
	private String master = null;
	private List<String> nodes = new ArrayList<String>();
	private String nodeName;
	private String nodeAddress;
	private List<User> allUsers = new ArrayList<>();
	
	@EJB
	private ChatBean chat;
	
	@PostConstruct
	private void init() {
		try {
			MBeanServer mbServer = ManagementFactory.getPlatformMBeanServer();
			ObjectName http = new ObjectName("jboss.as:socket-binding-group=standard-sockets,socket-binding=http");
			this.nodeAddress = (String) mbServer.getAttribute(http, "boundAddress");
			this.nodeName = System.getProperty("jboss.node.name") + ":8080";
			
			File f = getFile(HostCluster.class, "", "connections.properties");
			FileInputStream fis = new FileInputStream(f);
			Properties properties = new Properties();
			properties.load(fis);
			fis.close();
			this.master = properties.getProperty("master");
			
			if (master != null && !master.equals("")) {
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget rtarget = client.target("http://" + master + "/domaci-war/rest/cluster");
				HostCluster rest = rtarget.proxy(HostCluster.class);
				this.nodes = rest.newNode(this.nodeAddress + ":8080");
//				this.allUsers = rest.allUsers(this.nodeAddress + ":8080");
				this.allUsers = rest.allUsers(master);
				chat.loggedInUsers = this.allUsers;
				this.nodes.remove(this.nodeName);
				this.nodes.add(this.master);
			} else {
				this.master = nodeAddress + ":8080";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public List<String> newNode(String node) {
		for (String n : nodes) {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget rtarget = client.target("http://" + n + "domaci-war/rest/cluster");
			HostCluster rest = rtarget.proxy(HostCluster.class);
			rest.addNode(node);
		}
		nodes.add(node);
		
		return nodes;
	}
	
	@Override
	public void addNode(String node) {
		nodes.add(node);
	}
	
	@Override
	public List<User> nodesUsers() {
		return chat.loggedInUsers;
	}
	
	@Override
	public List<User> allUsers(String node) {
		allUsers = chat.loggedInUsers;
		
		return allUsers;
	}
	
	@Override
	public void removeNode(String alias) {
		nodes.remove(alias);
		removeUsers(alias);
	}
	
	@Override
	public String getNode(){
		return this.nodeAddress;
	}
	
//	@Schedule(hour = "*", minute = "*", second = "*/60")
//	public void heartbeat(){
//		Set<String> corrupted = new HashSet<String>();
//		for (String n : nodes) {
//			try {
//				String ret = checkConnection(n);
//			} catch (ServiceUnavailableException|ConnectException|ConcurrentAccessTimeoutException|ProcessingException e) { 
//				System.out.println("DESILA SE GRESKA PROBAJ PONOVO");
//			}  finally {
//				try {
//					String ret = checkConnection(n);
//				} catch (ServiceUnavailableException|ConnectException|ConcurrentAccessTimeoutException|ProcessingException e) {
//					System.out.println("DESILA SE GRESKA OBRISI CVOR");
//				} finally {
//					corrupted.add(n);
//				}
//			}
//		}
//		Iterator<String> i = corrupted.iterator();
//		
//		System.out.println("TIMER TRIGEROVAN");
//		while(i.hasNext()) {
//			nodes.remove(i.next());
//		}
//		
//		for (String n: nodes) {
//			ResteasyClient client = new ResteasyClientBuilder().build();
//			ResteasyWebTarget rtarget = client.target("http://" + n + "/domaci-war/rest/cluster");
//			HostCluster rest = rtarget.proxy(HostCluster.class);
//			while(i.hasNext()) {
//				rest.removeNode(i.next());
//			}
//		}
//	}
	
	
	public String checkConnection(String n) throws ConnectException, ServiceUnavailableException, ConcurrentAccessTimeoutException, ProcessingException {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget rtarget = client.target("http://" + n + "/domaci-war/rest/cluster");
		HostCluster rest = rtarget.proxy(HostCluster.class);
		String ret = rest.getNode();
		
		return ret;
	}
	
	@Override
	public void informNodes(User user) {	
		for (String n : nodes) {
			if (!n.equals(user.getHost().getAddress())) {
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget rtarget = client.target("http://" + n + "/domaci-war/rest/cluster");
				HostCluster rest = rtarget.proxy(HostCluster.class);
				rest.updateUsers(user);
			}
		}
	}
	
	@Override
	public void updateUsers(User user) {
		if (determineAction(user)) {
			chat.loggedInUsers.add(user);
			chat.informOtherNodes("USER_LOGGED_IN");
		} else {
			User del = null;
			for (User u : chat.loggedInUsers) {
				if (u.getUsername().equals(user.getUsername())) {
					del = u;
					break;
				}
			}
			chat.loggedInUsers.remove(del);
			chat.informOtherNodes("USER_LOGGED_OUT");
		}
	}
	
	@Override
	public void sendMessageUser(Message message) {
		chat.messageFromOtherNode(message);
	}
	
	@Override
	public void sendMessageAll(Message message) {
		chat.messageFromOtherNodeAll(message);
	}
	
	public boolean determineAction(User user) {
		if (allUsers.isEmpty()) {
			return true;
		} else {
			for (User u : allUsers) {
				if (user.getUsername().equals(u.getUsername())) {
					return false;
				}
			}
			return true;
		}
	}
	
	public void removeUsers(String address) {
		Set<User> del = new HashSet<User>();
		for (User u : chat.loggedInUsers) {
			if (address.equals(u.getHost().getAddress())) {
				del.add(u);
			}
		}
		Iterator<User> i = del.iterator();
		while(i.hasNext()) {
			chat.loggedInUsers.remove(i.next());
		}
	}
	
	public static File getFile(Class<?> c, String prefix, String fileName) {
		File f = null;
		
		URL url = c.getResource(prefix + fileName);
		
		if (url != null) {
			if (url.toString().startsWith("vfs:/")) {
				try {
					URLConnection conn = new URL(url.toString()).openConnection();
					VirtualFile vf = (VirtualFile)conn.getContent();
					f = vf.getPhysicalFile();
				} catch (Exception ex) {
					ex.printStackTrace();
					f = new File(".");
				}
			} else {
				try {
					f = new File(url.toURI());
				} catch (URISyntaxException e) {
					e.printStackTrace();
					f = new File(".");
				}
			}
		} else {
			f = new File(fileName);
		}
				
		return f;
	}
}
