class MainController
  isReady: false

  constructor: (@$scope, @actorService) ->
    console.log '[AW]MainController constructor'
    @$scope.$on 'actorReady', =>
      @$scope.$apply =>
        @isReady = true

MainController.$inject = ['$scope', 'actorService']

angular
  .module 'actorWeb'
  .controller 'mainController', MainController
