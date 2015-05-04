class MainController
  constructor: (@$rootScope, @$scope, @$timeout, @$mdSidenav, @$mdMedia, @actorService) ->
    console.log '[AW]MainController constructor'
    @$scope.$on 'actorReady', =>
      @$rootScope.$state.go 'login' if !@actorService.isLoggedIn

    @$scope.$on 'actorLoggedIn', =>
      @renderMyInfo()

    @$scope.$on 'onConversationOpen', (event, peer) =>
      # Unbind if need
      if @actorService.currentPeer
        if peer.type == 'user'
          @unbindUser peer.id
        else if peer.type == 'group'
          @unbindGroup peer.id
      # bind by peer type
      if peer.type == 'user'
        @bindUser peer.id
      else if peer.type == 'group'
        @bindGroup peer.id
      # bind typing


  bindUser: (id) ->
    console.log '[AW]MainController bindUser'
    console.log '[AW]MainController bindUser: id:', id
    @actorService.bindUser id, @renderPeerInfo
  bindGroup: (id) ->
    console.log '[AW]MainController bindGroup'
    console.log '[AW]MainController bindGroup: id:', id
    @actorService.bindGroup id, @renderPeerInfo
  unbindUser: (id) ->
    console.log '[AW]MainController unbindUser'
    console.log '[AW]MainController unbindUser: id:', id
    @actorService.unbindUser id, =>
      console.log '[AW]MainController unbindUser: unbinded'
  unbindGroup: (id) ->
    console.log '[AW]MainController unbindGroup'
    console.log '[AW]MainController unbindGroup: id:', id
    @actorService.unbindGroup id, =>
      console.log '[AW]MainController unbindGroup: unbinded'

  renderMyInfo: () =>
    console.log '[AW]MainController renderMyInfo'
    @$timeout =>
      @user = @actorService.getUser @actorService.getUid()
      console.log '[AW]MainController renderMyInfo: @user:', @user
  renderPeerInfo: (info) =>
    console.log '[AW]MainController renderPeerInfo'
    console.log '[AW]MainController renderPeerInfo: info:', info
    @$timeout =>
      @info = info

  openSidebar: ->
    console.log '[AW]MainController openSidebar'
    @$mdSidenav('left').open()
  onSidebarSwipeLeft: ->
    console.log '[AW]MainController onSidebarSwipeLeft'
    @$mdSidenav('left').close()

MainController.$inject = [
  '$rootScope'
  '$scope'
  '$timeout'
  '$mdSidenav'
  '$mdMedia'
  'actorService'
]

angular
  .module 'actorWeb'
  .controller 'mainController', MainController
