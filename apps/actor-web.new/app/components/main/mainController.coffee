class MainController
  constructor: (@$mdSidenav, @$mdMedia, @$mdBottomSheet, @actorService) ->
    console.log '[AW]MainController constructor'

  showBottomSheet: ->
    console.log '[AW]MainController showBottomSheet()'
    @$mdBottomSheet.show
      templateUrl: 'app/shared/bottomSheet/bottomSheetView.html'
      parent: '#content'
      disableParentScroll: false

  openSidebar: ->
    console.log '[AW]MainController openSidebar()'
    @$mdSidenav('left').toggle()

MainController.$inject = ['$mdSidenav', '$mdMedia', '$mdBottomSheet', 'actorService']

angular
  .module 'actorWeb'
  .controller 'mainController', MainController
