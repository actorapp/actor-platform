import _ from 'lodash';

import React from 'react';
import { PureRenderMixin } from 'react/addons';

import MessageActionCreators from '../../actions/MessageActionCreators';
import TypingActionCreators from '../../actions/TypingActionCreators';

//import DraftActionCreators from '../../actions/DraftActionCreators';
//import DraftStore from '../../stores/DraftStore';
import { KeyCodes } from '../../constants/ActorAppConstants';

const getStateFromStores = () => {
  return {
    //text: DraftStore.getDraft()
    text: ''
  };
};

var ComposeSection = React.createClass({
  displayName: 'ComposeSection',

  propTypes: {
    peer: React.PropTypes.object.isRequired
  },

  mixins: [PureRenderMixin],

  componentWillMount() {
    //DraftStore.addLoadDraftListener(this.onDraftLoad);
  },

  componentWillUnmount() {
    //DraftStore.removeLoadDraftListener(this.onDraftLoad);
  },

  getInitialState: function() {
    return getStateFromStores();
  },

  onDraftLoad() {
    this.setState(getStateFromStores());
  },

  _onChange: function(event) {
    TypingActionCreators.onTyping(this.props.peer);
    this.setState({text: event.target.value});
  },

  _onKeyDown: function(event) {
    if (event.keyCode === KeyCodes.ENTER && !event.shiftKey) {
      event.preventDefault();
      this._sendTextMessage();
    }
  },

  onKeyUp() {
    //DraftActionCreators.saveDraft(this.state.text);
  },

  _sendTextMessage() {
    const text = this.state.text;
    if (text) {
      MessageActionCreators.sendTextMessage(this.props.peer, text);
    }
    this.setState({text: ''});
    //DraftActionCreators.saveDraft('', true);
  },

  _onSendFileClick: function() {
    const fileInput = document.getElementById('composeFileInput');
    fileInput.click();
  },

  _onSendPhotoClick: function() {
    const photoInput = document.getElementById('composePhotoInput');
    photoInput.accept = 'image/*';
    photoInput.click();
  },

  _onFileInputChange: function() {
    const files = document.getElementById('composeFileInput').files;
    MessageActionCreators.sendFileMessage(this.props.peer, files[0]);
  },

  _onPhotoInputChange: function() {
    const photos = document.getElementById('composePhotoInput').files;
    MessageActionCreators.sendPhotoMessage(this.props.peer, photos[0]);
  },

  _onPaste: function(event) {
    let preventDefault = false;

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
    const text = this.state.text;

    return (
      <section className="compose" onPaste={this._onPaste}>
        <textarea className="compose__message"
                  onChange={this._onChange}
                  onKeyDown={this._onKeyDown}
                  onKeyUp={this.onKeyUp}
                  value={text}>
        </textarea>

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
