class LoginController
  isCodeRequested: false
  isSignUp: false

  constructor: (@$rootScope, @$scope, @actorService) ->
    console.log '[AW]LoginController constructor'
    @$scope.$on 'actorAuthCode', =>
      @$scope.$apply =>
        @isCodeRequested = true
    @$scope.$on 'actorSignUp', =>
      @$scope.$apply =>
        @isSignUp = true

  requestSms: (phone) ->
    console.log '[AW]LoginController requestSms'
    @actorService.requestSms phone

  sendCode: (code) ->
    console.log '[AW]LoginController sendCode'
    @actorService.sendCode code

  signUp: (name) ->
    console.log '[AW]LoginController signUp'
    console.log '[AW]LoginController signUp' #: name', name

  wrongNumber: ->
    console.log '[AW]LoginController wrongNumber'
    @isCodeRequested = false
    @isSignUp = false


LoginController.$inject = ['$rootScope', '$scope', 'actorService']

angular
  .module 'actorWeb'
  .controller 'loginController', LoginController
