class LoginController
  isCodeRequested: false
  isSignUp: false

  constructor: (@$rootScope, @$scope, @actorService) ->
    console.log '[AW]LoginController constructor'
    @$rootScope.$state.go 'home' if @actorService.isLoggedIn
    @$scope.$on 'actorAuthCode', =>
      console.log '[AW]LoginController constructor: actorAuthCode fired.'
      @$scope.$apply => @isCodeRequested = true
    @$scope.$on 'actorSignUp', =>
      console.log '[AW]LoginController constructor: actorSignUp fired.'
      @$scope.$apply => @isSignUp = true

  requestSms: (phone) ->
    console.log '[AW]LoginController requestSms'
    @actorService.requestSms phone

  sendCode: (code) ->
    console.log '[AW]LoginController sendCode'
    @actorService.sendCode code

  signUp: (name) ->
    console.log '[AW]LoginController signUp'
    console.log '[AW]LoginController signUp: name', name

LoginController.$inject = ['$rootScope', '$scope', 'actorService']

angular
  .module 'actorWeb'
  .controller 'loginController', LoginController
