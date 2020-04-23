Vue.component('homepage', {
	data: function() {
		return {
			host: null,
			socket: null,
			logged: null,
			loggedUsers: null,
			registeredUsers: null,
			content: "",
			subject: "",
			reciever: "",
			messageType: null,
			messages: null
		}
	},
	template:
		`
			<div>
			<div v-if="logged">
				<h3>Logged In Users</h3>
				<table>
					<thead>
						<tr>
							<th>Username</th>
						</tr>
					</thead>
					<tbody>
						<tr v-for="user in loggedUsers">
							<td>{{user.username}}</td>
						</tr>
					</tbody>
				</table>
				<h3>Registered Users</h3>
				<table>
					<thead>
						<tr>
							<th>Username</th>
						</tr>
					</thead>
					<tbody>
						<tr v-for="user in registeredUsers">
							<td>{{user.username}}</td>
						</tr>
					</tbody>
				</table>
				<div>
					<h3>Send Message</h3>
					<form method="post" v-on:submit.prevent="sendMessage">
						<label>
							<strong>Message Type</strong>
							<select v-model="messageType">
								<option>All</option>
								<option>Private</option>
							</select>
						</label> <br/>
						<label v-if="messageType==='Private'">To:</label><input v-if="messageType==='Private'" type="text" required v-model="reciever"> <br/>
						<label>Subject:</label><input type="text" required v-model="subject"> <br/>
						<label>Content:</label><input type="text" required v-model="content"> <br/>
						<button type="submit">Send</button>
					</form>
					<h3>Chat</h3>	
					<div id="consoleLog"></div>
					<h3>User messages</h3>
					<div id="messages" v-for="msg in messages">
						<div>{{msg.content}}</div>
					</div>
				</div>			
			</div>
			<div v-else>
				<h1>Register or login to send messages</h1>
			</div>
		</div>
		`,
	mounted: function() {
		var a = this;
		var user = localStorage.getItem("user");
		
		if (!(user === null)) {
			axios.get('rest/chat/users/loggedIn')
			.then(function(response) {
				a.loggedUsers = response.data;
			})
			.catch(function(error) {
				alert(error.response.data);
			});
			
			axios.get('rest/chat/users/registered')
				.then(function(response) {
					a.registeredUsers = response.data;
			})
				.catch(function(error) {
					alert(error.response.data);
			});
			
			axios.get('rest/chat/messages/' + user)
				.then(function(response) {
					a.logged = app.logged;
					a.messages = response.data;
				})
				.catch(function(error) {
					alert(error.response.data);
			});
			
		
			var socket;
			var host = "ws://localhost:8080/domaci-war/ws/" + user;
			try{
			    socket = new WebSocket(host);
			    console.log('connect: Socket Status: '+socket.readyState);
			
			    socket.onopen = function(){
			   	 	console.log('onopen: Socket Status: '+socket.readyState+' (open)');
			    }
			
			    socket.onmessage = function(msg){
					let message = msg.data;
					let messageElem = document.createElement('div');
					messageElem.textContent = message;
				    
			   	 	console.log('onmessage: Received: '+ msg.data);
			   	 	document.getElementById('consoleLog').append(messageElem);
			    }
			
			    socket.onclose = function(){
			    	socket = null;
			    }			
			
			} catch(exception){
			   	console.log('Error'+exception);
			}
			
			a.socket = socket;
			a.host = host;
		}
	},
	methods: {
		sendMessage: function() {
			var a = this;
			var sender = {
				"username": localStorage.getItem("user")
			}
			var rec = {
				"username": a.reciever
			}
			
			
			
			if (a.messageType === "Private") {
				message = {
					"reciever": rec,
					"sender": sender,
					"date": new Date(),
					"subject": a.subject,
					"content": a.content
				}
				
				axios.post('rest/chat/messages/user', message)
				.then(function(response) {
					a.subject = "";
					a.content = "";
					a.reciever = "";
					alert("Message sent!");
				})
				.catch(function(error) {
					alert(error.response.data);
				});
				
			} else {
				var message = {
					"reciever": null,
					"sender": sender,
					"date": new Date(),
					"subject": a.subject,
					"content": a.content
				}
				axios.post('rest/chat/messages/all', message)
					.then(function(response) {
						a.subject = "";
						a.content = "";
						alert("Message sent!");
					})
					.catch(function(error) {
						alert(error.response.data);
				});
			}
		}
	}
});