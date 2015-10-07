/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { assign, forEach } from 'lodash';

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
import DropZone from 'components/common/DropZone.react';

let getStateFromStores = () => {
  return {
    text: ComposeStore.getText(),
    profile: ActorClient.getUser(ActorClient.getUid()),
    sendByEnter: PreferencesStore.isSendByEnterEnabled(),
    mentions: ComposeStore.getMentions()
  };
};


export default
@ReactMixin.decorate(IntlMixin)
@ReactMixin.decorate(PureRenderMixin)
class ComposeSection extends React.Component {
  static propTypes = {
    peer: React.PropTypes.object.isRequired
  };

  constructor(props) {
    super(props);

    this.state = assign({
      isEmojiDropdownShow: false,
      isMardownHintShow: false
    }, getStateFromStores());

    GroupStore.addChangeListener(this.onChange);
    ComposeStore.addChangeListener(this.onChange);
    PreferencesStore.addListener(this.onChange);

    window.addEventListener('focus', this.onFocus);
  }

  componentWillUnmount() {
    GroupStore.removeChangeListener(this.onChange);
    ComposeStore.removeChangeListener(this.onChange);

    window.removeEventListener('focus', this.onFocus);
  }

  componentWillReceiveProps() {
    this.onFocus();
    this.setState({isMardownHintShow: false})
  }

  onChange = () => this.setState(getStateFromStores());

  onMessageChange = event => {
    const text = event.target.value;
    const { peer } = this.props;

    if (text.length >= 3) {
      this.setState({isMardownHintShow: true})
    } else {
      this.setState({isMardownHintShow: false})
    }

    ComposeActionCreators.onTyping(peer, text, this.getCaretPosition());
  };

  onKeyDown = event => {
    const { mentions, sendByEnter } = this.state;

    const send = () => {
      event.preventDefault();
      this.sendTextMessage();
      this.setState({isMardownHintShow: false})
    };

    if (mentions === null) {
      if (sendByEnter === true) {
        if (event.keyCode === KeyCodes.ENTER && !event.shiftKey) {
          send();
        }
      } else {
        if (event.keyCode === KeyCodes.ENTER && event.metaKey) {
          send();
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
    fileInput.setAttribute('multiple', true);
    fileInput.click();
  };

  onSendPhotoClick = () => {
    const photoInput = React.findDOMNode(this.refs.composePhotoInput);
    photoInput.setAttribute('multiple', true);
    photoInput.accept = 'image/jpeg,image/png';
    photoInput.click();
  };

  onFileInputChange = () => {
    const { peer } = this.props;
    const fileInput = React.findDOMNode(this.refs.composeFileInput);
    forEach(fileInput.files, (file) => MessageActionCreators.sendFileMessage(peer, file));
    this.resetSendFileForm();
  };

  onPhotoInputChange = () => {
    const { peer } = this.props;
    const photoInput = React.findDOMNode(this.refs.composePhotoInput);
    forEach(photoInput.files, (photo) => MessageActionCreators.sendPhotoMessage(peer, photo));
    this.resetSendFileForm();
  };

  resetSendFileForm = () => {
    const form = React.findDOMNode(this.refs.sendFileForm);
    form.reset();
  };

  onPaste = event => {
    let preventDefault = false;

    forEach(event.clipboardData.items, (item) => {
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

  onFocus = () => {
    const composeArea = React.findDOMNode(this.refs.area);
    composeArea.focus();
  };

  onDrop = (files) => {
    const { peer } = this.props;

    forEach(files, (file) => {
      if (file.type === 'image/jpeg') {
        MessageActionCreators.sendPhotoMessage(peer, file);
      } else {
        MessageActionCreators.sendFileMessage(peer, file);
      }
    });
  };

  render() {
    const { text, profile, mentions, isEmojiDropdownShow, isMardownHintShow } = this.state;

    const emojiOpenerClassName = classnames('emoji-opener material-icons', {
      'emoji-opener--active': isEmojiDropdownShow
    });
    const markdownHintClassName = classnames('compose__markdown-hint', {
      'compose__markdown-hint--active': isMardownHintShow
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

        <div className={markdownHintClassName}>
          <b>*{this.getIntlMessage('compose.markdown.bold')}*</b>
          &nbsp;&nbsp;
          <i>_{this.getIntlMessage('compose.markdown.italic')}_</i>
          &nbsp;&nbsp;
          <code>```{this.getIntlMessage('compose.markdown.preformatted')}```</code>
        </div>

        <AvatarItem className="my-avatar"
                    image={profile.avatar}
                    placeholder={profile.placeholder}
                    title={profile.name}/>

        <textarea className="compose__message"
                  onChange={this.onMessageChange}
                  onKeyDown={this.onKeyDown}
                  value={text}
                  ref="area"/>

        <DropZone onDropComplete={this.onDrop}>Drop your files here.</DropZone>

        <footer className="compose__footer row">
          <button className="button attachment" onClick={this.onSendFileClick}>
            <i className="material-icons">attachment</i> {this.getIntlMessage('compose.sendFile')}
          </button>
          <button className="button attachment" onClick={this.onSendPhotoClick}>
            <i className="material-icons">photo_camera</i> {this.getIntlMessage('compose.sendPhoto')}
          </button>
          <span className="col-xs"></span>
          <button className="button button--lightblue"
                  onClick={this.sendTextMessage}>{this.getIntlMessage('compose.send')}</button>
        </footer>

        <form className="compose__hidden" ref="sendFileForm">
          <input ref="composeFileInput" onChange={this.onFileInputChange} type="file"/>
          <input ref="composePhotoInput" onChange={this.onPhotoInputChange} type="file"/>
        </form>
      </section>
    );
  }
}
