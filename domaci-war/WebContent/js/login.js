Vue.component('login', {
	data: function() {
		return {
			username: undefined,
			password: undefined,
		}
	},
	template:
		`
		<div>
			<form method="post" v-on:submit.prevent="login">
				<label>Username:</label><input type="text" required v-model="username"> <br/>
				<label>Password:</label><input type="password" required v-model="password"> <br/>
				<button type="submit">Login</button>
			</form>
		</div>
		`,
	methods: {
		login: function() {
			var a = this;
			
			var h = window.location.host;
			var res = h.split(":");
			
			var host = {
				"alias": res[0],
				"address": res[1]
			}
			
			var user = {
				"username": a.username,
				"password": a.password,
				"host": host
			}
				
			axios.post('rest/chat/users/login', user)
				.then(function(response) {
					window.location.href = "#/";
					a.username = undefined;
					a.password = undefined;
					app.logged = true;
					localStorage.setItem('user', user.username);
				})
				.catch(function(error) {
					alert(error.response.data);
			});
		}
	}
});