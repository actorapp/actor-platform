class ActorService
  messenger: null
  currentPeer: null
  isReady: false

  constructor: (@$rootScope, @$timeout) ->
    console.log '[AW]ActorService: constructor'
    window.jsAppLoaded = @initActor

  initActor: =>
    console.log '[AW]ActorService: initActor'
    @messenger = new actor.ActorApp
    window.messenger = @messenger # for debug
    @$timeout =>
      @isReady = true

#    initActor = ->
#      console.log '[AW]ActorService: initActor'
#      if typeof actor != "undefined"
#        @messenger = new actor.ActorApp
#        window.messenger = @messenger # for debug
#        @isReady = true
#      else
#        setTimeout ->
#          initActor()
#        ,250
#    initActor()

  setLoggedOut: () =>
    console.log '[AW]ActorService: setLoggedOut'
    localStorage.clear()
    location.reload()

  setCurrentPeer: (peer) ->
    console.log '[AW]ActorService: setCurrentPeer'
    @currentPeer = peer

  bindChat: (peer, callback) ->
    console.log '[AW]ActorService: bindChat'
    @messenger.bindChat peer, callback
    @onConversationOpen peer

  bindDialogs: (callback) ->
    console.log '[AW]ActorService: bindDialogs'
    @messenger.bindDialogs (dialogs) ->
      callback dialogs

  bindTyping: (peer, callback) ->
    console.log '[AW]ActorService: bindTyping'
    @messenger.bindTyping peer, callback

  bindGroup: (id, callback) ->
    console.log '[AW]ActorService: bindGroup'
    @messenger.bindGroup id, callback

  bindUser: (id, callback) ->
    console.log '[AW]ActorService: bindUser'
    @messenger.bindUser id, callback

  unbindChat: (peer, callback) ->
    console.log '[AW]ActorService: unbindChat'
    @messenger.unbindChat peer, callback
    @onConversationClosed peer

  unbindDialogs: (callback) ->
    console.log '[AW]ActorService: unbindDialogs'
    @messenger.unbindDialogs (dialogs) ->
      callback dialogs

  unbindGroup: (peer, callback) ->
    console.log '[AW]ActorService: unbindGroup'
    @messenger.unbindGroup peer, callback

  unbindTyping: (peer, callback) ->
    console.log '[AW]ActorService: unbindTyping'
    @messenger.unbindTyping peer, callback

  unbindUser: (peer, callback) ->
    console.log '[AW]ActorService: unbindUser'
    @messenger.unbindUser peer, callback

#  getAuthPhone: ->
#    console.log '[AW]ActorService: getAuthPhone'
#  getAuthState: ->
#    console.log '[AW]ActorService: getAuthState'

  getGroup: (uid) ->
    console.log '[AW]ActorService: getGroup'
    @messenger.getGroup uid

#  getTyping: ->
#    console.log '[AW]ActorService: getTyping'

  getUid: ->
    console.log '[AW]ActorService: getUid'
    @messenger.getUid()

  getUser: (uid) ->
    console.log '[AW]ActorService: getUser'
    @messenger.getUser uid

#  clearChat: ->
#    console.log '[AW]ActorService: clearChat'
#  deleteChat: ->
#    console.log '[AW]ActorService: deleteChat'

  # Draft
  loadDraft: (peer) ->
    console.log '[AW]ActorService: loadDraft'
    @messenger.loadDraft peer

  saveDraft: (peer, draft) ->
    console.log '[AW]ActorService: saveDraft'
    if draft != null
      @messenger.saveDraft peer, draft

  requestSms: (phone) ->
    @messenger.requestSms phone.toString(), (state) =>
      console.log '[AW]ActorService: requestSms: ok'
      switch state
        when 'code'
          console.log '[AW]ActorService: requestSms: broadcast actorAuthCode'
          @$rootScope.$broadcast 'actorAuthCode'
    , (tag, message, canTryAgain, state) ->
      console.log '[AW]ActorService: requestSms: error'

  sendCode: (code) ->
    @messenger.sendCode code, (state) =>
      console.log '[AW]ActorService: sendCode: code accepted'
      switch state
        when 'logged_in'
          console.log '[AW]ActorService: sendCode: logged_in'
          @$rootScope.$state.go 'im'
        when 'signup'
          console.log '[AW]ActorService: sendCode: signup'
          @$rootScope.$broadcast 'actorSignUp'
    , (tag, message, canTryAgain, state) ->
      console.log '[AW]ActorService: sendCode: error'
      switch state
        when 'code'
          console.log '[AW]ActorService: sendCode: wrong code'

  signUp: (name) ->
    @messenger.signUp name, (state) =>
      switch state
        when 'logged_in'
          console.log '[AW]ActorService: signUp: logged_in'
          @$rootScope.$state.go 'im'

  sendFile: (peer, file) ->
    console.log '[AW]ActorService: sendFile'
    @messenger.sendFile peer, file

  sendMessage: (peer, message) ->
    console.log '[AW]ActorService: sendMessage'
    message = message.replace /^\s+|\s+$/g, ''
    if message.length > 0
      @messenger.sendMessage peer, message

  sendPhoto: (peer, file) ->
    console.log '[AW]ActorService: sendPhoto'
    @messenger.sendPhoto peer, file

  # Events
#  onChatEnd: ->
#    console.log '[AW]ActorService: onChatEnd'
#  onAppHidden: ->
#    console.log '[AW]ActorService: onAppHidden'
#  onAppVisible: ->
#    console.log '[AW]ActorService: onAppVisible'

  onConversationClosed: (peer) ->
    console.log '[AW]ActorService: onConversationClosed'
    @messenger.onConversationClosed peer

  onConversationOpen: (peer) ->
    console.log '[AW]ActorService: onConversationOpen'
    @messenger.onConversationOpen peer

#  onDialogsClosed: ->
#    console.log '[AW]ActorService: onDialogsClosed'
#  onDialogsEnd: ->
#    console.log '[AW]ActorService: onDialogsEnd'
#  onDialogsOpen: ->
#    console.log '[AW]ActorService: onDialogsOpen'

  onMessageShown: (peer, sortKey, isOut) ->
    console.log '[AW]ActorService: onMessageShown'
    @messenger.onMessageShown peer, sortKey, isOut

#  onProfileClosed: ->
#    console.log '[AW]ActorService: onProfileClosed'
#  onProfileOpen: ->
#    console.log '[AW]ActorService: onProfileOpen'

  onTyping: ->
    console.log '[AW]ActorService: onTyping'
    @messenger.onTyping @currentPeer

ActorService.$inject = ['$rootScope', '$timeout']

angular
  .module 'actorWeb'
  .service 'actorService', ActorService
