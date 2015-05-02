class SidebarController


  constructor: (@$scope, @$mdSidenav, @actorService) ->
    console.log '[AW]SidebarController constructor'
    @$scope.$on 'actorReady', =>
      console.log '[AW]SidebarController constructor: ActorReady fired.'
      @user = @actorService.getUser @actorService.getUid()
      console.log '[AW]SidebarController constructor: @user:', @user

  onSwipeLeft: ->
    console.log '[AW]SidebarController onSwipeLeft'
    @$mdSidenav('left').close()


SidebarController.$inject = ['$scope', '$mdSidenav', 'actorService']

angular
  .module 'actorWeb'
  .controller 'sidebarController', SidebarController
