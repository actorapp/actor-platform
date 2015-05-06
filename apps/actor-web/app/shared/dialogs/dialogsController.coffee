class DialogsController
  list: undefined

  constructor: (@$rootScope, @$scope, @$timeout, @$mdSidenav, @actorService) ->
    console.log '[AW]DialogsController constructor'
    @actorService.bindDialogs (items) => @renderDialogs items

  renderDialogs: (dialogs) ->
    console.log '[AW]DialogsController renderDialogs:', dialogs
    @$timeout =>
      @list = dialogs

  selectDialog: (peer) ->
    console.log '[AW]DialogsController selectDialog:', peer
    @$rootScope.$broadcast 'selectDialog', peer

    # Close sidebar
    @$mdSidenav('left').close()


DialogsController.$inject = [
  '$rootScope'
  '$scope'
  '$timeout'
  '$mdSidenav'
  'actorService'
]

angular
  .module 'actorWeb'
  .controller 'dialogsController', DialogsController
