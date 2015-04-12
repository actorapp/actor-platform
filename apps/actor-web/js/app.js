'use strict';

/* App Module */
var messenger, jsAppLoaded; 
  
var App = angular.module('App', [
	'ngRoute',
	'phonecatControllers',
	'actor.controllers',
	'ngSanitize',
	'luegg.directives' 
]);
App.config(['$routeProvider',
function($routeProvider) { 
	    $routeProvider.
	      when('/', {
	        templateUrl: 'partials/welcome.html',
	        controller: 'AppWelcomeController'
	      }).
	      when('/login', {
	        templateUrl: 'partials/login.html',
	        controller: 'AppLoginController'
	      }).
	      when('/im', {
	        templateUrl: 'partials/im.html',
	        controller: 'AppIMController'
	      }). 
	      otherwise({
	        redirectTo: '/'
	      });
}]); 


	  
	  

	   




   
