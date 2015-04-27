class MessagesController
  list: null

  constructor: (@$rootScope, @actorService) ->
    console.log '[AW]MessagesController constructor'
    @$rootScope.$on 'openConversation', (event, args) =>
      # @actorService.bindChat args, @renderMessages
      @getMessages(args)

  getMessages: (peer) ->
    console.log '[AW]MessagesController getMessages', peer
    # @actorService.getMessages (messages) =>
    #   @list = messages
    #   console.log @list

  renderMessages: ->
    console.log '[AW]MessagesController renderMessages'

MessagesController.$inject = ['$rootScope', 'actorService']

angular
  .module 'actorWeb'
  .controller 'messagesController', MessagesController
