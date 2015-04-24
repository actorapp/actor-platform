class MainController
  constructor: ->
    console.log 'MainController'

MainController.$inject = []

angular
  .module 'actorWeb'
  .controller 'mainController', MainController
