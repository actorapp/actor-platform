/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { forEach } from 'lodash';
import React, { Component, PropTypes } from 'react';
import { findDOMNode } from 'react-dom';
import { Container } from 'flux/utils';

import ActorClient from '../../utils/ActorClient';
import { KeyCodes } from '../../constants/ActorAppConstants';

import MessageActionCreators from '../../actions/MessageActionCreators';
import ComposeActionCreators from '../../actions/ComposeActionCreators';
import AttachmentsActionCreators from '../../actions/AttachmentsActionCreators';
import EmojiActionCreators from '../../actions/EmojiActionCreators';
import StickersActionCreators from '../../actions/StickersActionCreators';

import GroupStore from '../../stores/GroupStore';
import PreferencesStore from '../../stores/PreferencesStore';
import ComposeStore from '../../stores/ComposeStore';
import DialogStore from '../../stores/DialogStore';
import MessageArtStore from '../../stores/MessageArtStore';

import ComposeTextArea from './compose/ComposeTextArea.react';
import ComposeMarkdownHint from './compose/ComposeMarkdownHint.react';
import BotCommandsHint from './compose/BotCommandsHint.react';
import AvatarItem from '../common/AvatarItem.react';
import MentionDropdown from '../common/MentionDropdown.react';
import MessageArt from '../messageArt/MessageArt.react';
import VoiceRecorder from '../common/VoiceRecorder.react';
import DropZone from '../common/DropZone.react';

class ComposeSection extends Component {
  static contextTypes = {
    intl: PropTypes.object
  };

  static getStores() {
    return [DialogStore, GroupStore, PreferencesStore, ComposeStore, MessageArtStore];
  }

  static calculateState() {
    return {
      peer: DialogStore.getCurrentPeer(),
      compose: ComposeStore.getState(),
      profile: ActorClient.getUser(ActorClient.getUid()),
      sendByEnter: PreferencesStore.isSendByEnterEnabled(),
      isMessageArtOpen: MessageArtStore.getState().isOpen,
      stickers: MessageArtStore.getState().stickers
    };
  }

  constructor(props) {
    super(props);

    this.onTyping = this.onTyping.bind(this);
    this.onSubmit = this.onSubmit.bind(this);
    this.onPaste = this.onPaste.bind(this);
    this.onKeyDown = this.onKeyDown.bind(this);
    this.onCommandSelect = this.onCommandSelect.bind(this);
    this.onCommandClose = this.onCommandClose.bind(this);
  }

  componentDidUpdate(prevProps, prevState) {
    if (prevState.peer !== this.state.peer) {
      if (this.refs.area) {
        this.refs.area.autoFocus();
      }
    }
  }

  onTyping(text, caretPosition) {
    ComposeActionCreators.onTyping(this.state.peer, text, caretPosition);
  }

  onSubmit() {
    const { peer, compose: { text } } = this.state;

    if (text.trim().length) {
      MessageActionCreators.sendTextMessage(peer, text);
    }

    ComposeActionCreators.cleanText();
  }

  onPaste(event) {
    const attachments = Array.from(event.clipboardData.items)
      .filter((item) => item.type.indexOf('image') !== -1)
      .map((item) => item.getAsFile());

    if (attachments.length) {
      event.preventDefault();
      AttachmentsActionCreators.show(attachments);
    }
  }

  onKeyDown(event) {
    if (event.keyCode === KeyCodes.ARROW_UP && !event.target.value) {
      MessageActionCreators.editLastMessage();
    }
  }

  resetAttachmentForm = () => {
    const form = findDOMNode(this.refs.attachmentForm);
    form.reset();
  };

  // TODO: move this to textarea component
  onMentionSelect = (mention) => {
    const { peer, compose: { text } } = this.state;

    ComposeActionCreators.insertMention(peer, text, this.getCaretPosition(), mention);
    this.setFocus();
  };

  onMentionClose = () => {
    ComposeActionCreators.closeMention();
  };

  // TODO: move this to textarea component
  handleEmojiSelect = (emoji) => {
    const { compose: { text } } = this.state;
    EmojiActionCreators.insertEmoji(text, this.getCaretPosition(), emoji);
    this.setFocus();
  };

  onCommandSelect(command) {
    const { peer } = this.state;
    MessageActionCreators.sendTextMessage(peer, `/${command}`);
    ComposeActionCreators.cleanText();
  }

  onCommandClose() {
    ComposeActionCreators.cleanText();
  }

  handleStickerSelect = (sticker) => {
    const { peer } = this.state;
    StickersActionCreators.sendSticker(peer, sticker);
    this.setFocus();
  };

  setFocus = () => {
    findDOMNode(this.refs.area).focus();
  };

  handleDrop = (files) => {
    let attachments = [];

    forEach(files, (file) => attachments.push(file));

    if (attachments.length > 0) {
      AttachmentsActionCreators.show(attachments);
    }
  };

  handleAttachmentClick = () => {
    const attachmentInputNode = findDOMNode(this.refs.attachment);
    attachmentInputNode.setAttribute('multiple', true);
    attachmentInputNode.click();
  };

  handleComposeAttachmentChange = () => {
    const attachmentInputNode = findDOMNode(this.refs.attachment);
    let attachments = [];

    forEach(attachmentInputNode.files, (file) => attachments.push(file));

    AttachmentsActionCreators.show(attachments);
    this.resetAttachmentForm();
  };

  sendVoiceRecord = (duration, record) => {
    const { peer } = this.state;
    MessageActionCreators.sendVoiceMessage(peer, duration, record);
  };

  // TODO: remove this method
  getCaretPosition() {
    if (this.refs.area) {
      return this.refs.area.getCaretPosition();
    }

    return 0;
  }

  renderCommands() {
    const { compose } = this.state;
    if (!compose.commands) {
      return null;
    }

    return (
      <BotCommandsHint
        commands={compose.commands}
        onSelect={this.onCommandSelect}
        onClose={this.onCommandClose}
      />
    );
  }

  renderMentions() {
    const { compose } = this.state;
    if (!compose.mentions) {
      return null;
    }

    return (
      <MentionDropdown
        mentions={compose.mentions}
        onSelect={this.onMentionSelect}
        onClose={this.onMentionClose}
      />
    );
  }

  render() {
    const { compose, profile, stickers, isMessageArtOpen, sendByEnter } = this.state;
    const { intl } = this.context;

    return (
      <section className="compose">

        {this.renderMentions()}
        {this.renderCommands()}

        <MessageArt
          onSelect={this.handleEmojiSelect}
          onStickerSelect={this.handleStickerSelect}
          isActive={isMessageArtOpen}
          stickers={stickers}
        />

        <VoiceRecorder onFinish={this.sendVoiceRecord}/>

        <AvatarItem
          className="my-avatar"
          image={profile.avatar}
          placeholder={profile.placeholder}
          title={profile.name}
        />

        <ComposeMarkdownHint isActive={compose.text.length >= 3} />
        <ComposeTextArea
          ref="area"
          value={compose.text}
          autoFocus={compose.autoFocus}
          sendByEnter={sendByEnter}
          sendEnabled={!compose.mentions && !compose.commands}
          onTyping={this.onTyping}
          onSubmit={this.onSubmit}
          onPaste={this.onPaste}
          onKeyDown={this.onKeyDown}
        />

        <DropZone onDropComplete={this.handleDrop}>
          {intl.messages['compose.dropzone']}
        </DropZone>

        <footer className="compose__footer row">
          <button className="button attachment" onClick={this.handleAttachmentClick}>
            <i className="material-icons">attachment</i> {intl.messages['compose.attach']}
          </button>
          <span className="col-xs"/>
          <button className="button button--lightblue" onClick={this.sendTextMessage}>
            {intl.messages['compose.send']}
          </button>
        </footer>

        <form className="compose__hidden" ref="attachmentForm">
          <input ref="attachment" onChange={this.handleComposeAttachmentChange} type="file"/>
        </form>
      </section>
    );
  }
}

export default Container.create(ComposeSection, { pure: false });
