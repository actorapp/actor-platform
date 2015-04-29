class MainController
  constructor: (@$mdSidenav, @$mdMedia, @actorService) ->
    console.log '[AW]MainController constructor'

  showCompose: ->
    console.log '[AW]MainController showCompose'
    # @$mdBottomSheet.show
    #   templateUrl: 'app/shared/compose/copmoseView.html'
    #   parent: '#content'
    #   disableParentScroll: false

  openSidebar: ->
    console.log '[AW]MainController openSidebar'
    @$mdSidenav('left').toggle()

MainController.$inject = ['$mdSidenav', '$mdMedia', 'actorService']

angular
  .module 'actorWeb'
  .controller 'mainController', MainController
