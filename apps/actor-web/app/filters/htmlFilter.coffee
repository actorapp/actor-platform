angular
  .module 'actorWeb'
  .filter 'html', ['$sce', ($sce) ->
    (text) -> $sce.trustAsHtml(text)
  ]


#class HtmlFilter
#  constructor: ($sce) ->
#    console.log '[AW]HtmlFilter constructor (render text as html)'
#    (text) ->
#      console.log '[AW]HtmlFilter text:', text
#      console.log '[AW]HtmlFilter html:', $sce.trustAsHtml(text)
#
#HtmlFilter.$inject = ['$sce']
#
#angular
#  .module 'actorWeb'
#  .filter 'html', -> HtmlFilter
