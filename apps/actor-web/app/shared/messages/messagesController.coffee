class MessagesController
  list: []
  typing: null
  messageDebounce: 30 # time in ms after which the message is considered read

  constructor: (@$rootScope, @$scope, @$timeout, @actorService) ->
    console.log '[AW]MessagesController constructor'

    @$scope.$on 'selectDialog', (event, peer) =>
      console.log '[AW]MessagesController: selectDialog fired.' #, peer
      if @actorService.currentPeer == peer
        console.log '[AW]MessagesController selectDialog: this peer already selected.'
        return
      if @actorService.currentPeer
        console.log '[AW]MessagesController selectDialog: conversation already opened: unbind...'
        @actorService.unbindChat @actorService.currentPeer, @renderMessages
        @actorService.unbindTyping peer, @renderTyping

      @actorService.bindChat peer, @renderMessages
      @actorService.bindTyping peer, @renderTyping

  renderMessages: (messages) =>
    console.log '[AW]MessagesController renderMessages' #, messages
    @list = messages

  renderTyping: (typing) =>
    console.log '[AW]MessagesController renderTyping'
    @$timeout =>
      @typing = typing.typing

  setViewed: (sortKey, isOut) ->
    console.log '[AW]MessagesController setViewed' #, sortKey, isOut
    @actorService.onMessageShown @actorService.currentPeer, sortKey, isOut

MessagesController.$inject = ['$rootScope', '$scope', '$timeout', 'actorService']

angular
  .module 'actorWeb'
  .controller 'messagesController', MessagesController
