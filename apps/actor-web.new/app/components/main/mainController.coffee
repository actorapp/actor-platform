class MainController
  constructor: (@$mdSidenav, @$mdMedia, @$mdBottomSheet) ->
    console.log 'MainController'
    console.log 'openBottomSheet'
    @$mdBottomSheet.show
      templateUrl: 'app/shared/bottomSheet/bottomSheetView.html'
      parent: '#content'

  openSidebar: ->
    console.log 'openSidebar'
    @$mdSidenav('left').toggle()

MainController.$inject = ['$mdSidenav', '$mdMedia', '$mdBottomSheet']

angular
  .module 'actorWeb'
  .controller 'mainController', MainController
