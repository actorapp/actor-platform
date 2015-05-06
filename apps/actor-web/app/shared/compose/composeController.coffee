class ComposeController
  draft: ''
  message: ''
  isEnabled: false

  constructor: (@$rootScope, @$scope, @$timeout, @actorService) ->
    console.log '[AW]ComposeController constructor'
    @$scope.$on 'onConversationOpen', =>
      console.log '[AW]ComposeController constructor: onConversationOpen fired.'
      @enableCompose()

  enableCompose: ->
    console.log '[AW]ComposeController enableCompose'
    @isEnabled = true
    @draft = @actorService.loadDraft @actorService.currentPeer
    console.log '[AW]ComposeController enableCompose: @draft:', @draft
    @message = if @draft then @draft else ''
    console.log '[AW]ComposeController enableCompose: @message:', @message

  onTyping: ->
    console.log '[AW]ComposeController onTyping'
    @actorService.onTyping()
    @actorService.saveDraft @actorService.currentPeer, @message

  sendMessage: ->
    console.log '[AW]ComposeController sendMessage', @message
    @actorService.sendMessage @actorService.currentPeer, @message
    @message = @draft = ''
    @actorService.saveDraft @actorService.currentPeer, @draft

#  sendFileMessage: (file) ->
#    console.log '[AW]ComposeController sendFileMessage'
#    console.log file
#    @actorService.sendFile @actorService.currentPeer, file

ComposeController.$inject = ['$rootScope', '$scope', '$timeout', 'actorService']

angular
  .module 'actorWeb'
  .controller 'composeController', ComposeController
