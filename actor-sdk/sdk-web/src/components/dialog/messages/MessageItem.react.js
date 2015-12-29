/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { escape } from 'lodash';
import React, { Component, PropTypes } from 'react';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl'
import addons from 'react/addons';
import classnames from 'classnames';
import { escapeWithEmoji } from '../../../utils/EmojiUtils';
import PeerUtils from '../../../utils/PeerUtils';
import { MessageContentTypes } from '../../../constants/ActorAppConstants';

import VisibilitySensor from 'react-visibility-sensor';

import DialogActionCreators from '../../../actions/DialogActionCreators';
import MessageActionCreators from '../../../actions/MessageActionCreators';
import ActivityActionCreators from '../../../actions/ActivityActionCreators';
import ComposeActionCreators from '../../../actions/ComposeActionCreators';
import DropdownActionCreators from '../../../actions/DropdownActionCreators';

import UserStore from '../../../stores/UserStore';
import DropdownStore from '../../../stores/DropdownStore';

import AvatarItem from '../../common/AvatarItem.react';
import State from './State.react';
import Reactions from './Reactions.react';

// Default message content components
import DefaultService from './Service.react';
import DefaultText from './Text.react';
import DefaultPhoto from './Photo.react.js';
import DefaultDocument from './Document.react';
import DefaultVoice from './Voice.react';
import DefaultContact from './Contact.react';
import DefaultLocation from './Location.react.js';
import DefaultModern from './Modern.react.js';
import DefaultSticker from './Sticker.react.js';

const {addons: { PureRenderMixin }} = addons;

class MessageItem extends Component {
  static propTypes = {
    peer: PropTypes.object.isRequired,
    message: PropTypes.object.isRequired,
    isShortMessage: PropTypes.bool,
    isSelected: PropTypes.bool,
    onVisibilityChange: PropTypes.func,
    onSelect: PropTypes.func
  };

  static contextTypes = {
    delegate: PropTypes.object,
    isExperimental: PropTypes.bool
  };

  constructor(props) {
    super(props);

    this.state = {
      isHighlighted: DropdownStore.isOpen(props.message.rid)
    };

    DropdownStore.addListener(this.onMessagesChange);
  }

  onMessagesChange = () => {
    const { message } = this.props;
    this.setState({isHighlighted: DropdownStore.isOpen(message.rid)});
  };

  onClick = () => {
    const { message, peer } = this.props;

    if (PeerUtils.equals(peer, message.sender.peer)) {
      ActivityActionCreators.show();
    } else {
      DialogActionCreators.selectDialogPeerUser(message.sender.peer.id);
    }
  };

  onVisibilityChange = (isVisible) => {
    const { message, onVisibilityChange } = this.props;
    onVisibilityChange(message, isVisible);
  };

  showActions = (event) => {
    const { message } = this.props;
    DropdownActionCreators.openMessageActions(event.target.getBoundingClientRect(), message);
  };

  toggleMessageSelection = () => {
    const { message, onSelect } = this.props;
    onSelect && onSelect(message.rid);
  };

  render() {
    const { message, isShortMessage, onVisibilityChange, peer, isSelected } = this.props;
    const { isHighlighted } = this.state;
    const { delegate, isExperimental } = this.context;

    let Service, Text, Modern, Photo, Document, Voice, Contact, Location, Sticker;
    if (delegate.components.dialog !== null && delegate.components.dialog.messages) {
      Service = delegate.components.dialog.messages.service || DefaultService;
      Text = delegate.components.dialog.messages.text || DefaultText;
      Modern = delegate.components.dialog.messages.modern || DefaultModern;
      Photo = delegate.components.dialog.messages.photo || DefaultPhoto;
      Document = delegate.components.dialog.messages.document || DefaultDocument;
      Voice = delegate.components.dialog.messages.voice || DefaultVoice;
      Contact = delegate.components.dialog.messages.contact || DefaultContact;
      Location = delegate.components.dialog.messages.location || DefaultLocation;
      Sticker = delegate.components.dialog.messages.sticker || DefaultSticker;
    } else {
      Service = DefaultService;
      Text = DefaultText;
      Modern = DefaultModern;
      Photo = DefaultPhoto;
      Document = DefaultDocument;
      Voice = DefaultVoice;
      Contact = DefaultContact;
      Location = DefaultLocation;
      Sticker = DefaultSticker;
    }

    let header = null,
        messageContent = null,
        leftBlock = null;

    const messageSender = escapeWithEmoji(message.sender.title);

    const messageClassName = classnames('message row', {
      'message--same-sender': isShortMessage,
      'message--active': isHighlighted,
      'message--selected': isSelected
    });
    const messageActionsMenuClassName = classnames('message__actions__menu', {
      'message__actions__menu--opened': isHighlighted
    });

    if (isShortMessage) {
      leftBlock = (
        <div className="message__info text-right">
          <time className="message__timestamp">{message.date}</time>
          <State message={message}/>
        </div>
      );
    } else {
      leftBlock = (
        <div className="message__info message__info--avatar">
          <a onClick={this.onClick}>
            <AvatarItem image={message.sender.avatar}
                        placeholder={message.sender.placeholder}
                        title={message.sender.title}/>
          </a>
        </div>
      );
      header = (
        <header className="message__header">
          <h3 className="message__sender">
            <a onClick={this.onClick} dangerouslySetInnerHTML={{__html: messageSender}}/>
          </h3>
          <time className="message__timestamp">{message.date}</time>
          <State message={message}/>
        </header>
      );
    }

    switch (message.content.content) {
      case MessageContentTypes.SERVICE:
        messageContent = <Service {...message.content} className="message__content message__content--service"/>;
        break;
      case MessageContentTypes.TEXT:
        messageContent = <Text {...message.content} className="message__content message__content--text"/>;
        break;
      case MessageContentTypes.PHOTO:
        messageContent = <Photo content={message.content} className="message__content message__content--photo"
                                loadedClassName="message__content--photo--loaded"/>;
        break;
      case MessageContentTypes.DOCUMENT:
        messageContent = <Document content={message.content} className="message__content message__content--document"/>;
        break;
      case MessageContentTypes.VOICE:
        messageContent = <Voice content={message.content} className="message__content message__content--voice"/>;
        break;
      case MessageContentTypes.CONTACT:
        messageContent = <Contact {...message.content} className="message__content message__content--contact"/>;
        break;
      case MessageContentTypes.LOCATION:
        messageContent = <Location content={message.content} className="message__content message__content--location"/>;
        break;
      case MessageContentTypes.TEXT_MODERN:
        messageContent = <Modern {...message.content} className="message__content message__content--modern"/>;
        break;
      case MessageContentTypes.STICKER:
        messageContent = <Sticker {...message.content} className="message__content message__content--sticker"/>;
        break;
      default:
    }

    return (
      <li className={messageClassName}>
        {leftBlock}
        <div className="message__body col-xs">
          {header}
          {messageContent}
          {onVisibilityChange ? <VisibilitySensor onChange={this.onVisibilityChange}/> : null}
        </div>
        <div className="message__actions">
          <Reactions peer={peer} message={message}/>

          <div className={messageActionsMenuClassName} onClick={this.showActions}>
            <svg className="icon icon--dropdown"
                 dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/images/icons.svg#cog"/>'}}/>
          </div>

          {
            isExperimental
              ? <div className="message__actions__selector" onClick={this.toggleMessageSelection}>
                  <i className="icon material-icons">check</i>
                </div>
              : null
          }

        </div>
      </li>
    );
  }
}

ReactMixin.onClass(MessageItem, IntlMixin);
ReactMixin.onClass(MessageItem, PureRenderMixin);

export default MessageItem;
