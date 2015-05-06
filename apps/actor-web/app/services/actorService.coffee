class ActorService
  messenger: null
  currentPeer: null

  constructor: (@$rootScope) ->
    console.log '[AW]ActorService constructor'
    window.jsAppLoaded = => @initActor()

  initActor: ->
    console.log '[AW]ActorService initActor'
    @messenger = new actor.ActorApp
    window.messenger = @messenger # for debug
    if @messenger.isLoggedIn()
      console.log '[AW]ActorService initActor: User already logged, redirect to im.'
      @$rootScope.$state.go 'im'
    else
      console.log '[AW]ActorService initActor: User not logged, redirect to login.'
      @$rootScope.$state.go 'login'
    console.log '[AW]ActorService initActor: broadcast: actorReady'
    @$rootScope.$broadcast 'actorReady'


  setLoggedIn: () =>
    console.log '[AW]ActorService setLoggedIn'
#    @isLoggedIn = true
    @$rootScope.$state.go 'im'
#    @$rootScope.$broadcast 'actorLoggedIn'
  setLoggedOut: () =>
    console.log '[AW]ActorService setLoggedOut'
    localStorage.clear()
    location.reload()
#    @isLoggedIn = false
#    @$rootScope.$state.go 'login'
#    @$rootScope.$broadcast 'actorLoggedOut'
  setCurrentPeer: (peer) ->
    console.log '[AW]ActorService setCurrentPeer', peer
    @currentPeer = peer

  bindChat: (peer, callback) ->
    console.log '[AW]ActorService bindChat', peer
    @setCurrentPeer peer
    @messenger.bindChat peer, callback
  bindDialogs: (callback) ->
    console.log '[AW]ActorService bindDialogs'
    @messenger.bindDialogs (dialogs) ->
      console.log '[AW]ActorService bindDialogs: dialogs:', dialogs
      callback dialogs
  bindGroup: (id, callback) ->
    console.log '[AW]ActorService bindGroup', id
    @messenger.bindGroup id, callback
  bindTyping: ->
    console.log '[AW]ActorService bindTyping'
  bindUser: (id, callback) ->
    console.log '[AW]ActorService bindUser', id
    @messenger.bindUser id, callback

  unbindChat: (peer, callback) ->
    console.log '[AW]ActorService unbindChat', peer
    @messenger.unbindChat peer, callback
  unbindDialogs: (callback) ->
    console.log '[AW]ActorService unbindDialogs'
  unbindGroup: (peer, callback) ->
    console.log '[AW]ActorService unbindGroup', peer
    @messenger.unbindGroup peer, callback
  unbindTyping: ->
    console.log '[AW]ActorService unbindTyping'
  unbindUser: (peer, callback) ->
    console.log '[AW]ActorService unbindUser', peer
    @messenger.unbindUser peer, callback

  getAuthPhone: ->
    console.log '[AW]ActorService getAuthPhone'
  getAuthState: ->
    console.log '[AW]ActorService getAuthState'
  getGroup: (uid) ->
    console.log '[AW]ActorService getGroup', uid
    @messenger.getGroup uid
  getTyping: ->
    console.log '[AW]ActorService getTyping'
  getUid: ->
    console.log '[AW]ActorService getUid'
    @messenger.getUid()
  getUser: (uid) ->
    console.log '[AW]ActorService getUser', uid
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
    console.log '[AW]ActorService saveDraft', draft
    if draft != null
      @messenger.saveDraft peer, draft


  requestSms: (phone) ->
    console.log '[AW]ActorService requestSms', phone
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
      console.log '[AW]ActorService sendCode: ok:', state
      switch state
        when 'logged_in'
          @setLoggedIn()
        when 'signup'
          @$rootScope.$broadcast 'actorSignUp'
    , (tag, message, canTryAgain, state) ->
      console.log '[AW]ActorService sendCode: error:', state
      switch state
        when 'code'
          console.log '[AW]ActorService sendCode: wrong code'


  sendFile: (peer, file) ->
    console.log '[AW]ActorService sendFile', file
    @messenger.sendFile peer, file
  sendMessage: (peer, message) ->
    console.log '[AW]ActorService sendMessage', message
    message = message.replace /^\s+|\s+$/g, ''
    if message.length > 0
      console.log '[AW]ActorService sendMessage: message:', message.length
    # console.log '[AW]ActorService sendMessage: peer:', peer
      @messenger.sendMessage peer, message
  sendPhoto: ->
    console.log '[AW]ActorService sendPhoto'

  # Events
  onChatEnd: ->
    console.log '[AW]ActorService onChatEnd'
  onAppHidden: ->
    console.log '[AW]ActorService onAppHidden'
  onAppVisible: ->
    console.log '[AW]ActorService onAppVisible'
  onConversationClosed: (peer) ->
    console.log '[AW]ActorService onConversationClosed', peer
    @messenger.onConversationClosed peer
    @$rootScope.$broadcast 'onConversationClosed', peer
  onConversationOpen: (peer) ->
    console.log '[AW]ActorService onConversationOpen', peer
    @messenger.onConversationOpen peer
    @$rootScope.$broadcast 'onConversationOpen', peer
  onDialogsClosed: ->
    console.log '[AW]ActorService onDialogsClosed'
  onDialogsEnd: ->
    console.log '[AW]ActorService onDialogsEnd'
  onDialogsOpen: ->
    console.log '[AW]ActorService onDialogsOpen'
  onMessageShown: ->
    console.log '[AW]ActorService onMessageShown'
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
