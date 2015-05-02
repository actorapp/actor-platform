class SidebarController


  constructor: (@$scope, @$timeout, @$mdSidenav, @actorService) ->
    console.log '[AW]SidebarController constructor'
    @$scope.$on 'actorLoggedIn', =>
      console.log '[AW]SidebarController constructor: actorLoggedIn fired.'
      @$timeout =>
        @user = @actorService.getUser @actorService.getUid()
        console.log '[AW]SidebarController constructor: @user:', @user
#    @$scope.$on 'actorReady', =>
#      console.log '[AW]SidebarController constructor: ActorReady fired.'
#      if @actorService.isLoggedIn
#        @$timeout =>
#          @user = @actorService.getUser @actorService.getUid()
#          console.log '[AW]SidebarController constructor: @user:', @user

  onSwipeLeft: ->
    console.log '[AW]SidebarController onSwipeLeft'
    @$mdSidenav('left').close()


SidebarController.$inject = ['$scope', '$timeout', '$mdSidenav', 'actorService']

angular
  .module 'actorWeb'
  .controller 'sidebarController', SidebarController
