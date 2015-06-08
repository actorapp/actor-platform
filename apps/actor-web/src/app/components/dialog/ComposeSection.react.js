'use strict';

var _ = require('lodash');

var React = require('react');
var PureRenderMixin = require('react/addons').addons.PureRenderMixin;

var MessageActionCreators = require('../../actions/MessageActionCreators');
var TypingActionCreators = require('../../actions/TypingActionCreators');

var TestActionCreators = require('../../actions/TestActionCreators');

var ENTER_KEY_CODE = 13;

var ComposeSection = React.createClass({
  mixins: [PureRenderMixin],

  propTypes: {
    peer: React.PropTypes.object.isRequired
  },

  getInitialState: function() {
    return({
      text: ''
    });
  },

  render: function() {
    return (
      <section className="compose" onPaste={this._onPaste}>
        <textarea className="compose__message" value={this.state.text} onChange={this._onChange} onKeyDown={this._onKeyDown}></textarea>
        <footer className="compose__footer row">
          <button className="button" onClick={this._onSendFileClick}>
            <i className="material-icons">attachment</i> Send file
          </button>
          <button className="button" onClick={this._onSendPhotoClick}>
            <i className="material-icons">photo_camera</i> Send photo
          </button>
          <span className="col-xs"></span>

          <button className="button" onClick={this._changeName}>ChangeName</button>

          <button className="button button--primary">Send</button>
        </footer>

        <div className="compose__hidden">
          <input type="file"
                 id="composeFileInput"
                 onChange={this._onFileInputChange}/>
          <input type="file"
                 id="composePhotoInput"
                 onChange={this._onPhotoInputChange}/>
        </div>
      </section>
    )
  },

  _onChange: function(event) {
    TypingActionCreators.onTyping(this.props.peer);
    this.setState({text: event.target.value});
  },

  _onKeyDown: function(event) {
    if (event.keyCode === ENTER_KEY_CODE && !event.shiftKey) {
      event.preventDefault();
      var text = this.state.text;
      if (text) {
        MessageActionCreators.sendTextMessage(this.props.peer, text);
      }
      this.setState({text: ''});
    }
  },

  _onSendFileClick: function() {
    var fileInput = document.getElementById('composeFileInput');
    fileInput.click();
  },

  _onSendPhotoClick: function() {
    var photoInput = document.getElementById('composePhotoInput');
    photoInput.accept = "image/*";
    photoInput.click();
  },

  _onFileInputChange: function() {
    var files = document.getElementById('composeFileInput').files;
    MessageActionCreators.sendFileMessage(this.props.peer, files[0]);
  },

  _onPhotoInputChange: function() {
    var photos = document.getElementById('composePhotoInput').files;
    MessageActionCreators.sendPhotoMessage(this.props.peer, photos[0]);
  },

  _onPaste: function(event) {
    var preventDefault = false;

    _.forEach(event.clipboardData.items, function(item) {
      if (item.type.indexOf('image') != -1) {
        preventDefault = true;
        MessageActionCreators.sendClipboardPhotoMessage(this.props.peer, item.getAsFile());
      }
    }, this);

    if (preventDefault) {
      event.preventDefault();
    }
  },

  _changeName: function() {
    TestActionCreators.editMyName("Fooooo");
  }
});

module.exports = ComposeSection;
