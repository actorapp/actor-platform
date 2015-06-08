class AppController
  user: undefined
  info: undefined

  constructor: (@$rootScope, @$scope, @$timeout, @$mdSidenav, @$mdMedia, @actorService) ->
    console.log '[AW]AppController: constructor'

    @$scope.$watch 'main.actorService.isReady', (newValue, oldValue) =>
      if newValue
        console.log '[AW]AppController: actorService.isReady.'
        @actorService.bindUser @actorService.getUid(), @renderMyInfo

    @$scope.$watch 'main.actorService.currentPeer', (newValue, oldValue) =>
      console.log '[AW]AppController: actorService.currentPeer changed.'
      if newValue != oldValue
        # unbind old peer
        if oldValue != null
          switch oldValue.type
            when 'user'
              @actorService.unbindUser oldValue.id, @renderPeerInfo
            when 'group'
              @actorService.unbindGroup oldValue.id, @renderPeerInfo

        # bind new peer
        switch newValue.type
          when 'user'
            @actorService.bindUser newValue.id, @renderPeerInfo
          when 'group'
            @actorService.bindGroup newValue.id, @renderPeerInfo


  renderMyInfo: (user) =>
    console.log '[AW]AppController: renderMyInfo'
    @$timeout =>
      @user = user

  renderPeerInfo: (info) =>
    console.log '[AW]AppController: renderPeerInfo'
    @$timeout =>
      @info = info

  openSidebar: ->
    console.log '[AW]AppController: openSidebar'
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
