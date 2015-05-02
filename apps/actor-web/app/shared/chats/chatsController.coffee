class ChatsController
  list: undefined

  constructor: (@$rootScope, @$scope, @$timeout, @$mdSidenav, @actorService) ->
    console.log '[AW]ChatsController constructor'
    console.log '[AW]ChatsController constructor: @isLoggedIn', @actorService.isLoggedIn
    if @actorService.isLoggedIn
      console.log '[AW]ChatsController constructor: bindChats() if @actorService.isLoggedIn.'
      @bindDialogs()
    @$scope.$on 'actorLoggedIn', =>
      console.log '[AW]ChatsController constructor: actorLoggedIn fired.'
      @bindDialogs()

  bindDialogs: ->
    console.log '[AW]ChatsController bindDialogs'
    @actorService.bindDialogs (items) => @renderConversations items

  renderConversations: (list) ->
    console.log '[AW]MessagesController renderConversations'
    console.log '[AW]MessagesController renderConversations: list:', list
    @$timeout =>
#      list.forEach (item) ->
#        console.log item
#        @getUnreadMessages (item.peer.id)
      @list = list
      @$rootScope.$broadcast 'renderConversations'

  selectChat: (peer) ->
    console.log '[AW]ChatsController selectChat'

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
