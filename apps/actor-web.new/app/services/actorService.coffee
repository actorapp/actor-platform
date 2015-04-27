class ActorService
  constructor: (@$rootScope, @$sessionStorage) ->
    console.log '[AW]ActorService constructor'
    @isLoggedIn = @$sessionStorage.isLoggedIn
    window.jsAppLoaded = =>
      @messenger = new actor.ActorApp
      @$rootScope.$broadcast 'actor-ready'

  setLoggedIn: () =>
    console.log '[AW]ActorService setLoggedIn()'
    @isLoggedIn = true
    @$rootScope.isLogedIn = true
    @$sessionStorage.isLogedIn = true
    @$rootScope.$state.go('home')

  setLoggedOut: () =>
    console.log '[AW]ActorService setLoggedOut()'
    @isLoggedIn = false
    @$rootScope.isLogedIn = false
    @$sessionStorage.isLogedIn = false
    @$rootScope.$state.go('login')

  requestSms: (phone) ->
    console.log '[AW]ActorService requestSms()'
    @messenger.requestSms phone.toString(), (state) ->
      console.log '[AW]ActorService requestSms(): state:', state
    , (tag, message, canTryAgain, state) ->
      console.log '[AW]ActorService requestSms(): error'
      console.log '[AW]ActorService requestSms(): tag:', tag
      console.log '[AW]ActorService requestSms(): message:', message
      console.log '[AW]ActorService requestSms(): canTryAgain:', canTryAgain
      console.log '[AW]ActorService requestSms(): state:', state

  sendCode: (code) ->
    console.log '[AW]ActorService sendCode()'
    setLoggedIn = @setLoggedIn
    @messenger.sendCode code, (state) ->
      console.log '[AW]ActorService sendCode(): state:', state
      setLoggedIn() if state == 'logged_in'
    , (tag, message, canTryAgain, state) ->
      console.log '[AW]ActorService sendCode(): error'
      console.log '[AW]ActorService sendCode(): tag:', tag
      console.log '[AW]ActorService sendCode(): message:', message
      console.log '[AW]ActorService sendCode(): canTryAgain:', canTryAgain
      console.log '[AW]ActorService sendCode(): state:', state

  getDialogs: (callback) ->
    console.log '[AW]ActorService getDialogs()'
    @messenger.bindDialogs (items) -> callback items

  checkAccess: (event, toState, toParams, fromState, fromParams) ->
    if toState.data != undefined
      if toState.data.noLogin != undefined && toState.data.noLogin
        console.log '[AW]ActorService checkAccess(): before login'
        return
    else
      if @$sessionStorage.isLogedIn
        console.log '[AW]ActorService checkAccess(): authenticated'
        @$rootScope.isLogedIn = @$sessionStorage.isLogedIn
      else
        console.log '[AW]ActorService checkAccess(): redirect to login'
        event.preventDefault()
        @$rootScope.$state.go('login')

ActorService.$inject = ['$rootScope', '$sessionStorage']

angular
  .module 'actorWeb'
  .service 'actorService', ActorService
