angular
  .module 'actorWeb'
  .directive 'messages', ['reactDirective', (reactDirective) -> reactDirective('Messages')]