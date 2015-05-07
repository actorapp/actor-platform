class AppController
  isReady: false
  user: undefined
  info: undefined

  constructor: (@$rootScope, @$scope, @$timeout, @$mdSidenav, @$mdMedia, @actorService) ->
    console.log '[AW]AppController constructor'
    @actorService.bindUser @actorService.getUid(), (user) => @renderMyInfo user

    @$scope.$on 'onConversationOpen', (event, peer) =>
      switch peer.type
        when 'user'
          @actorService.bindUser peer.id, (info) => @renderPeerInfo info
        when 'group'
          @actorService.bindGroup peer.id, (info) => @renderPeerInfo info

    @$scope.$on 'onConversationClosed', (event, peer) =>
      switch peer.type
        when 'user'
          @actorService.unbindUser peer.id, =>
            console.log '[AW]AppController unbindUser: unbinded'
        when 'group'
          @actorService.unbindGroup peer.id, =>
            console.log '[AW]AppController unbindGroup: unbinded'

  renderMyInfo: (info) =>
    console.log '[AW]AppController renderMyInfo', info
    @$timeout =>
      @user = info

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
