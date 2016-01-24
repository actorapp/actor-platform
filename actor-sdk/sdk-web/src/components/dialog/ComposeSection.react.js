/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { assign, forEach } from 'lodash';
import React, { Component, PropTypes } from 'react';
import classnames from 'classnames';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';
import { Container } from 'flux/utils';

import ActorClient from '../../utils/ActorClient';
import Inputs from '../../utils/Inputs';

import { KeyCodes } from '../../constants/ActorAppConstants';

import MessageActionCreators from '../../actions/MessageActionCreators';
import ComposeActionCreators from '../../actions/ComposeActionCreators';
import AttachmentsActionCreators from '../../actions/AttachmentsActionCreators';
import EmojiActionCreators from '../../actions/EmojiActionCreators';

import GroupStore from '../../stores/GroupStore';
import PreferencesStore from '../../stores/PreferencesStore';
import ComposeStore from '../../stores/ComposeStore';
import AttachmentStore from '../../stores/AttachmentStore';
import DialogStore from '../../stores/DialogStore';

import AvatarItem from '../common/AvatarItem.react';
import MentionDropdown from '../common/MentionDropdown.react';
import EmojiDropdown from '../common/EmojiDropdown.react';
import VoiceRecorder from '../common/VoiceRecorder.react';
import DropZone from '../common/DropZone.react';
import SendAttachment from '../modals/SendAttachment';

class ComposeSection extends Component {
  static getStores = () => [DialogStore, GroupStore, PreferencesStore, AttachmentStore, ComposeStore];

  static calculateState(prevState) {
    return {
      peer: DialogStore.getCurrentPeer(),
      text: ComposeStore.getText(),
      profile: ActorClient.getUser(ActorClient.getUid()),
      sendByEnter: PreferencesStore.isSendByEnterEnabled(),
      mentions: ComposeStore.getMentions(),
      isSendAttachmentOpen: AttachmentStore.isOpen(),
      isMarkdownHintShow: prevState ? prevState.isMarkdownHintShow || false : false,
      isAutoFocusEnabled: ComposeStore.isAutoFocusEnabled()
    };
  }

  static contextTypes = {
    isExperimental: PropTypes.bool
  };

  constructor(props) {
    super(props);

    this.setListeners();
  }

  componentWillUnmount() {
    this.setBlur();
    this.clearListeners();
  }

  componentDidMount() {
    this.setFocus();
  }

  componentDidUpdate(prevProps, prevState) {
    const { isAutoFocusEnabled } = this.state;

    if (isAutoFocusEnabled) {
      if (prevState.isAutoFocusEnabled !== true) {
        this.setListeners();
      }
      this.setFocus();
    } else {
      if (prevState.isAutoFocusEnabled !== false) {
        this.clearListeners();
      }
    }
  }

  setListeners() {
    window.addEventListener('focus', this.setFocus);
    document.addEventListener('keydown', this.handleKeyDown, false);
  }

  clearListeners() {
    window.removeEventListener('focus', this.setFocus);
    document.removeEventListener('keydown', this.handleKeyDown, false);
  }

  handleKeyDown = (event) => {
    const { isAutoFocusEnabled } = this.state;
    if (isAutoFocusEnabled) {
      if (!event.metaKey && !event.altKey && !event.ctrlKey && !event.shiftKey) {
        this.setFocus();
      }
    }
  };

  onMessageChange = event => {
    const text = event.target.value;
    const { peer } = this.state;

    if (text.length >= 3) {
      this.setState({isMarkdownHintShow: true})
    } else {
      this.setState({isMarkdownHintShow: false})
    }

    ComposeActionCreators.onTyping(peer, text, this.getCaretPosition());
  };

  onKeyDown = event => {
    const { mentions, sendByEnter } = this.state;

    const send = () => {
      event.preventDefault();
      this.sendTextMessage();
      this.setState({isMarkdownHintShow: false})
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
    const { peer, text } = this.state;

    if (text.trim().length !== 0) {
      MessageActionCreators.sendTextMessage(peer, text);
    }
    ComposeActionCreators.cleanText();
  };

  resetAttachmentForm = () => {
    const form = React.findDOMNode(this.refs.attachmentForm);
    form.reset();
  };

  onPaste = event => {
    let preventDefault = false;
    let attachments = [];

    forEach(event.clipboardData.items, (item) => {
      if (item.type.indexOf('image') !== -1) {
        preventDefault = true;
        attachments.push(item.getAsFile());
      }
    }, this);

    if (attachments.length > 0) {
      AttachmentsActionCreators.show(attachments);
    }

    if (preventDefault) {
      event.preventDefault();
    }
  };

  onMentionSelect = (mention) => {
    const { peer, text } = this.state;

    ComposeActionCreators.insertMention(peer, text, this.getCaretPosition(), mention);
    this.setFocus();
  };

  onMentionClose = () => {
    ComposeActionCreators.closeMention();
  };

  getCaretPosition = () => {
    const composeArea = React.findDOMNode(this.refs.area);
    const selection = Inputs.getInputSelection(composeArea);
    return selection.start;
  };

  handleEmojiSelect = (emoji) => {
    EmojiActionCreators.insertEmoji(this.state.text, this.getCaretPosition(), emoji);
    this.setFocus();
  };

  setFocus = () => {
    React.findDOMNode(this.refs.area).focus();
  };

  setBlur = () => {
    React.findDOMNode(this.refs.area).blur();
  };

  handleDrop = (files) => {
    let attachments = [];

    forEach(files, (file) => attachments.push(file));

    if (attachments.length > 0) {
      AttachmentsActionCreators.show(attachments);
    }
  };

  handleAttachmentClick = () => {
    const attachmentInputNode = React.findDOMNode(this.refs.attachment);
    attachmentInputNode.setAttribute('multiple', true);
    attachmentInputNode.click();
  };

  handleComposeAttachmentChange = () => {
    const attachmentInputNode = React.findDOMNode(this.refs.attachment);
    let attachments = [];

    forEach(attachmentInputNode.files, (file) => attachments.push(file));

    AttachmentsActionCreators.show(attachments);
    this.resetAttachmentForm();
  };

  sendVoiceRecord = (duration, record) => {
    const { peer } = this.state;
    MessageActionCreators.sendVoiceMessage(peer, duration, record);
  };

  render() {
    const { text, profile, mentions, isMarkdownHintShow, isSendAttachmentOpen } = this.state;
    const { isExperimental } = this.context;
    const markdownHintClassName = classnames('compose__markdown-hint', {
      'compose__markdown-hint--active': isMarkdownHintShow
    });

    return (
      <section className="compose">
        <MentionDropdown mentions={mentions}
                         onSelect={this.onMentionSelect}
                         onClose={this.onMentionClose}/>

        <EmojiDropdown onSelect={this.handleEmojiSelect}/>

        {
          isExperimental
            ? <VoiceRecorder onFinish={this.sendVoiceRecord}/>
            : null
        }

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
                  onPaste={this.onPaste}
                  value={text}
                  ref="area"/>

        <DropZone onDropComplete={this.handleDrop}>{this.getIntlMessage('compose.dropzone')}</DropZone>

        <footer className="compose__footer row">
          <button className="button attachment" onClick={this.handleAttachmentClick}>
            <i className="material-icons">attachment</i> {this.getIntlMessage('compose.attach')}
          </button>
          <span className="col-xs"/>
          <button className="button button--lightblue" onClick={this.sendTextMessage}>
            {this.getIntlMessage('compose.send')}
          </button>
        </footer>

        <form className="compose__hidden" ref="attachmentForm">
          <input ref="attachment" onChange={this.handleComposeAttachmentChange} type="file"/>
        </form>

        {/* Attachment modal */}
        {isSendAttachmentOpen ? <SendAttachment/> : null}

      </section>
    );
  }
}

ReactMixin.onClass(ComposeSection, IntlMixin);

export default Container.create(ComposeSection, {pure: false});
