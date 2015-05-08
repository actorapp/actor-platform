class Nl2br
  constructor: ($sanitize) ->
    return (text) =>
      text = String(text).replace /(\r\n|\n\r|\r|\n|&#10;&#13;|&#13;&#10;|&#10;|&#13;)/g, '<br>' + '$1'
      return $sanitize text

Nl2br.$inject = ['$sanitize']

angular
  .module 'actorWeb'
  .filter 'nl2br', Nl2br
