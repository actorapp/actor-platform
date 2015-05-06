class AppController
  isReady: false

  constructor: (@$rootScope, @$scope, @$timeout, @$mdSidenav, @$mdMedia, @actorService) ->
    console.log '[AW]AppController constructor'
    @$timeout =>
      @user = @actorService.getUser @actorService.getUid()

    @$scope.$on 'onConversationOpen', (event, peer) =>
      if peer.type == 'user'
        @actorService.bindUser peer.id, @renderPeerInfo
      else if peer.type == 'group'
        @actorService.bindGroup peer.id, @renderPeerInfo

    @$scope.$on 'onConversationClosed', (event, peer) =>
      if peer.type == 'user'
        @actorService.unbindUser peer.id, =>
          console.log '[AW]AppController unbindUser: unbinded'
      else if peer.type == 'group'
        @actorService.unbindGroup peer.id, =>
          console.log '[AW]AppController unbindGroup: unbinded'

  renderPeerInfo: (info) =>
    console.log '[AW]AppController renderPeerInfo', info
    @$timeout =>
      @info = info

  openSidebar: ->
    console.log '[AW]AppController openSidebar'
    @$mdSidenav('left').open()
  onSidebarSwipeLeft: ->
    console.log '[AW]AppController: onSidebarSwipeLeft'
    @$mdSidenav('left').close()

AppController.$inject = [
  '$rootScope'
  '$scope'
  '$timeout'
  '$mdSidenav'
  '$mdMedia'
  'actorService'
]

angular
  .module 'actorWeb'
  .controller 'appController', AppController
