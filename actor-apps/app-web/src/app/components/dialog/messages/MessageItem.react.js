/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { escape } from 'lodash';
import React from 'react';
import ReactMixin from 'react-mixin';
import addons from 'react/addons';
import classnames from 'classnames';
import { escapeWithEmoji } from 'utils/EmojiUtils';

import VisibilitySensor from 'react-visibility-sensor';

import DialogActionCreators from 'actions/DialogActionCreators';
import { MessageContentTypes } from 'constants/ActorAppConstants';

import AvatarItem from 'components/common/AvatarItem.react';
import Text from './Text.react';
import Image from './Image.react';
import Document from './Document.react';
import State from './State.react';

const {addons: { PureRenderMixin }} = addons;

@ReactMixin.decorate(PureRenderMixin)
class MessageItem extends React.Component {
  static propTypes = {
    peer: React.PropTypes.object.isRequired,
    message: React.PropTypes.object.isRequired,
    isNewDay: React.PropTypes.bool,
    isSameSender: React.PropTypes.bool,
    onVisibilityChange: React.PropTypes.func
  };

  constructor(props) {
    super(props);

    //this.state = {
    //  isActionsShown: false
    //};
  }

  onClick = () => {
    const { message } = this.props;
    DialogActionCreators.selectDialogPeerUser(message.sender.peer.id);
  };

  onVisibilityChange = (isVisible) => {
    const { message } = this.props;
    this.props.onVisibilityChange(message, isVisible);
  };

  onDelete = () => {
    const { peer, message } = this.props;
    DialogActionCreators.deleteMessages(peer, [message.rid]);
  };

  //showActions = () => {
  //  this.setState({isActionsShown: true});
  //  document.addEventListener('click', this.hideActions, false);
  //};

  //hideActions = () => {
  //  this.setState({isActionsShown: false});
  //  document.removeEventListener('click', this.hideActions, false);
  //};

  render() {
    const { message, isSameSender, onVisibilityChange } = this.props;

    let header = null,
        messageContent = null,
        visibilitySensor = null,
        leftBlock = null;

    const messageSender = escapeWithEmoji(message.sender.title);

    const messageClassName = classnames('message row', {
      'message--same-sender': isSameSender
    });

    //let actionsDropdownClassName = classnames({
    //  'dropdown': true,
    //  'dropdown--small': true,
    //  'dropdown--opened': this.state.isActionsShown
    //});

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
        messageContent = <div className="message__content message__content--service" dangerouslySetInnerHTML={{__html: escapeWithEmoji(message.content.text)}}/>;
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
      default:
        return null;
    }

    if (onVisibilityChange) {
      visibilitySensor = <VisibilitySensor onChange={this.onVisibilityChange}/>;
    }

    return (
      <li className={messageClassName}>
        {leftBlock}
        <div className="message__body col-xs">
          {header}
          {messageContent}
          {visibilitySensor}
        </div>
        {/*
         <div className="message__actions">
         <i className="material-icons"  onClick={this.onDelete}>close</i>
         </div>
         <div className="message__actions hide">
         <div className={actionsDropdownClassName}>
         <span className="dropdown__button" onClick={this.showActions}>
         <i className="material-icons">arrow_drop_down</i>
         </span>
         <ul className="dropdown__menu dropdown__menu--right">
         <li className="dropdown__menu__item">
         <i className="icon material-icons">reply</i>
         Reply
         </li>
         <li className="dropdown__menu__item hide">
         <i className="icon material-icons">forward</i>
         Forward
         </li>
         <li className="dropdown__menu__item" onClick={this.onDelete}>
         <i className="icon material-icons">close</i>
         Delete
         </li>
         </ul>
         </div>
         </div>
         */}
      </li>
    );
  }
}

export default MessageItem;
