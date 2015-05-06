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
    @message = @draft if @draft

  onTyping: ->
    console.log '[AW]ComposeController onTyping'
    @actorService.onTyping()
    @actorService.saveDraft @actorService.currentPeer, @message

  sendMessage: ->
    console.log '[AW]ComposeController sendMessage', @message
    @actorService.sendMessage @actorService.currentPeer, @message
    @message = @draft = ''
    @actorService.saveDraft @actorService.currentPeer, @draft

  openFileDialog: ->
    console.log '[AW]ComposeController openFileDialog'
    document.getElementById('file').click()

  fileSelected: ->
    console.log '[AW]ComposeController fileSelected'
    files = document.getElementById('file').files
    console.log '[AW]ComposeController fileSelected: files', files
    @sendFile files[0]

  sendFile: (file) ->
    console.log '[AW]ComposeController sendFileMessage', file
    @actorService.sendFile @actorService.currentPeer, file

ComposeController.$inject = ['$rootScope', '$scope', '$timeout', 'actorService']

angular
  .module 'actorWeb'
  .controller 'composeController', ComposeController
