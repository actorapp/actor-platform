class ActorService
  messenger: null
  currentPeer: null
  isLoggedIn: false

  constructor: (@$rootScope, @$sessionStorage) ->
    console.log '[AW]ActorService constructor'
    window.jsAppLoaded = =>
      @initActor()
      console.log '[AW]ActorService $broadcast: actorReady'
      @$rootScope.$broadcast 'actorReady'

  initActor: ->
    console.log '[AW]ActorService initActor'
    window.messenger = @messenger = new actor.ActorApp
    @isLoggedIn = @messenger.isLoggedIn()
    console.log '[AW]ActorService initActor: @isLoggedIn:', @isLoggedIn
    if @isLoggedIn then @setLoggedIn() else @setLoggedOut()

  # isLoggedIn: ->
  #   @messenger.isLoggedIn()

  # checkAccess: (event, toState, toParams, fromState, fromParams) ->
  #   console.log '[AW]ActorService checkAccess'
  #   if toState.data != undefined
  #     if toState.data.noLogin != undefined && toState.data.noLogin
  #       console.log '[AW]ActorService checkAccess: before login'
  #       return
  #   else
  #     if @isLoggedIn
  #       console.log '[AW]ActorService checkAccess: authenticated'
  #       @$rootScope.isLoggedIn = @$sessionStorage.isLoggedIn
  #     else
  #       console.log '[AW]ActorService checkAccess: redirect to login'
  #       event.preventDefault()
  #       @$rootScope.$state.go 'login'

  setLoggedIn: () =>
    console.log '[AW]ActorService setLoggedIn'
    @$rootScope.$state.go 'home'
    @$rootScope.$broadcast 'actorLoggedIn'

  setLoggedOut: () =>
    console.log '[AW]ActorService setLoggedOut'
    localStorage.clear()
    @isLoggedIn = false
    @$rootScope.$state.go 'login'
    @$rootScope.$broadcast 'actorLoggedOut'

  requestSms: (phone) ->
    console.log '[AW]ActorService requestSms'
    console.log '[AW]ActorService requestSms: phone:', phone
    @messenger.requestSms phone.toString(), (state) =>
      console.log '[AW]ActorService requestSms: state:', state
      if state == 'code'
        console.log '[AW]ActorService requestSms: state: code'
        @$rootScope.$broadcast 'actorAuthCode'
    , (tag, message, canTryAgain, state) ->
      console.log '[AW]ActorService requestSms: error'

  sendCode: (code) ->
    console.log '[AW]ActorService sendCode'
    @messenger.sendCode code, (state) =>
      console.log '[AW]ActorService sendCode: state:', state
      if state == 'logged_in'
        @setLoggedIn()
      if state == 'signup'
        @$rootScope.$broadcast 'actorSignUp'
    , (tag, message, canTryAgain, state) =>
      console.log '[AW]ActorService sendCode: error'
      console.log '[AW]ActorService sendCode: tag, message, canTryAgain, state:', tag, message, canTryAgain, state

  bindDialogs: (callback) ->
    console.log '[AW]ActorService bindDialogs'
    @messenger.bindDialogs (items) ->
      console.log '[AW]ActorService bindDialogs: items', items
      callback items

  onConversationClosed: (peer) ->
    console.log '[AW]ActorService closeConversation'
    console.log '[AW]ActorService closeConversation: peer:', peer
    @messenger.onConversationClosed peer
    @$rootScope.$broadcast 'onConversationClosed', peer

  onConversationOpen: (peer) ->
    console.log '[AW]ActorService openConversation'
    console.log '[AW]ActorService openConversation: peer:', peer
    @setCurrentPeer peer
    @messenger.onConversationOpen peer
    @$rootScope.$broadcast 'onConversationOpen', peer

  bindChat: (peer, callback) ->
    console.log '[AW]ActorService bindChat'
    console.log '[AW]ActorService bindChat: peer:', peer
    @messenger.bindChat peer, callback

  unbindChat: (peer) ->
    console.log '[AW]ActorService unbindChat'
    console.log '[AW]ActorService unbindChat: peer:', peer
    @messenger.unbindChat peer, ->

  setCurrentPeer: (peer) ->
    console.log '[AW]ActorService setCurrentPeer'
    console.log '[AW]ActorService setCurrentPeer: peer:', peer
    @currentPeer = peer

  onTyping: ->
    console.log '[AW]ActorService onTyping'
    @messenger.onTyping @currentPeer

  sendMessage: (peer, message) ->
    console.log '[AW]ActorService sendMessage'
    # console.log '[AW]ActorService sendMessage: message:', message
    # console.log '[AW]ActorService sendMessage: peer:', peer
    @messenger.sendMessage peer, message

  sendFile: (peer, file) ->
    console.log '[AW]ActorService sendFile'
    @messenger.sendFile peer, file

#  getAuthPhone: () ->
#    console.log '[AW]ActorService getAuthPhone'
#    @messenger.getAuthPhone()

  loadDraft: (peer) ->
    console.log '[AW]ActorService loadDraft'
    @messenger.loadDraft peer

  saveDraft: (peer, draft) ->
    console.log '[AW]ActorService saveDraft'
    console.log '[AW]ActorService saveDraft: draft:', draft
    if draft != null
      @messenger.saveDraft peer, draft

  # onGroupOnline: () ->
  #   console.log '[AW]ActorService onGroupOnline'
  #   @$rootScope.$broadcast 'onGroupOnline'

  getUid: ->
    @messenger.getUid()

  getUser: (uid) ->
    console.log '[AW]ActorService getUser'
    @messenger.getUser uid


ActorService.$inject = ['$rootScope', '$sessionStorage']

angular
  .module 'actorWeb'
  .service 'actorService', ActorService
