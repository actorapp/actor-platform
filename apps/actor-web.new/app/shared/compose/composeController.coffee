class ComposeController
  draft: null
  message: null
  isEnabled: false

  constructor: (@$rootScope, @actorService) ->
    console.log '[AW]ComposeController constructor'

    @$rootScope.$on 'actorReady', =>
      console.log '[AW]ComposeController constructor: actorReady fired.'

      @enableCompose()


  enableCompose: ->
    console.log '[AW]ComposeController enableCompose'

    @message = @draft if @draft
    console.log '[AW]ComposeController enableCompose: @message:', @message
    console.log '[AW]ComposeController enableCompose: @draft:', @draft

    @isEnabled = true
    console.log '[AW]ComposeController enableCompose: @isEnabled:', @isEnabled


  # selectChat: (chat) ->
  #   console.log '[AW]ComposeController selectChat'
  #   console.log '[AW]ComposeController selectChat: @selectedChat:', @selectedChat

ComposeController.$inject = ['$rootScope', 'actorService']

angular
  .module 'actorWeb'
  .controller 'composeController', ComposeController
