package beans;

import java.io.File;
import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.vfs.VirtualFile;

@Singleton
@Startup
@Remote(HostCluster.class)
@Path("/cluster")
public class HostClusterBean implements HostCluster {
	private String master = null;
	private List<String> nodes = new ArrayList<String>();
	private String nodeName;
	private String nodeAddress;
	
	
	@PostConstruct
	private void init() {
		try {
			System.out.println("\nDODAVANJE CVORA\n");
			MBeanServer mbServer = ManagementFactory.getPlatformMBeanServer();
			ObjectName http = new ObjectName("jboss.as:socket-binding-group=standard-sockets,socket-binding=http");
			this.nodeAddress = (String) mbServer.getAttribute(http, "boundAddress");
			this.nodeName = System.getProperty("jboss.node.name") + ":8080";
			
			System.out.println("\nNOVI CVOR: " + nodeName + ":" + nodeAddress);
			
			File f = getFile(HostCluster.class, "", "connections.properties");
			FileInputStream fis = new FileInputStream(f);
			Properties properties = new Properties();
			properties.load(fis);
			fis.close();
			this.master = properties.getProperty("master");
			
			if (master != null && !master.equals("")) {
				System.out.println("\nMASTER NIJE NULL");
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget rtarget = client.target("http://" + master + "domaci-war/rest/cluster");
				HostCluster rest = rtarget.proxy(HostCluster.class);
				this.nodes = rest.newNode(this.nodeName);
				this.nodes.remove(this.nodeName);
				this.nodes.add(this.master);
				

				System.out.println(this.nodes);
			}

			System.out.println("\nMASTER JE NULL");
			System.out.println(this.nodes);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@POST
	@Path("/register")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
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
	
	@POST
	@Path("/node")
	@Consumes(MediaType.APPLICATION_JSON)
	public void addNode(String node) {
		nodes.add(node);
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
