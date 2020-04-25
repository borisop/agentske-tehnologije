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
				<div id="loggedInUsers">
					<div v-for="user in loggedUsers">
						<div>{{user.username}}</div>
					</div>		
				</div>
				<h3>Registered Users</h3>
				<div id="registeredUsers">
					<div v-for="user in registeredUsers">
						<div>{{user.username}}</div>
					</div>		
				</div>
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
					<div id="consoleLog">
						<div v-for="msg in messages">
							<div>
								[{{new Date(msg.date).toLocaleString()}}] {{msg.sender.username}}: {{msg.content}}
							</div>
						</div>
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
			var host = "ws://" + window.location.host + "/domaci-war/ws/" + user;
			
			this.$store.commit('openSocket', {
				newHost: host
			});
			
			try{
//			    socket = new WebSocket(host);
			    console.log('connect: Socket Status: '+this.$store.state.socket.readyState);
			
			    this.$store.state.socket.onopen = function(){
//			   	 	console.log('onopen: Socket Status: '+this.$store.state.socket.readyState+' (open)');
			    }
			
			    this.$store.state.socket.onmessage = function(msg){
			    	var users = document.getElementById('loggedInUsers');
		    		var temp = null;
			    	if (msg.data === "USER_LOGGED_IN" || msg.data === "USER_LOGGED_OUT") {
			    		axios.get('rest/chat/users/loggedIn')
						.then(function(response) {
							a.loggedUsers = response.data;
							temp = response.data;
						})
						.catch(function(error) {
							alert(error.response.data);
						});
			    	} else {
						let message = JSON.parse(msg.data);
						let messageElem = document.createElement('div');
						messageElem.textContent = "[" + new Date(message.date).toLocaleString() + "] " 
												+ message.sender.username + ":" + message.content;
//				   	 	console.log('onmessage: Received: '+ msg.data);
				   	 	document.getElementById('consoleLog').append(messageElem);
			    	}
			    	if ((msg.data === "USER_LOGGED_IN" || msg.data === "USER_LOGGED_OUT") && temp != null) {
			    		var html = "";
			    		for (let i = 0; i < temp.length; i++) {
			    			html += "<div>" + temp[i].username + "</div>"
			    		}
			    		users.innerHTML = html;
			    	}
			    }
			    this.$store.state.socket.onclose = function(){
//			    	this.$store.commit('closeSocket');
//			    	socket = null;
			    }			
			} catch(exception){
			   	console.log('Error'+exception);
			}
			
			a.socket = this.$store.state.socket;
			a.host = this.$store.state.host;
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
					})
					.catch(function(error) {
						alert(error.response.data);
				});
			}
		}
	}
});