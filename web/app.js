var search = new Vue({
	el: '#search',
	data: {
		message: 'Hello Vue!'
	}
});
document.addEventListener('DOMContentLoaded', function() {
	var elems = document.querySelectorAll('.collapsible');
	var instances = M.Collapsible.init(elems, {}/*options*/);
});

var normalSearch = new Vue({
	el: '#normal_search',
	data: {
		text: "",
	},
	methods: {
	}
});

var result = new Vue({
	el: '#result',
	data: {res:[{"google.com":1.0}]},
	methods: {
	}
});

var advanceSearch = new Vue({
	el: '#advance_search',
	data: {
		must: [{value:""}],
		must_not: [{value:""}],
		should: [{value:""}],
	},
	methods: {
		add_must: function () {
			this.must.push({value: ""});
		},
		add_must_not: function () {
			this.must_not.push({value:""});
		},
		add_should: function () {
			this.should.push({value:""});
		}
	}
});



var search_button = new Vue({
	el: '#search_button',
	methods: {
		click: function () {
			let params, method;
			let safety = document.getElementById('safety').checked;
			let pageRank = document.getElementById('pageRank').checked;

			if (document.getElementsByClassName("active").item(0).getAttribute('id') === 'advance_search') {
				params = {
					must: advanceSearch.must.map(item => item.value),
					must_not: advanceSearch.must_not.map(item => item.value),
					should: advanceSearch.should.map(item => item.value),
					safety,pageRank
				};
				method = "advanceSearch";
			}
			else {
				params = {
					text: normalSearch.text,
					safety,pageRank
				};
				method = "normalSearch";
			}
			console.log(method, params);
			jQuery.post("http://localhost:8080",JSON.stringify({method, params, jsonrpc:"2.0", id:Date.now()}), item=> {
				result.res = JSON.parse(item).result;
			});
		}
	}
});