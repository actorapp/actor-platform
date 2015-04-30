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
    @draft = @actorService.loadDraft @actorService.currentPeer
    console.log '[AW]ComposeController enableCompose: @draft:', @draft
    @message = if @draft then @draft else null
    console.log '[AW]ComposeController enableCompose: @message:', @message

  onTyping: ->
    console.log '[AW]ComposeController onTyping'
    @actorService.onTyping()
    @actorService.saveDraft @actorService.currentPeer, @message

  sendMessage: ->
    console.log '[AW]ComposeController sendMessage'
    console.log '[AW]ComposeController sendMessage: @message:', @message
    console.log '[AW]ComposeController sendMessage: @draft:', @draft
    @actorService.sendMessage @actorService.currentPeer, @message
    @actorService.saveDraft peer, ''
    @message = @draft = null

ComposeController.$inject = ['$rootScope', 'actorService']

angular
  .module 'actorWeb'
  .controller 'composeController', ComposeController
