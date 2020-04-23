package model;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private User reciever;
	private User sender;
	private Date date;
	private String subject;
	private String content;
	
	public User getReciever() {
		return reciever;
	}
	public void setReciever(User reciever) {
		this.reciever = reciever;
	}
	public User getSender() {
		return sender;
	}
	public void setSender(User sender) {
		this.sender = sender;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public Message(User reciever, User sender, Date date, String subject, String content) {
		super();
		this.reciever = reciever;
		this.sender = sender;
		this.date = date;
		this.subject = subject;
		this.content = content;
	}
	
	public Message() {
		
	}
	
}
