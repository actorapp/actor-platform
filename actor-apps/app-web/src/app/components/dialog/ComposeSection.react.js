/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import _ from 'lodash';

import React from 'react';
import classnames from 'classnames';
import ReactMixin from 'react-mixin';
import addons from 'react/addons';
import { IntlMixin } from 'react-intl';

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
import EmojiDropdown from 'components/common/EmojiDropdown.react';

let getStateFromStores = () => {
  return {
    text: ComposeStore.getText(),
    profile: ActorClient.getUser(ActorClient.getUid()),
    sendByEnter: PreferencesStore.isSendByEnterEnabled(),
    mentions: ComposeStore.getMentions()
  };
};

@ReactMixin.decorate(IntlMixin)
@ReactMixin.decorate(PureRenderMixin)
class ComposeSection extends React.Component {
  static propTypes = {
    peer: React.PropTypes.object.isRequired
  };

  constructor(props) {
    super(props);

    this.state = _.assign({
      isEmojiDropdownShow: false
    }, getStateFromStores());

    GroupStore.addChangeListener(this.onChange);
    ComposeStore.addChangeListener(this.onChange);
    PreferencesStore.addListener(this.onChange);
  }

  componentWillUnmount() {
    GroupStore.removeChangeListener(this.onChange);
    ComposeStore.removeChangeListener(this.onChange);
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
    const fileInput = React.findDOMNode(this.refs.composeFileInput);
    fileInput.click();
  };

  onSendPhotoClick = () => {
    const photoInput = React.findDOMNode(this.refs.composePhotoInput);
    photoInput.accept = 'image/*';
    photoInput.click();
  };

  onFileInputChange = () => {
    const fileInput = React.findDOMNode(this.refs.composeFileInput);
    MessageActionCreators.sendFileMessage(this.props.peer, fileInput.files[0]);
    this.resetSendFileForm();
  };

  onPhotoInputChange = () => {
    console.debug('onPhotoInputChange');
    const photoInput = React.findDOMNode(this.refs.composePhotoInput);
    MessageActionCreators.sendPhotoMessage(this.props.peer, photoInput.files[0]);
    this.resetSendFileForm();
  };

  resetSendFileForm = () => {
    const form = React.findDOMNode(this.refs.sendFileForm);
    form.reset();
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
    React.findDOMNode(this.refs.area).focus();
  };

  onMentionClose = () => {
    ComposeActionCreators.closeMention();
  };

  getCaretPosition = () => {
    const composeArea = React.findDOMNode(this.refs.area);
    const selection = Inputs.getInputSelection(composeArea);
    return selection.start;
  };

  onEmojiDropdownSelect = (emoji) => {
    ComposeActionCreators.insertEmoji(this.state.text, this.getCaretPosition(), emoji);
    React.findDOMNode(this.refs.area).focus();
  };
  onEmojiDropdownClose = () => this.setState({isEmojiDropdownShow: false});
  onEmojiShowClick = () => this.setState({isEmojiDropdownShow: true});

  render() {
    const { text, profile, mentions, isEmojiDropdownShow } = this.state;

    const emojiOpenerClassName = classnames('emoji-opener material-icons', {
      'emoji-opener--active': isEmojiDropdownShow
    });

    return (
      <section className="compose" onPaste={this.onPaste}>

        <MentionDropdown mentions={mentions}
                         onSelect={this.onMentionSelect}
                         onClose={this.onMentionClose}/>

        <EmojiDropdown isOpen={isEmojiDropdownShow}
                       onSelect={this.onEmojiDropdownSelect}
                       onClose={this.onEmojiDropdownClose}/>
        <i className={emojiOpenerClassName}
           onClick={this.onEmojiShowClick}>insert_emoticon</i>

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
            <i className="material-icons">attachment</i> {this.getIntlMessage('composeSendFile')}
          </button>
          <button className="button attachment" onClick={this.onSendPhotoClick}>
            <i className="material-icons">photo_camera</i> {this.getIntlMessage('composeSendPhote')}
          </button>
          <span className="col-xs"></span>
          <button className="button button--lightblue"
                  onClick={this.sendTextMessage}>{this.getIntlMessage('composeSend')}</button>
        </footer>

        <form className="compose__hidden" ref="sendFileForm">
          <input ref="composeFileInput" onChange={this.onFileInputChange} type="file"/>
          <input ref="composePhotoInput" onChange={this.onPhotoInputChange} type="file"/>
        </form>
      </section>
    );
  }
}

export default ComposeSection;
