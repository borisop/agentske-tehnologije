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
		<div class="signup-form">
			<form class="box" method="post" v-on:submit.prevent="register">
				<h1>Registration</h1>
				<input class="txtb" type="text" placeholder="Username" required v-model="username"> <br/>
				<input class="txtb" type="password" placeholder="Password" required v-model="password"> <br/>
				<input class="txtb" type="password" placeholder="Retype Password" required v-model="retypePassword"> <br/>
				<input class="signup-btn" type="submit" value="Sign up">
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
						window.location.href = '#/login';
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