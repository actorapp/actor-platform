class LoginController
  constructor: (@actorService) ->
    console.log '[AW]LoginController constructor'

  isCodeRequested: false

  requestCode: (phone) ->
    console.log '[AW]LoginController requestCode()'
    @actorService.requestSms phone
    @isCodeRequested = true

  checkCode: (code) ->
    console.log '[AW]LoginController checkCode()'
    @actorService.sendCode code

LoginController.$inject = ['actorService']

angular
  .module 'actorWeb'
  .controller 'loginController', LoginController
