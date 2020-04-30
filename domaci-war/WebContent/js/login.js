Vue.component('login', {
	data: function() {
		return {
			username: undefined,
			password: undefined,
		}
	},
	template:
		`
		<div class="signup-form">
			<form class="box" method="post" v-on:submit.prevent="login">
				<h1>Login</h1>
				<input class="txtb" type="text" placeholder="Username" required v-model="username"> <br/>
				<input class="txtb" type="password" placeholder="Password" required v-model="password"> <br/>
				<input class="signup-btn" type="submit" value="Sign in">
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