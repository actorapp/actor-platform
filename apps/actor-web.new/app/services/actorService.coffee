class ActorService
  messenger: null

  constructor: (@$rootScope, @$sessionStorage) ->
    console.log '[AW]ActorService constructor'
    @isLoggedIn = @$sessionStorage.isLoggedIn
    window.jsAppLoaded = =>
      @messenger = new actor.ActorApp
      @uid = @messenger.getUid()
      @$rootScope.$broadcast 'actorReady'


  checkAccess: (event, toState, toParams, fromState, fromParams) ->
    if toState.data != undefined
      if toState.data.noLogin != undefined && toState.data.noLogin
        console.log '[AW]ActorService checkAccess: before login'
        return
    else
      if @$sessionStorage.isLoggedIn
        console.log '[AW]ActorService checkAccess: authenticated'
        @$rootScope.isLoggedIn = @$sessionStorage.isLoggedIn
      else
        console.log '[AW]ActorService checkAccess: redirect to login'
        event.preventDefault()
        @$rootScope.$state.go('login')

  setLoggedIn: () =>
    console.log '[AW]ActorService setLoggedIn'
    @isLoggedIn = true
    @$rootScope.isLoggedIn = true
    @$sessionStorage.isLoggedIn = true
    @$rootScope.$state.go('home')
    @$rootScope.$broadcast 'actorLoggedIn'

  setLoggedOut: () =>
    console.log '[AW]ActorService setLoggedOut'
    @isLoggedIn = false
    @$rootScope.isLoggedIn = false
    @$sessionStorage.isLoggedIn = false
    @$rootScope.$state.go('login')
    @$rootScope.$broadcast 'actorLoggedOut'

  requestSms: (phone) ->
    console.log '[AW]ActorService requestSms'
    @messenger.requestSms phone.toString(), (state) ->
      console.log '[AW]ActorService requestSms: state:', state
    , (tag, message, canTryAgain, state) ->
      console.log '[AW]ActorService requestSms: error'

  sendCode: (code) ->
    console.log '[AW]ActorService sendCode'
    @messenger.sendCode code, (state) =>
      console.log '[AW]ActorService sendCode: state:', state
      @setLoggedIn() if state == 'logged_in'
    , (tag, message, canTryAgain, state) ->
      console.log '[AW]ActorService sendCode: error'


  getDialogs: (callback) ->
    console.log '[AW]ActorService getDialogs'
    @messenger.bindDialogs (items) -> callback items

  closeConversation: (peer) ->
    console.log '[AW]ActorService closeConversation'
    @messenger.onConversationClosed peer
    @$rootScope.$broadcast 'closeConversation', peer

  openConversation: (peer) ->
    console.log '[AW]ActorService openConversation', peer
    @messenger.getUid()
    @messenger.onConversationOpen peer
    @$rootScope.$broadcast 'openConversation', peer

  bindChat: (peer, render, callback) ->
    console.log '[AW]ActorService bindChat'#, peer, render
    @messenger.bindChat (peer, render) -> callback items
    # @messenger.bindChat peer, render

  getMessages: (callback) ->


ActorService.$inject = ['$rootScope', '$sessionStorage']

angular
  .module 'actorWeb'
  .service 'actorService', ActorService
