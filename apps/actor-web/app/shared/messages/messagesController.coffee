class MessagesController
  list: null

  constructor: (@$rootScope, @$scope, @$timeout, @actorService) ->
    console.log '[AW]MessagesController constructor'

    @$scope.$on 'selectDialog', (event, peer) =>
      console.log '[AW]MessagesController: selectDialog fired.', peer
      if @actorService.currentPeer == peer
        console.log '[AW]MessagesController selectDialog: this peer already selected.'
        return
      if @actorService.currentPeer
        console.log '[AW]MessagesController selectDialog: conversation already opened: unbind...'
        @actorService.unbindChat @actorService.currentPeer, =>
          @actorService.onConversationClosed @actorService.currentPeer

      @actorService.bindChat peer, (messages) =>
        @actorService.onConversationOpen peer
        @renderMessages messages

  renderMessages: (messages) ->
    console.log '[AW]MessagesController renderMessages', messages
    @$timeout =>
      @list = messages

MessagesController.$inject = ['$rootScope', '$scope', '$timeout', 'actorService']

angular
  .module 'actorWeb'
  .controller 'messagesController', MessagesController
