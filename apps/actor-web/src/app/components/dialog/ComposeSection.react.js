import _ from 'lodash';

import React from 'react';
import { PureRenderMixin } from 'react/addons';

import MessageActionCreators from '../../actions/MessageActionCreators';
import TypingActionCreators from '../../actions/TypingActionCreators';

import DraftActions from '../../actions/DraftActions';
import DraftStore from '../../stores/DraftStore';

const ENTER_KEY_CODE = 13;

var ComposeSection = React.createClass({
  propTypes: {
    peer: React.PropTypes.object.isRequired
  },

  mixins: [PureRenderMixin],

  getInitialState: function() {
    return {
      text: '',
      draft: DraftStore.getDraft()
    };
  },

  componentWillMount() {
    this.unsubscribe = DraftStore.listen(this.onChangeDraft);
  },

  componentWillUnmount() {
    this.unsubscribe();
  },

  onChangeDraft() {
    this.setState({text: DraftStore.getDraft()});
  },

  _onChange: function(event) {
    TypingActionCreators.onTyping(this.props.peer);
    this.setState({text: event.target.value});
  },

  _onKeyDown: function(event) {
    DraftActions.saveDraft(this.props.peer, this.state.text);
    if (event.keyCode === ENTER_KEY_CODE && !event.shiftKey) {
      event.preventDefault();
      this._sendTextMessage();
    }
  },

  _sendTextMessage() {
    let text = this.state.text;
    if (text) {
      MessageActionCreators.sendTextMessage(this.props.peer, text);
    }
    this.setState({text: ''});
    DraftActions.saveDraft(this.props.peer, '');
  },

  _onSendFileClick: function() {
    var fileInput = document.getElementById('composeFileInput');
    fileInput.click();
  },

  _onSendPhotoClick: function() {
    var photoInput = document.getElementById('composePhotoInput');
    photoInput.accept = 'image/*';
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
      if (item.type.indexOf('image') !== -1) {
        preventDefault = true;
        MessageActionCreators.sendClipboardPhotoMessage(this.props.peer, item.getAsFile());
      }
    }, this);

    if (preventDefault) {
      event.preventDefault();
    }
  },

  render: function() {
    let text = this.state.text;

    return (
      <section className="compose" onPaste={this._onPaste}>
        <textarea className="compose__message" onChange={this._onChange} onKeyDown={this._onKeyDown} value={text}></textarea>
        <footer className="compose__footer row">
          <button className="button" onClick={this._onSendFileClick}>
            <i className="material-icons">attachment</i> Send file
          </button>
          <button className="button" onClick={this._onSendPhotoClick}>
            <i className="material-icons">photo_camera</i> Send photo
          </button>

          <span className="col-xs"></span>

          <button className="button button--primary" onClick={this._sendTextMessage}>Send</button>
        </footer>

        <div className="compose__hidden">
          <input id="composeFileInput"
                 onChange={this._onFileInputChange}
                 type="file"/>
          <input id="composePhotoInput"
                 onChange={this._onPhotoInputChange}
                 type="file"/>
        </div>
      </section>
    );
  }

});

export default ComposeSection;
