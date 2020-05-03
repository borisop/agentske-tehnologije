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
			messages: null
		}
	},
	template:
		`
		<div id="master-wrap">
			<div class="container">
				<div v-if="logged">
					<div class="content-wrap loggedUsersPanel">
						<h1>Online</h1>
						<div id="loggedInUsers">
							<div v-for="user in loggedUsers">
								<div class="users-logged"><label class="username">{{user.username}}</label></div>
							</div>
						</div>
						<!--<h1>Offline</h1>
						<div id="registeredUsers">
							<div v-for="user in registeredUsers">
								<div class="users-registered"><label class="username">{{user.username}}</label></div>
							</div>				
						</div>--->
					</div>
					<div class="content-wrap sendMessage">
						<div class="message-form">
							<h1>Compose Message</h1>
							<form id="composeMessageForm" class="box" method="post" v-on:submit.prevent="sendMessage">
								<input class="txtb" placeholder="To" type="text" v-model="reciever">
								<input class="txtb" type="text" placeholder="Subject" required v-model="subject">
								<input class="txtb" type="text" placeholder="Content" required v-model="content">
								<input class="signup-btn" type="submit" value="Send">
							</form>
						</div>
					</div>
					<div class="content-wrap chat">
						<h1>Chat</h1>	
						<div id="consoleLog">
							<div v-for="msg in messages">
								<div class="message-container">
									[{{new Date(msg.date).toLocaleString()}}] {{msg.sender.username}}: {{msg.content}}
								</div>
							</div>
						</div>
					</div>			
				</div>
				<div class="content-wrap" v-else>
					<h1>Sign in to send messages</h1>
				</div>
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
			    	if (msg.data === "USER_LOGGED_IN" || msg.data === "USER_LOGGED_OUT" || msg.data === "\"USER_LOGGED_IN\"" || msg.data === "\"USER_LOGGED_OUT\"") {
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
						messageElem.classList.add("message-container")
				   	 	document.getElementById('consoleLog').append(messageElem);
			    	}
			    	if ((msg.data === "USER_LOGGED_IN" || msg.data === "USER_LOGGED_OUT") && temp != null) {
			    		var html = "";
			    		for (let i = 0; i < temp.length; i++) {
			    			html += `<div class="users-logged"><label class="username">` + temp[i].username + "</label></div>"
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
//			a.offlineUsers = difference(a.registeredUsers, a.loggedUsers);
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
			var date = new Date();
			
			let messageElem = document.createElement('div');
			messageElem.textContent = "[" + new Date(date).toLocaleString() + "] " 
									+ sender.username + ":" + a.content;
			messageElem.classList.add("message-container")
	   	 	document.getElementById('consoleLog').append(messageElem);
			
			if (a.reciever === "") {	
				var message = {
					"reciever": null,
					"sender": sender,
					"date": date,
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
			} else {
				message = {
					"reciever": rec,
					"sender": sender,
					"date": new Date(),
					"subject": a.subject,
					"content": a.content
				}
				
				axios.post('rest/chat/messages/user', message)
				.then(function(response) {
					document.getElementById("composeMessageForm").reset();
				})
				.catch(function(error) {
					alert(error.response.data);
				});
			}
		}
	}
});