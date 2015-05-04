class ChatsController
  list: undefined

  constructor: (@$rootScope, @$scope, @$timeout, @$mdSidenav, @actorService) ->
    console.log '[AW]ChatsController constructor'
    console.log '[AW]ChatsController constructor: @isLoggedIn', @actorService.isLoggedIn
    if @actorService.isLoggedIn
      @bindDialogs()
    @$scope.$on 'actorLoggedIn', =>
      console.log '[AW]ChatsController constructor: actorLoggedIn fired.'
      @bindDialogs()

  bindDialogs: ->
    console.log '[AW]ChatsController bindDialogs'
    @actorService.bindDialogs (items) => @renderDialogs items

  renderDialogs: (dialogs) ->
    console.log '[AW]ChatsController renderDialogs'
    console.log '[AW]ChatsController renderDialogs: dialogs:', dialogs
    @$timeout =>
      @list = dialogs
      @$rootScope.$broadcast 'renderDialogs'

  selectChat: (peer) ->
    console.log '[AW]ChatsController selectChat'
    console.log '[AW]ChatsController selectChat: peer:', peer
    if @actorService.currentPeer == peer
      console.log '[AW]ChatsController selectChat: this peer already selected.'
      return
    if @actorService.currentPeer
      console.log '[AW]ChatsController selectChat: conversation already opened: unbind...'
      @actorService.unbindChat @actorService.currentPeer
      @actorService.onConversationClosed @actorService.currentPeer

    @actorService.bindChat peer
    @actorService.onConversationOpen peer

    # Close sidebar
    @$mdSidenav('left').close()

#  getUnreadMessages: (peer) ->
#    console.log '[AW]ChatsController getUnreadMessages'
#    console.log '[AW]ChatsController getUnreadMessages'
#    console.log @actorService.messenger.g.messenger.angularModule.dialogsList.values.array



ChatsController.$inject = [
    '$rootScope'
    '$scope'
    '$timeout'
    '$mdSidenav'
    'actorService'
  ]

angular
  .module 'actorWeb'
  .controller 'chatsController', ChatsController
