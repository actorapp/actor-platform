class MainController
  constructor: (@$rootScope, @$scope, @$mdSidenav, @$mdMedia, @actorService) ->
    console.log '[AW]MainController constructor'

    @$scope.$on 'actorReady', =>
      console.log '[AW]MainController constructor: actorReady fired.'
      console.log '[AW]MainController constructor: @actorService.isLoggedIn',  @actorService.isLoggedIn
      @$rootScope.$state.go 'login' if !@actorService.isLoggedIn

  # showCompose: ->
    # console.log '[AW]MainController showCompose'
    # @$mdBottomSheet.show
    #   templateUrl: 'app/shared/compose/copmoseView.html'
    #   parent: '#content'
    #   disableParentScroll: false

  openSidebar: ->
    console.log '[AW]MainController openSidebar'
    @$mdSidenav('left').open()

MainController.$inject = ['$rootScope', '$scope', '$mdSidenav', '$mdMedia', 'actorService']

angular
  .module 'actorWeb'
  .controller 'mainController', MainController
