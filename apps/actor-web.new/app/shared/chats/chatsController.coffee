class ChatsController
  list: undefined

  constructor: (@$scope, @$rootScope, @$timeout, @actorService) ->
    console.log '[AW]ChatsController constructor'
    @$rootScope.$on 'actorReady', =>
      console.log '[AW]ChatsController constructor: actorReady fired.'
      @getConversations()

  getConversations: ->
    console.log '[AW]ChatsController getChats'
    @actorService.bindDialogs (items) => @renderConversations items

  renderConversations: (list) =>
    console.log '[AW]MessagesController renderConversations'
    console.log '[AW]MessagesController renderConversations: list:', list
    @$timeout =>
      @$scope.$apply (@scope) =>
        @list = list
        @$rootScope.$broadcast 'renderConversations'

  selectChat: (peer) ->
    console.log '[AW]ChatsController selectChat'
    if @actorService.currentPeer
      @actorService.closeConversation @actorService.currentPeer

    @actorService.openConversation peer

ChatsController.$inject = ['$scope', '$rootScope', '$timeout', 'actorService']

angular
  .module 'actorWeb'
  .controller 'chatsController', ChatsController
