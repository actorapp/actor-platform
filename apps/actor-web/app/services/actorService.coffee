class ActorService
  isLoggedIn: false
  messenger: null
  currentPeer: null

  constructor: (@$rootScope) ->
    console.log '[AW]ActorService constructor'
    window.jsAppLoaded = => @initActor()

  initActor: ->
    console.log '[AW]ActorService initActor'
    @messenger = new actor.ActorApp
    window.messenger = @messenger
    @setLoggedIn() if @messenger.isLoggedIn()
    console.log '[AW]ActorService initActor: @isLoggedIn:', @isLoggedIn
    console.log '[AW]ActorService initActor: $broadcast: actorReady'
    @$rootScope.$broadcast 'actorReady'


  setLoggedIn: () =>
    console.log '[AW]ActorService setLoggedIn'
    @isLoggedIn = true
    @$rootScope.$state.go 'home'
    @$rootScope.$broadcast 'actorLoggedIn'
  setLoggedOut: () =>
    console.log '[AW]ActorService setLoggedOut'
    localStorage.clear()
    @isLoggedIn = false
    @$rootScope.$state.go 'login'
    @$rootScope.$broadcast 'actorLoggedOut'
    location.reload()
  setCurrentPeer: (peer) ->
    console.log '[AW]ActorService setCurrentPeer'
    console.log '[AW]ActorService setCurrentPeer: peer:', peer
    @currentPeer = peer

  bindChat: (peer, callback) ->
    console.log '[AW]ActorService bindChat'
    console.log '[AW]ActorService bindChat: peer:', peer
    @setCurrentPeer peer
    @messenger.bindChat peer, callback
  bindDialogs: (callback) ->
    console.log '[AW]ActorService bindDialogs'
    @messenger.bindDialogs (dialogs) ->
      console.log '[AW]ActorService bindDialogs: dialogs:', dialogs
      callback dialogs
  bindGroup: (peer) ->
    console.log '[AW]ActorService bindGroup'
    console.log '[AW]ActorService bindGroup: peer:', peer
  bindTyping: ->
    console.log '[AW]ActorService bindTyping'
  bindUser: (peer) ->
    console.log '[AW]ActorService bindUser'
    console.log '[AW]ActorService bindUser: peer:', peer

  unbindChat: (peer, callback) ->
    console.log '[AW]ActorService unbindChat'
    console.log '[AW]ActorService unbindChat: peer:', peer
    @messenger.unbindChat peer, callback
  unbindDialogs: ->
    console.log '[AW]ActorService unbindDialogs'
  unbindGroup: ->
    console.log '[AW]ActorService unbindGroup'
  unbindTyping: ->
    console.log '[AW]ActorService unbindTyping'
  unbindUser: ->
    console.log '[AW]ActorService unbindUser'

  getAuthPhone: ->
    console.log '[AW]ActorService getAuthPhone'
  getAuthState: ->
    console.log '[AW]ActorService getAuthState'
  getGroup: ->
    console.log '[AW]ActorService getGroup'
  getTyping: ->
    console.log '[AW]ActorService getTyping'
  getUid: ->
    console.log '[AW]ActorService getUid'
    @messenger.getUid()
  getUser: (uid) ->
    console.log '[AW]ActorService getUser'
    @messenger.getUser uid

  clearChat: ->
    console.log '[AW]ActorService clearChat'
  deleteChat: ->
    console.log '[AW]ActorService deleteChat'

  # Draft
  loadDraft: (peer) ->
    console.log '[AW]ActorService loadDraft'
    @messenger.loadDraft peer
  saveDraft: (peer, draft) ->
    console.log '[AW]ActorService saveDraft'
    console.log '[AW]ActorService saveDraft: draft:', draft
    if draft != null
      @messenger.saveDraft peer, draft


  requestSms: (phone) ->
    console.log '[AW]ActorService requestSms'
    console.log '[AW]ActorService requestSms: phone:', phone
    @messenger.requestSms phone.toString(), (state) =>
      console.log '[AW]ActorService requestSms: state:', state
      switch state
        when 'code'
          console.log '[AW]ActorService requestSms: $broadcast actorAuthCode'
          @$rootScope.$broadcast 'actorAuthCode'
    , (tag, message, canTryAgain, state) ->
      console.log '[AW]ActorService requestSms: error: state:', state
  sendCode: (code) ->
    console.log '[AW]ActorService sendCode'
    @messenger.sendCode code, (state) =>
      console.log '[AW]ActorService sendCode: state:', state
      switch state
        when 'logged_in'
          @setLoggedIn()
        when 'logged_in'
          @$rootScope.$broadcast 'actorSignUp'
    , (tag, message, canTryAgain, state) ->
      console.log '[AW]ActorService sendCode: error: state:', state


  sendMessage: (peer, message) ->
    console.log '[AW]ActorService sendMessage'
    console.log '[AW]ActorService sendMessage: message:', message
    message = message.replace /^\s+|\s+$/g, ''
    if message.length > 0
      console.log '[AW]ActorService sendMessage: message:', message.length
    # console.log '[AW]ActorService sendMessage: peer:', peer
      @messenger.sendMessage peer, message


  # Events
  onAppHidden: ->
    console.log '[AW]ActorService onAppHidden'
  onAppVisible: ->
    console.log '[AW]ActorService onAppVisible'
  onConversationClosed: (peer) ->
    console.log '[AW]ActorService onConversationClosed'
    console.log '[AW]ActorService onConversationClosed: peer:', peer
    @messenger.onConversationClosed peer
    @$rootScope.$broadcast 'onConversationClosed', peer
  onConversationOpen: (peer) ->
    console.log '[AW]ActorService onConversationOpen'
    console.log '[AW]ActorService onConversationOpen: peer:', peer
    @messenger.onConversationOpen peer
    @$rootScope.$broadcast 'onConversationOpen', peer
  onDialogsClosed: ->
    console.log '[AW]ActorService onDialogsClosed'
  onDialogsOpen: ->
    console.log '[AW]ActorService onDialogsOpen'
  onProfileClosed: ->
    console.log '[AW]ActorService onProfileClosed'
  onProfileOpen: ->
    console.log '[AW]ActorService onProfileOpen'
  onTyping: ->
    console.log '[AW]ActorService onTyping'
    @messenger.onTyping @currentPeer

ActorService.$inject = ['$rootScope']

angular
  .module 'actorWeb'
  .service 'actorService', ActorService
