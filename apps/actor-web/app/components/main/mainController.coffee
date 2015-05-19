class MainController
  isFullScreen: false

  constructor: (@$rootScope, @$scope, @actorService) ->
    console.log '[AW]MainController: constructor'

    @$scope.$watch 'main.actorService.messenger', (newValue, oldValue) =>
      return if newValue == oldValue
      if newValue != null
        console.log '[AW]MainController: actorService.isReady.'
        if @actorService.messenger.isLoggedIn()
          console.log '[AW]MainController: user logged, go to im.'
          @$rootScope.$state.go 'im'
        else
          console.log '[AW]MainController: user not logged, go to login.'
          @$rootScope.$state.go 'login'

  toggleFullScreen: ->
    console.log '[AW]AppController: toggleFullScreen'
    @isFullScreen = !@isFullScreen
    angular.element(document.body).toggleClass('fullscreen')

MainController.$inject = ['$rootScope', '$scope', 'actorService']

angular
  .module 'actorWeb'
  .controller 'mainController', MainController
