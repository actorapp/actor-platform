class ComposeController
  draft: null
  message: null
  isEnabled: false

  constructor: (@$rootScope, @actorService) ->
    console.log '[AW]ComposeController constructor'
    @$rootScope.$on 'openConversation', =>
      console.log '[AW]ComposeController constructor: openConversation fired.'
      @enableCompose()

  enableCompose: ->
    console.log '[AW]ComposeController enableCompose'
    @isEnabled = true
    # @message = @draft if @draft
    # console.log '[AW]ComposeController enableCompose: @message:', @message
    # console.log '[AW]ComposeController enableCompose: @draft:', @draft

  onTyping: ->
    console.log '[AW]ComposeController onTyping'
    @actorService.onTyping()

  sendMessage: ->
    console.log '[AW]ComposeController sendMessage'
    console.log '[AW]ComposeController sendMessage: @message:', @message
    console.log '[AW]ComposeController sendMessage: @draft:', @draft
    @actorService.sendMessage @actorService.currentPeer, @message
    @message = null

ComposeController.$inject = ['$rootScope', 'actorService']

angular
  .module 'actorWeb'
  .controller 'composeController', ComposeController
