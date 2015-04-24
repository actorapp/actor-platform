class MainController
  constructor: (@$mdSidenav, @$mdMedia, @$mdBottomSheet) ->
    console.log 'MainController'
#    @$mdBottomSheet.show
#      templateUrl: 'app/shared/bottomSheet/bottomSheetView.html'
#      parent: '#content'

  messages: [
    who: 'Min Li Chan',
    notes: "A robot may not injure a human being or, through inaction, allow a human being to come to harm.
            A robot must obey the orders given it by human beings, except where such orders would conflict with the First Law.
            A robot must protect its own existence as long as such protection does not conflict with the First or Second Law."
  ,
    who: 'Степан',
    notes: "Проверка блин"
  ,
    who: 'Min Li Chan',
    notes: 'Brunch this weekend?',
  ,
    who: 'Min Li Chan',
    notes: " I'll be in your neighborhood doing errands"
  ]

  openSidebar: ->
    console.log 'openSidebar'
    @$mdSidenav('left').toggle()

MainController.$inject = ['$mdSidenav', '$mdMedia', '$mdBottomSheet']

angular
  .module 'actorWeb'
  .controller 'mainController', MainController
