class FocusMe
  constructor: () ->
    @restrict = 'A'

  link: (scope, element) ->
    element[0].focus()

angular
  .module 'actorWeb'
  .directive 'focusMe', -> new FocusMe
