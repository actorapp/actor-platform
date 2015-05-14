class ComposeController
  draft: ''
  message: ''
  isEnabled: false

  constructor: (@$rootScope, @$scope, @$timeout, @actorService) ->
    console.log '[AW]ComposeController: constructor'
    @$scope.$watch 'main.actorService.currentPeer', (newValue, oldValue) =>
      if newValue != oldValue
        @$timeout =>
          @message = @draft = @actorService.loadDraft newValue
          @isEnabled = true

  onTyping: ->
    console.log '[AW]ComposeController: onTyping'
    @actorService.onTyping()
    @actorService.saveDraft @actorService.currentPeer, @message

  sendMessage: ->
    console.log '[AW]ComposeController: sendMessage'
    @actorService.sendMessage @actorService.currentPeer, @message
    @message = @draft = ''
    @actorService.saveDraft @actorService.currentPeer, @draft

  openFileDialog: ->
    console.log '[AW]ComposeController: openFileDialog'
    document.getElementById('file').click()

  openPhotoDialog: ->
    console.log '[AW]ComposeController: openFileDialog'
    fileInput = document.getElementById('photo')
    fileInput.accept = "image/*";
    fileInput.click()

  fileSelected: ->
    files = document.getElementById('file').files
    console.log '[AW]ComposeController: fileSelected: files'
    @sendFile files[0]

  photoSelected: ->
    files = document.getElementById('photo').files
    console.log '[AW]ComposeController: photoSelected: files'
    @sendPhoto files[0]

  sendFile: (file) ->
    console.log '[AW]ComposeController: sendFile'
    @actorService.sendFile @actorService.currentPeer, file

  sendPhoto: (file) ->
    console.log '[AW]ComposeController: sendPhoto'
    @actorService.sendPhoto @actorService.currentPeer, file


ComposeController.$inject = ['$rootScope', '$scope', '$timeout', 'actorService']

angular
  .module 'actorWeb'
  .controller 'composeController', ComposeController
