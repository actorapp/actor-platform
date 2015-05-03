class ComposeController
  draft: ''
  message: ''
  isEnabled: false

  constructor: (@$rootScope, @$scope, @actorService) ->
    console.log '[AW]ComposeController constructor'
    @$scope.$on 'onConversationOpen', =>
      console.log '[AW]ComposeController constructor: onConversationOpen fired.'
      @enableCompose()

  enableCompose: ->
    console.log '[AW]ComposeController enableCompose'
    @isEnabled = true
    console.log '[AW]ComposeController enableCompose @actorService.currentPeer', @actorService.currentPeer
    @draft = @actorService.loadDraft @actorService.currentPeer
    console.log '[AW]ComposeController enableCompose: @draft:', @draft
    @message = if @draft then @draft else ''
    console.log '[AW]ComposeController enableCompose: @message:', @message

  onTyping: ->
    console.log '[AW]ComposeController onTyping'
    @actorService.onTyping()
    @actorService.saveDraft @actorService.currentPeer, @message

  sendMessage: ->
    console.log '[AW]ComposeController sendMessage'
    # console.log '[AW]ComposeController sendMessage: @message:', @message
    # console.log '[AW]ComposeController sendMessage: @draft:', @draft
    @actorService.sendMessage @actorService.currentPeer, @message
    @message = @draft = ''
    @actorService.saveDraft @actorService.currentPeer, @draft

  sendFileMessage: (@file) ->
    console.log @file
    @actorService.sendFile @actorService.currentPeer, @file

ComposeController.$inject = ['$rootScope', '$scope', 'actorService']

angular
  .module 'actorWeb'
  .controller 'composeController', ComposeController
