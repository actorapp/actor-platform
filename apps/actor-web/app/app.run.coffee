class ActorRun
  constructor: ($rootScope, $state, $stateParams) ->
    console.log '[AW]Run'
    $rootScope.$state = $state
    $rootScope.$stateParams = $stateParams

ActorRun.$inject = ['$rootScope', '$state', '$stateParams']

angular
  .module 'actorWeb'
  .run ActorRun
