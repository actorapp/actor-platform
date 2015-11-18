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

import ActorClient from '../../utils/ActorClient';
import Inputs from '../../utils/Inputs';

import { KeyCodes } from '../../constants/ActorAppConstants';

import MessageActionCreators from '../../actions/MessageActionCreators';
import ComposeActionCreators from '../../actions/ComposeActionCreators';
import AttachmentsActionCreators from '../../actions/AttachmentsActionCreators';

import GroupStore from '../../stores/GroupStore';
import PreferencesStore from '../../stores/PreferencesStore';
import ComposeStore from '../../stores/ComposeStore';
import AttachmentStore from '../../stores/AttachmentStore';

import AvatarItem from '../common/AvatarItem.react';
import MentionDropdown from '../common/MentionDropdown.react';
import EmojiDropdown from '../common/EmojiDropdown.react';
import DropZone from '../common/DropZone.react';
import SendAttachment from '../modals/SendAttachment';

let getStateFromStores = () => {
  return {
    text: ComposeStore.getText(),
    profile: ActorClient.getUser(ActorClient.getUid()),
    sendByEnter: PreferencesStore.isSendByEnterEnabled(),
    mentions: ComposeStore.getMentions(),
    isSendAttachmentOpen: AttachmentStore.isOpen()
  };
};

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

    ComposeStore.addChangeListener(this.onChange);
    GroupStore.addListener(this.onChange);
    PreferencesStore.addListener(this.onChange);
    AttachmentStore.addListener(this.onChange);

    window.addEventListener('focus', this.setFocus);
  }

  componentWillUnmount() {
    ComposeStore.removeChangeListener(this.onChange);

    window.removeEventListener('focus', this.setFocus);
  }

  componentWillReceiveProps() {
    this.setFocus();
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

  resetAttachmentForm = () => {
    const form = React.findDOMNode(this.refs.attachmentForm);
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

  setFocus = () => React.findDOMNode(this.refs.area).focus();

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

  render() {
    const { text, profile, mentions, isEmojiDropdownShow, isMardownHintShow, isSendAttachmentOpen } = this.state;

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
ReactMixin.onClass(ComposeSection, PureRenderMixin);

export default ComposeSection;
