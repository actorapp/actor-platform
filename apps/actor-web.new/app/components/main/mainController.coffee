class MainController
  constructor: (@$mdSidenav, @$mdMedia, @$mdBottomSheet) ->
    console.log 'MainController'
#    @$mdBottomSheet.show
#      templateUrl: 'app/shared/bottomSheet/bottomSheetView.html'
#      parent: '#content'

  messages: [
    face : '/img/list/60.jpeg',
    what: 'Brunch this weekend?',
    who: 'Min Li Chan',
    when: '3:08PM',
    notes: " I'll be in your neighborhood doing errands"
  ,
    face : '/img/list/60.jpeg',
    what: 'Brunch this weekend?',
    who: 'Min Li Chan',
    when: '3:08PM',
    notes: " I'll be in your neighborhood doing errands"
  ,
    face : '/img/list/60.jpeg',
    what: 'Brunch this weekend?',
    who: 'Min Li Chan',
    when: '3:08PM',
    notes: " I'll be in your neighborhood doing errands"
  ,
    face : '/img/list/60.jpeg',
    what: 'Brunch this weekend?',
    who: 'Min Li Chan',
    when: '3:08PM',
    notes: " I'll be in your neighborhood doing errands"
  ,
    face : '/img/list/60.jpeg',
    what: 'Brunch this weekend?',
    who: 'Min Li Chan',
    when: '3:08PM',
    notes: " I'll be in your neighborhood doing errands"
  ,
    face : '/img/list/60.jpeg',
    what: 'Brunch this weekend?',
    who: 'Min Li Chan',
    when: '3:08PM',
    notes: " I'll be in your neighborhood doing errands"
  ,
    face : '/img/list/60.jpeg',
    what: 'Brunch this weekend?',
    who: 'Min Li Chan',
    when: '3:08PM',
    notes: " I'll be in your neighborhood doing errands"
  ,
    face : '/img/list/60.jpeg',
    what: 'Brunch this weekend?',
    who: 'Min Li Chan',
    when: '3:08PM',
    notes: " I'll be in your neighborhood doing errands"
  ,
    face : '/img/list/60.jpeg',
    what: 'Brunch this weekend?',
    who: 'Min Li Chan',
    when: '3:08PM',
    notes: " I'll be in your neighborhood doing errands"
  ,
    face : '/img/list/60.jpeg',
    what: 'Brunch this weekend?',
    who: 'Min Li Chan',
    when: '3:08PM',
    notes: " I'll be in your neighborhood doing errands"
  ,
    face : '/img/list/60.jpeg',
    what: 'Brunch this weekend?',
    who: 'Min Li Chan',
    when: '3:08PM',
    notes: " I'll be in your neighborhood doing errands"
  ]

  openSidebar: ->
    console.log 'openSidebar'
    @$mdSidenav('left').toggle()

MainController.$inject = ['$mdSidenav', '$mdMedia', '$mdBottomSheet']

angular
  .module 'actorWeb'
  .controller 'mainController', MainController
