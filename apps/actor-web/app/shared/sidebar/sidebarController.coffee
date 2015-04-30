class SidebarController


  constructor: (@$rootScope, @actorService) ->
    console.log '[AW]SidebarController constructor'
    @$rootScope.$on 'actorReady', =>
      console.log '[AW]SidebarController constructor: ActorReady fired.'
      @user = @actorService.getUser @actorService.getUid()
      console.log '[AW]SidebarController constructor: @user:', @user


SidebarController.$inject = ['$rootScope', 'actorService']

angular
  .module 'actorWeb'
  .controller 'sidebarController', SidebarController
