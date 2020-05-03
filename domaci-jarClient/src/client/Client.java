package client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import beans.ChatRemote;

public class Client {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Context context = new InitialContext();
			String remoteName = "ejb:domaci-ear/domaci-jar/ChatBean!" + ChatRemote.class.getName();
			System.err.println("Looking up for: " + remoteName);
			ChatRemote chat = (ChatRemote) context.lookup(remoteName);
		} catch(NamingException e) {
			e.printStackTrace();
		}
	}

}
