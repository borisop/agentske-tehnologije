package beans;

import java.util.List;

public interface HostCluster {
	public List<String> newNode(String nodeName);
	
	public void addNode(String node);
}
