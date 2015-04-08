
'use strict';

/* Controllers */
 
jsAppLoaded = function(){
	messenger = new actor.ActorApp;  
} 

angular.module('actor.controllers', []) 
	.controller('AppWelcomeController', function($scope, $location) {
		jsAppLoaded = function(){
			messenger = new actor.ActorApp; 
			
			if(messenger.isLoggedIn() == true){
				$scope.$apply(function() {
					$location.path('/im');
				});				
			}
			else{
				$scope.$apply(function() {
					$location.path('/login');
				});				
			}
		} 	
		if(messenger != undefined){
			if(messenger.isLoggedIn() == true){ 
				$location.path('/im'); 
			}	
			else{ 
				$location.path('/login'); 
			}		
		}
			
	
	})
	.controller('AppLoginController', function($scope, $routeParams, $location, focus) {  

		if( messenger == undefined){
			$location.path('/'); 
		}
		if(messenger.isLoggedIn() == true){ 
				$location.path('/im'); 
		} 

		focus('phone_number');

		$scope.codeHide = true; 
		
		$scope.sendPhone = function(user){  
		    messenger.requestSms( user.phone.toString(), function(state) {
		        //Ok 
		        console.log($scope.codeHide);
		        if(state == 'code'){ 
					$scope.$apply(function() {
						$scope.codeHide = false;  
				        $scope.phoneHide = true;
				        focus('activation_code');
					}); 
		        } 
		    }, function(tag, message, canTryAgain, state) { 
		        //Err
		        alert(message); 
		        console.log(state); 
		    });
		    
			console.log(user);
		}
		$scope.sendCode = function(code){
			messenger.sendCode(code.activation_code, function(state) {
				if( state == 'logged_in' ){ 
					$scope.$apply(function() {
						$location.path('/im');
					});
					console.log($location);
				}
				console.log(state);
			}, function(tag, message, canTryAgain, state) { 
			    alert(message); 
			});
		} 
  })
	.controller('AppIMController', function($scope, $location,  $routeParams) { 
		if( messenger == undefined ){
			$location.path('/'); 
		}
		else{
			if(messenger.isLoggedIn() == false){ 
					$location.path('/login'); 
			} 			
		}  
	})
	.controller('AppImDialogsController', function($scope, $location, $timeout, $rootScope) {  
		if( messenger === undefined){ return 0; }
		messenger.bindDialogs(function(items) {  
				items.forEach(function(item) {
					if(item.avatar != null){
						item.pic = '<img class="im_dialog_photo" src="'+item.avatar+'">';
					}
					else{
						console.log(item.peer.id);
						
						var num = parseInt(''+item.peer.peer.id) % 7;
						var text = item.peer.title.substr(0,1);
						if(parseInt(text) >= 0 ){
							text = "#"+text;
						}
						
						item.pic = '<span class="user_bg user_bgcolor_'+num+'">'+text+'</span>';
					}
				});
				$timeout(function() {
	                $scope.$apply(function($scope) {
	                    $scope.dialogs = items;
	                });
				});				

        });
		$scope.dialogSelect = function(peerid){
			$rootScope.$broadcast('selectPeer', peerid); 

			console.log(peerid);
		}

		function isInteger(num) {
		  return (num ^ 0) === num;
		}


	})

	.controller('AppIMChat', function($scope, $location, $timeout) {    
		$scope.historyHide = true;

		$scope.$on('selectPeer', function(event, peerid) {  
			$scope.uid = messenger.getUid();
			$scope.peer = peerid;
			$scope.historyHide = false;
			messenger.bindChat({peerType: "user", peerId: peerid}, renderMessages);
		}); 

		$scope.sendMessage = function(peer,message, $event){
			console.log($event);
			if ($event.which == 13 && $event.shiftKey == false) {
				console.log(1);
				messenger.sendMessage({peerType:"user", peerId: peer}, message.text); 
				
				$scope.message.text  = ""; 
				console.log($scope.message);
			} 
			//messenger.sendMessage({peerType:"user", peerId: peer}, message.text); 
		} 
		function renderMessages(messages){
			//console.log(messages);
			$timeout(function() { 
				$scope.$apply(function($scope) { 
					messages.forEach(function(message){
						if(message.content.text == undefined){
							message.content.text = 'вввs';
						}
						
						if(message.sender.avatar != null){
							item.pic = '<img class="im_dialog_photo" src="'+message.sender.avatar+'">';
						}
						else{
							var num = parseInt(''+message.sender.peer.id) % 7;
							var text = message.sender.title.substr(0,1);
							if(parseInt(text) >= 0 ){
								text = "#"+text;
							}
							
							message.pic = '<span class="user_bg user_bgcolor_'+num+'">'+text+'</span>';
						}
					}); 
					$scope.messages = messages;
					
				});				
				
			});
		}
	})

var phonecatControllers = angular.module('phonecatControllers', []);

phonecatControllers.controller('PhoneListCtrl', ['$scope', '$http',
  function($scope, $http) {
    $http.get('phones/phones.json').success(function(data) {
      $scope.phones = data;
    });

    $scope.orderProp = 'age';
  }]);

phonecatControllers.controller('PhoneDetailCtrl', ['$scope', '$routeParams',
  function($scope, $routeParams) {
    $scope.phoneId = $routeParams.phoneId;
  }]); 
