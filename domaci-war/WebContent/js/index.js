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

const store = new Vuex.Store({
	state: {
		count: 0,
		host: "",
		socket: null
	},
	mutations: {
		increment (state) {
			state.count++;
		},
		openSocket (state, payload) {
			state.host = payload.newHost;
			try {
				state.socket = new WebSocket(payload.newHost);
			} catch(exception){
			   	console.log('Error'+exception);
			}
		},
		closeSocket (state) {
			state.socket.close();
			state.socket = null;
		}
	}
})


var app = new Vue({
	router,
	el: '#app',
	store: store,
	data: {
		logged: false
	},
	mounted: function() {
		if (localStorage.getItem("user") === null) {
			this.logged = false;
			try {
				this.$store.state.socket.onclose = function(){
//		    	this.$store.commit('closeSocket');
		    	socket = null;
		    }			
			} catch(exception){
				console.log('Error'+exception);
			}
		} else {
			this.logged = true;
		}
	}, 
	methods: {
		logout: function() {
			let user = localStorage.getItem("user");
			localStorage.clear();
			this.logged = false;
			
			this.$store.commit('closeSocket');
			
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