Vue.component('registration', {
	data: function() {
		return {
			username: undefined,
			password: undefined,
			retypePassword: undefined
		}
	},
	template:
		`
		<div>
			<form method="post" v-on:submit.prevent="register">
				<label>Username:</label><input type="text" required v-model="username"> <br/>
				<label>Password:</label><input type="password" required v-model="password"> <br/>
				<label>Retype Password:</label><input type="password" required v-model="retypePassword"> <br/>
				<button type="submit">Register</button>
			</form>
		</div>
		`,
	methods: {
		register: function() {
			if (this.password === this.retypePassword) {
				var user = {
					"username": this.username,
					"password": this.password
				}
				
				var a = this;
				
				axios.post('rest/chat/users/register', user)
					.then(function(response) {
						window.location.href = '#/login'
						alert("Successful registration!");
						a.username = undefined;
						a.password = undefined;
						a.retypePassword = undefined;
					})
					.catch(function(error) {
						alert(error.response.data);
					});
			} else {
				alert("Retype password doesn't match!");
			}
		}
	}
});