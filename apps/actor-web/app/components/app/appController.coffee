class AppController
  user: undefined
  info: undefined

  constructor: (@$rootScope, @$scope, @$timeout, @$mdSidenav, @$mdMedia, @actorService) ->
    console.log '[AW]AppController constructor'
    @actorService.bindUser @actorService.getUid(), (user) => @renderMyInfo user

    @$scope.$on 'onConversationOpen', (event, peer) =>
      switch peer.type
        when 'user'
          @actorService.bindUser peer.id, @renderPeerInfo
        when 'group'
          @actorService.bindGroup peer.id, @renderPeerInfo

    @$scope.$on 'onConversationClosed', (event, peer) =>
      switch peer.type
        when 'user'
          @actorService.unbindUser peer.id, @renderPeerInfo
        when 'group'
          @actorService.unbindGroup peer.id, @renderPeerInfo

  renderMyInfo: (info) =>
    console.log '[AW]AppController renderMyInfo' #, info
    @$timeout =>
      @user = info

  renderPeerInfo: (info) =>
    console.log '[AW]AppController renderPeerInfo' #, info
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
