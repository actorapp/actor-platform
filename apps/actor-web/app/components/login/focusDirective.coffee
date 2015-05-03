class FocusMe
  constructor: () ->
    @restrict = 'A'

  link: (scope, element) ->
    console.log '[AW] focus on element:', element
    element[0].focus()

angular
  .module 'actorWeb'
  .directive 'focusMe', -> new FocusMe
