class LoginController
  isCodeRequested: false
  isSignUp: false

  constructor: (@$rootScope, @$scope, @actorService) ->
    console.log '[AW]LoginController: constructor'
    if @actorService.messenger.isLoggedIn()
      @$rootScope.$state.go 'im'

    @$scope.$on 'actorAuthCode', =>
      @$scope.$apply =>
        @isCodeRequested = true
    @$scope.$on 'actorSignUp', =>
      @$scope.$apply =>
        @isSignUp = true

  requestSms: (phone) ->
    @actorService.requestSms phone

  sendCode: (code) ->
    @actorService.sendCode code

  signUp: (name) ->
    @actorService.signUp name

  wrongNumber: ->
    console.log '[AW]LoginController: wrongNumber'
    @isCodeRequested = false
    @isSignUp = false


LoginController.$inject = ['$rootScope', '$scope', 'actorService']

angular
  .module 'actorWeb'
  .controller 'loginController', LoginController
