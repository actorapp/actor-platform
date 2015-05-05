class ActorConfig
  constructor: ($stateProvider, $urlRouterProvider) ->
    $stateProvider
      .state 'home',
        abstract: true
        views:
          '@': templateUrl: 'app/components/main/mainView.html'

      .state 'im',
        url: '/im'
        parent: 'home'
        views:
          'sidebar': templateUrl: 'app/shared/sidebar/sidebarView.html'
          'toolbar': templateUrl: 'app/shared/toolbar/toolbarView.html'
          'content': templateUrl: 'app/shared/messages/messagesView.html'
          'compose': templateUrl: 'app/shared/compose/composeView.html'

      .state 'login',
        url: '/login'
        data:
          noLogin: true
        views:
          '@': templateUrl: 'app/components/login/loginView.html'

    $urlRouterProvider
      .otherwise '/'

ActorConfig.$inject = ['$stateProvider', '$urlRouterProvider']

angular
  .module 'actorWeb'
  .config ActorConfig
