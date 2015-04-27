class ChatsController
  selectedChat: null

  constructor: (@$rootScope, @actorService) ->
    console.log '[AW]ChatsController constructor'
    @$rootScope.$on 'actorReady', => @getChats()

  getChats: ->
    console.log '[AW]ChatsController getChats'
    @actorService.getDialogs (items) => @list = items
    # console.log '[AW]ChatsController @list:', @list

  selectChat: (chat) ->
    console.log '[AW]ChatsController selectChat'
    # console.log '[AW]ChatsController @selectedChat', @selectedChat
    if @selectedChat
      @actorService.closeConversation @selectedChat
    @selectedChat = chat
    @actorService.openConversation @selectedChat
    # @actorService.bindChat @selectedChat



ChatsController.$inject = ['$rootScope', 'actorService']

angular
  .module 'actorWeb'
  .controller 'chatsController', ChatsController
