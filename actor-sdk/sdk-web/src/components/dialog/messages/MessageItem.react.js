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

import UserStore from '../../../stores/UserStore';

import AvatarItem from '../../common/AvatarItem.react';
import Text from './Text.react';
import Image from './Image.react';
import Document from './Document.react';
import Voice from './Voice.react';
import Contact from './Contact.react';
import Geolocation from './Geolocation.react';
import State from './State.react';
import Reactions from './Reactions.react';

const {addons: { PureRenderMixin }} = addons;

class MessageItem extends Component {
  static propTypes = {
    peer: PropTypes.object.isRequired,
    message: PropTypes.object.isRequired,
    isNewDay: PropTypes.bool,
    isSameSender: PropTypes.bool,
    onVisibilityChange: PropTypes.func
  };

  constructor(props) {
    super(props);

    this.state = {
      isThisMyMessage: UserStore.getMyId() === props.message.sender.peer.id,
      isActionsShown: false
    };
  }

  onClick = () => {
    const { message, peer } = this.props;

    if (PeerUtils.equals(peer, message.sender.peer)) {
      ActivityActionCreators.show();
    } else {
      DialogActionCreators.selectDialogPeerUser(message.sender.peer.id);
    }
  };

  onVisibilityChange = (isVisible) => {
    const { message } = this.props;
    this.props.onVisibilityChange(message, isVisible);
  };

  handleDelete = () => {
    const { peer, message } = this.props;
    MessageActionCreators.deleteMessage(peer, message.rid);
  };

  handleReply = () => {
    const { message } = this.props;
    const info = UserStore.getUser(message.sender.peer.id);
    const replyText = info.nick ? `@${info.nick}: ` : `${info.name}: `;
    ComposeActionCreators.pasteText(replyText);
  };

  handleQuote = () => {
    const { message } = this.props;
    ComposeActionCreators.pasteText(`> ${message.content.text} \n`);
  };

  showActions = () => {
    this.setState({isActionsShown: true});
    document.addEventListener('click', this.hideActions, false);
  };

  hideActions = () => {
    this.setState({isActionsShown: false});
    document.removeEventListener('click', this.hideActions, false);
  };

  render() {
    const { message, isSameSender, onVisibilityChange, peer } = this.props;
    const { isThisMyMessage, isActionsShown } = this.state;

    let header = null,
        messageContent = null,
        leftBlock = null;

    const messageSender = escapeWithEmoji(message.sender.title);

    const messageClassName = classnames('message row', {
      'message--same-sender': isSameSender
    });

    const actionsDropdownClassName = classnames('message__actions__menu dropdown dropdown--small', {
      'dropdown--opened': isActionsShown
    });

    if (isSameSender) {
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
        messageContent = (
          <div className="message__content message__content--service"
               dangerouslySetInnerHTML={{__html: escapeWithEmoji(message.content.text)}}/>
        );
        break;
      case MessageContentTypes.TEXT:
        messageContent = (
          <Text content={message.content}
                className="message__content message__content--text"/>
        );
        break;
      case MessageContentTypes.PHOTO:
        messageContent = (
          <Image content={message.content}
                 className="message__content message__content--photo"
                 loadedClassName="message__content--photo--loaded"/>
        );
        break;
      case MessageContentTypes.DOCUMENT:
        messageContent = (
          <Document content={message.content}
                    className="message__content message__content--document"/>
        );
        break;
      case MessageContentTypes.VOICE:
        messageContent = (
          <Voice content={message.content}
                    className="message__content message__content--voice"/>
        );
        break;
      case MessageContentTypes.CONTACT:
        messageContent = (
          <Contact content={message.content}
                    className="message__content message__content--contact"/>
        );
        break;
      case MessageContentTypes.LOCATION:
        messageContent = (
          <Geolocation content={message.content}
                       className="message__content message__content--location"/>
        );
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

          <div className={actionsDropdownClassName}>
            <span className="dropdown__button" onClick={this.showActions}>
              <svg className="icon icon--dropdown"
                   dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/images/icons.svg#cog"/>'}}/>
            </span>
            <ul className="dropdown__menu dropdown__menu--right">
              <li className="dropdown__menu__item hide">
                <i className="icon material-icons">star_rate</i> {this.getIntlMessage('message.pin')}
              </li>
              {
                !isThisMyMessage
                  ? <li className="dropdown__menu__item" onClick={this.handleReply}>
                      <i className="icon material-icons">reply</i> {this.getIntlMessage('message.reply')}
                    </li>
                  : null
              }
              {
                message.content.content === MessageContentTypes.TEXT
                  ? <li className="dropdown__menu__item" onClick={this.handleQuote}>
                      <i className="icon material-icons">format_quote</i> {this.getIntlMessage('message.quote')}
                    </li>
                  : null
              }
              <li className="dropdown__menu__item hide">
                <i className="icon material-icons">forward</i> {this.getIntlMessage('message.forward')}
              </li>
              <li className="dropdown__menu__item" onClick={this.handleDelete}>
                <i className="icon material-icons">delete</i> {this.getIntlMessage('message.delete')}
              </li>
            </ul>
          </div>
        </div>
      </li>
    );
  }
}

ReactMixin.onClass(MessageItem, IntlMixin);
ReactMixin.onClass(MessageItem, PureRenderMixin);

export default MessageItem;
