class LoginController
  isCodeRequested: false

  constructor: (@$rootScope, @actorService) ->
    console.log '[AW]LoginController constructor'
    @$rootScope.$state.go 'home' if @actorService.isLoggedIn


  requestCode: (phone) ->
    console.log '[AW]LoginController requestCode'
    @actorService.requestSms phone
    @isCodeRequested = true

  checkCode: (code) ->
    console.log '[AW]LoginController checkCode'
    @actorService.sendCode code

LoginController.$inject = ['$rootScope', 'actorService']

angular
  .module 'actorWeb'
  .controller 'loginController', LoginController
