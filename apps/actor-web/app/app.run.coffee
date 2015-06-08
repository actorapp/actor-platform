class ActorRun
  constructor: ($rootScope, $state, $stateParams) ->
    $rootScope.$state = $state
    $rootScope.$stateParams = $stateParams

ActorRun.$inject = ['$rootScope', '$state', '$stateParams']

angular
  .module 'actorWeb'
  .run ActorRun

