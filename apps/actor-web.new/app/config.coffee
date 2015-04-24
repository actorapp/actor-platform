config = ($stateProvider, $urlRouterProvider) ->

  $stateProvider
    .state 'home',
      url: '/'
      views:
        'toolbar': templateUrl: 'app/shared/toolbar/toolbarDefaultView.html'

    .state 'login',
      url: '/login'
      views:
        'toolbar': templateUrl: 'app/shared/toolbar/toolbarLoginView.html'
        'login': templateUrl: 'app/components/login/loginView.html'

  $urlRouterProvider
    .otherwise '/'

angular
  .module 'actorWeb'
  .config config
  .run ($rootScope, $state, $stateParams) ->
    $rootScope.$state = $state
    $rootScope.$stateParams = $stateParams
