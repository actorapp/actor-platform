/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import _ from 'lodash';

import React from 'react';
import ReactMixin from 'react-mixin';
import addons from 'react/addons';
const {addons: { PureRenderMixin }} = addons;

import ActorClient from 'utils/ActorClient';
import Inputs from 'utils/Inputs';

import { KeyCodes } from 'constants/ActorAppConstants';

import MessageActionCreators from 'actions/MessageActionCreators';
import ComposeActionCreators from 'actions/ComposeActionCreators';

import GroupStore from 'stores/GroupStore';
import PreferencesStore from 'stores/PreferencesStore';
import ComposeStore from 'stores/ComposeStore';

import AvatarItem from 'components/common/AvatarItem.react';
import MentionDropdown from 'components/common/MentionDropdown.react';

let getStateFromStores = () => {
  return {
    text: ComposeStore.getText(),
    profile: ActorClient.getUser(ActorClient.getUid()),
    sendByEnter: PreferencesStore.istSendByEnterEnabled(),
    mentions: ComposeStore.getMentions()
  };
};

@ReactMixin.decorate(PureRenderMixin) class ComposeSection extends React.Component {
  static propTypes = {
    peer: React.PropTypes.object.isRequired
  };

  constructor(props) {
    super(props);

    this.state = getStateFromStores();

    GroupStore.addChangeListener(this.onChange);
    ComposeStore.addChangeListener(this.onChange);
    PreferencesStore.addChangeListener(this.onChange);
  }

  componentWillUnmount() {
    GroupStore.removeChangeListener(this.onChange);
    ComposeStore.removeChangeListener(this.onChange);
    PreferencesStore.removeChangeListener(this.onChange);
  }

  onChange = () => {
    this.setState(getStateFromStores());
  };

  onMessageChange = event => {
    const text = event.target.value;
    const { peer } = this.props;

    ComposeActionCreators.onTyping(peer, text, this.getCaretPosition());
  };

  onKeyDown = event => {
    const { mentions, sendByEnter } = this.state;

    if (mentions === null) {
      if (sendByEnter === true) {
        if (event.keyCode === KeyCodes.ENTER && !event.shiftKey) {
          event.preventDefault();
          this.sendTextMessage();
        }
      } else {
        if (event.keyCode === KeyCodes.ENTER && event.metaKey) {
          event.preventDefault();
          this.sendTextMessage();
        }
      }
    }
  };

  sendTextMessage = () => {
    const { text } = this.state;
    const { peer } = this.props;

    if (text.trim().length !== 0) {
      MessageActionCreators.sendTextMessage(peer, text);
    }
    ComposeActionCreators.cleanText();
  };

  onSendFileClick = () => {
    const fileInput = document.getElementById('composeFileInput');
    fileInput.click();
  };

  onSendPhotoClick = () => {
    const photoInput = document.getElementById('composePhotoInput');
    photoInput.accept = 'image/*';
    photoInput.click();
  };

  onFileInputChange = () => {
    const files = document.getElementById('composeFileInput').files;
    MessageActionCreators.sendFileMessage(this.props.peer, files[0]);
  };

  onPhotoInputChange = () => {
    const photos = document.getElementById('composePhotoInput').files;
    MessageActionCreators.sendPhotoMessage(this.props.peer, photos[0]);
  };

  onPaste = event => {
    let preventDefault = false;

    _.forEach(event.clipboardData.items, (item) => {
      if (item.type.indexOf('image') !== -1) {
        preventDefault = true;
        MessageActionCreators.sendClipboardPhotoMessage(this.props.peer, item.getAsFile());
      }
    }, this);

    if (preventDefault) {
      event.preventDefault();
    }
  };

  onMentionSelect = (mention) => {
    const { peer } = this.props;
    const { text } = this.state;

    ComposeActionCreators.insertMention(peer, text, this.getCaretPosition(), mention);
    this.refs.area.getDOMNode().focus();
  };

  onMentionClose = () => {
    ComposeActionCreators.closeMention();
  };

  getCaretPosition = () => {
    const el = this.refs.area.getDOMNode();
    const selection = Inputs.getInputSelection(el);
    return selection.start;
  };

  render() {
    const { text, profile, mentions } = this.state;

    return (
      <section className="compose" onPaste={this.onPaste}>

        <MentionDropdown mentions={mentions}
                         onSelect={this.onMentionSelect}
                         onClose={this.onMentionClose}/>

        <AvatarItem className="my-avatar"
                    image={profile.avatar}
                    placeholder={profile.placeholder}
                    title={profile.name}/>

        <textarea className="compose__message"
                  onChange={this.onMessageChange}
                  onKeyDown={this.onKeyDown}
                  value={text}
                  ref="area"/>

        <footer className="compose__footer row">
          <button className="button attachment" onClick={this.onSendFileClick}>
            <i className="material-icons">attachment</i> Send file
          </button>
          <button className="button attachment" onClick={this.onSendPhotoClick}>
            <i className="material-icons">photo_camera</i> Send photo
          </button>

          <span className="col-xs"></span>

          <button className="button button--lightblue" onClick={this.sendTextMessage} >Send</button>

        </footer>

        <div className="compose__hidden">
          <input id="composeFileInput"
                 onChange={this.onFileInputChange}
                 type="file"/>
          <input id="composePhotoInput"
                 onChange={this.onPhotoInputChange}
                 type="file"/>
        </div>
      </section>
    );
  }
}

export default ComposeSection;
