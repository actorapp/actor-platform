class MessagesController
  list: null

  constructor: (@$rootScope, @actorService) ->
    console.log '[AW]MessagesController constructor'
    @$rootScope.$on 'openConversation', (event, peer) =>
      @getMessages(peer)

  getMessages: (peer) ->
    console.log '[AW]MessagesController getMessages'
    console.log '[AW]MessagesController getMessages: peer:', peer
    @actorService.bindChat peer, @renderMessages

  renderMessages: (messages) =>
    console.log '[AW]MessagesController renderMessages'
    console.log '[AW]MessagesController renderMessages: messages:', messages
    @list = messages


MessagesController.$inject = ['$rootScope', 'actorService']

angular
  .module 'actorWeb'
  .controller 'messagesController', MessagesController
