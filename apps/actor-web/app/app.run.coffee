class ActorRun
  constructor: ($rootScope, $state, $stateParams, actorService) ->
    console.log '[AW]Run'
    $rootScope.$state = $state
    $rootScope.$stateParams = $stateParams

    # $rootScope.isLoggedIn = null
    # $rootScope.$on '$stateChangeStart', (event, toState, toParams, fromState, fromParams) ->
      # actorService.checkAccess(event, toState, toParams, fromState, fromParams)

ActorRun.$inject = ['$rootScope', '$state', '$stateParams', 'actorService']

angular
  .module 'actorWeb'
  .run ActorRun
