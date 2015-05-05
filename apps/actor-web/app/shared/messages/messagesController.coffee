class MessagesController
  list: null

  constructor: (@$rootScope, @$scope, @$timeout, @actorService) ->
    console.log '[AW]MessagesController constructor'
    @$scope.$on 'onConversationOpen', (event, peer) =>
      console.log '[AW]MessagesController constructor: onConversationOpen fired.'
      @bindChat peer

  bindChat: (peer) ->
    console.log '[AW]MessagesController getMessages'
    console.log '[AW]MessagesController getMessages: peer:', peer
    @actorService.bindChat peer, @renderMessages

  renderMessages: (messages) =>
    console.log '[AW]MessagesController renderMessages'
    console.log '[AW]MessagesController renderMessages: messages:', messages
    @$timeout =>
      for message in messages
        if message.content.content == 'text'
          message.content.text = message.content.text.replace(/\n/g, '<br/>')
      @list = messages
      @$rootScope.$broadcast 'renderMessages'


MessagesController.$inject = ['$rootScope', '$scope', '$timeout', 'actorService']

angular
  .module 'actorWeb'
  .controller 'messagesController', MessagesController
