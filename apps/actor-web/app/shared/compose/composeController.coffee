class ComposeController
  draft: ''
  message: ''
  isEnabled: false

  constructor: (@$rootScope, @$scope, @$timeout, @actorService) ->
    console.log '[AW]ComposeController constructor'
    @$scope.$on 'onConversationOpen', =>
      console.log '[AW]ComposeController constructor: onConversationOpen fired.'
      @$timeout =>
        @message = @draft = @actorService.loadDraft @actorService.currentPeer
        console.log '[AW]ComposeController constructor' #: @draft:', @draft
        @isEnabled = true

  onTyping: ->
    console.log '[AW]ComposeController onTyping'
    @actorService.onTyping()
    @actorService.saveDraft @actorService.currentPeer, @message

  sendMessage: ->
    console.log '[AW]ComposeController sendMessage' #, @message
    @actorService.sendMessage @actorService.currentPeer, @message
    @message = @draft = ''
    @actorService.saveDraft @actorService.currentPeer, @draft

  openFileDialog: ->
    console.log '[AW]ComposeController openFileDialog'
    document.getElementById('file').click()

  openPhotoDialog: ->
    console.log '[AW]ComposeController openFileDialog'
    fileInput = document.getElementById('photo')
    fileInput.accept = "image/*";
    fileInput.click()

  fileSelected: ->
    files = document.getElementById('file').files
    console.log '[AW]ComposeController fileSelected: files' #, files
    @sendFile files[0]

  photoSelected: ->
    files = document.getElementById('photo').files
    console.log '[AW]ComposeController photoSelected: files' #, files
    @sendPhoto files[0]

  sendFile: (file) ->
    console.log '[AW]ComposeController sendFile' #, file
    @actorService.sendFile @actorService.currentPeer, file

  sendPhoto: (file) ->
    console.log '[AW]ComposeController sendPhoto' #, file
    @actorService.sendPhoto @actorService.currentPeer, file


ComposeController.$inject = ['$rootScope', '$scope', '$timeout', 'actorService']

angular
  .module 'actorWeb'
  .controller 'composeController', ComposeController
