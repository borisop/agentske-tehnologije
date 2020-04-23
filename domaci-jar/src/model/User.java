package model;

import java.io.Serializable;

public class User implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String username;
	private String password;
	private Host host;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Host getHost() {
		return host;
	}
	public void setHost(Host host) {
		this.host = host;
	}
	
	public User() {
		
	}
	public User(String username, String password, Host host) {
		super();
		this.username = username;
		this.password = password;
		this.host = host;
	}
	public User(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}
	public User(User u) {
		this.username = u.getUsername();
		this.password = u.getPassword();
		this.host = u.getHost();
	}
	@Override
	public String toString() {
		return "User:" + username;
	}
	
}
