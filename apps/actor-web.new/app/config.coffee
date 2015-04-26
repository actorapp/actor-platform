class ActorConfig
  constructor: ($stateProvider, $urlRouterProvider) ->
    $stateProvider
      .state 'home',
        url: '/'
        views:
          'sidebar': templateUrl: 'app/shared/sidebar/sidebarDefaultView.html'
          'content': templateUrl: 'app/components/main/mainView.html'
          'toolbar': templateUrl: 'app/shared/toolbar/toolbarDefaultView.html'

      .state 'login',
        url: '/login'
        data:
          noLogin: true
        views:
          'content': templateUrl: 'app/components/login/loginView.html'

    $urlRouterProvider
      .otherwise '/'

ActorConfig.$inject = ['$stateProvider', '$urlRouterProvider']

angular
  .module 'actorWeb'
  .config ActorConfig
