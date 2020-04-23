const Registration = {template: '<registration></registration>'}
const Login = {template: '<login></login>'}
const Homepage = {template: '<homepage></homepage>'}

const router = new VueRouter({
	mode: 'hash',
	routes: [
		{path: '/register', component: Registration},
		{path: '/login', component: Login},
		{path: '/', component: Homepage}
	]
});

var app = new Vue({
	router,
	el: '#app',
	data: {
		logged : false
	},
	mounted: function() {
		if (localStorage.getItem("user") === null) {
			this.logged = false;
		} else {
			this.logged = true;
		}
	}, 
	methods: {
		logout: function() {
			let user = localStorage.getItem("user");
			localStorage.clear();
			this.logged = false;
			axios.delete('rest/chat/users/loggedIn/' + user)
				.then(function(response) {
					router.push('/login');
					alert("You logged out!")
				})
				.catch(function(error) {
					alert(error.response.data)
				});
		}
	}
});