class ChatsController
  constructor: (@$scope, @actorService) ->
    console.log '[AW]ChatsController constructor'
    @$scope.$on 'actor-ready', => @getChats()

  getChats: ->
    console.log '[AW]ChatsController getChats()'
    @actorService.getDialogs (items) =>
      items.forEach (item) ->
        console.log item
        if !item.peer.avatar
          item.peer.avatar = 'http://api.adorable.io/avatars/48/'
          item.peer.avatar += item.peer.peer.id
          item.peer.avatar += '.png';
      @list = items
    console.log '[AW]ChatsController @list:', @list

ChatsController.$inject = ['$scope', 'actorService']

angular
  .module 'actorWeb'
  .controller 'chatsController', ChatsController
