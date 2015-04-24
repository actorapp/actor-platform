config = ($stateProvider, $urlRouterProvider) ->

  $stateProvider
    .state 'home',
      url: '/'
      views:
        'toolbar': templateUrl: 'app/shared/toolbar/toolbarDefaultView.html'
        'sidebar': templateUrl: 'app/shared/sidebar/sidebarDefaultView.html'
        'content': templateUrl: 'app/components/main/mainView.html'

    .state 'login',
      url: '/login'
      views:
        'content': templateUrl: 'app/components/login/loginView.html'

  $urlRouterProvider
    .otherwise '/'

angular
  .module 'actorWeb'
  .config config
  .run ($rootScope, $state, $stateParams) ->
    $rootScope.$state = $state
    $rootScope.$stateParams = $stateParams
