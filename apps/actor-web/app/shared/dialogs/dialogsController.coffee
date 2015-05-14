class DialogsController
  list: undefined

  constructor: (@$rootScope, @$scope, @$timeout, @$mdSidenav, @actorService) ->
    console.log '[AW]DialogsController: constructor'
    @$scope.$watch 'main.actorService.isReady', (newValue, oldValue) =>
      if newValue
        console.log '[AW]DialogsController: actorService.isReady.'
        @actorService.bindDialogs @renderDialogs

  renderDialogs: (dialogs) =>
    console.log '[AW]DialogsController: renderDialogs'
    @$timeout =>
      @list = dialogs

  selectDialog: (peer) ->
    console.log '[AW]DialogsController: selectDialog'
    @actorService.setCurrentPeer peer

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
