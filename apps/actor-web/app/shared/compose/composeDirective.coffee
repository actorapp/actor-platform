class EnterSubmit
  constructor: () ->
    @restrict = 'A'

  link: (scope, elem, attrs) ->
    elem.bind 'keydown', (event) ->
      code = event.keyCode || event.which
      if code == 13
        if !event.shiftKey
          event.preventDefault()
          scope.$apply attrs.enterSubmit

angular
  .module 'actorWeb'
  .directive 'enterSubmit', -> new EnterSubmit
