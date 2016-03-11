/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { isFunction } from 'lodash';
import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import { escapeWithEmoji } from '../../../utils/EmojiUtils';
import PeerUtils from '../../../utils/PeerUtils';
import { MessageContentTypes } from '../../../constants/ActorAppConstants';

import DialogActionCreators from '../../../actions/DialogActionCreators';
import ActivityActionCreators from '../../../actions/ActivityActionCreators';
import DropdownActionCreators from '../../../actions/DropdownActionCreators';

import DropdownStore from '../../../stores/DropdownStore';

import SvgIcon from '../../common/SvgIcon.react';
import AvatarItem from '../../common/AvatarItem.react';
import State from './State.react';
import Reactions from './Reactions.react';

// Default message content components
import DefaultService from './Service.react';
import DefaultText from './Text.react';
import DefaultPhoto from './Photo.react';
import DefaultDocument from './Document.react';
import DefaultVoice from './Voice.react';
import DefaultContact from './Contact.react';
import DefaultLocation from './Location.react';
import DefaultModern from './Modern.react';
import DefaultSticker from './Sticker.react';

class MessageItem extends Component {
  static getStores() {
    return [DropdownStore];
  }

  static calculateState(prevState, props) {
    return {
      isHighlighted: props && props.message ? DropdownStore.isMessageDropdownOpen(props.message.rid) : false
    }
  }

  static propTypes = {
    peer: PropTypes.object.isRequired,
    message: PropTypes.object.isRequired,
    state: PropTypes.string.isRequired,
    isShort: PropTypes.bool.isRequired,
    isSelected: PropTypes.bool,
    onSelect: PropTypes.func
  };

  static contextTypes = {
    delegate: PropTypes.object,
    isExperimental: PropTypes.bool
  };

  shouldComponentUpdate(nextProps) {
    return (this.props.message !== nextProps.message || this.props.isShort !== nextProps.isShort);
  }

  onClick = () => {
    const { message, peer } = this.props;

    if (PeerUtils.equals(peer, message.sender.peer)) {
      ActivityActionCreators.show();
    } else {
      DialogActionCreators.selectDialogPeerUser(message.sender.peer.id);
    }
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
    const { peer, message, state, isShort, isSelected } = this.props;
    const { isHighlighted } = this.state;
    const { delegate, isExperimental } = this.context;

    let Service, Text, Modern, Photo, Document, Voice, Contact, Location, Sticker;
    if (delegate.components.dialog && delegate.components.dialog.messages && !isFunction(delegate.components.dialog.messages.message)) {
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

    const messageClassName = classnames('message', {
      'message--same-sender': isShort,
      'message--active': isHighlighted,
      'message--selected': isSelected
    });
    const messageActionsMenuClassName = classnames('message__actions__menu', {
      'message__actions__menu--opened': isHighlighted
    });

    if (isShort) {
      leftBlock = (
        <div className="message__info text-right">
          <time className="message__timestamp">{message.date}</time>
          <State state={state} />
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
            <a onClick={this.onClick}>
              {
                message.sender.title
                  ? <span className="message__sender__name" dangerouslySetInnerHTML={{ __html: messageSender }}/>
                  : null
              }
              {
                message.sender.userName
                  ? <span className="message__sender__nick">@{message.sender.userName}</span>
                  : null
              }
            </a>
          </h3>
          <time className="message__timestamp">{message.date}</time>
          <State state={state} />
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
        messageContent = (<Photo content={message.content} className="message__content message__content--photo"
                                loadedClassName="message__content--photo--loaded"/>);
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
      <div className={messageClassName}>
        <div className="row">
          {leftBlock}
          <div className="message__body col-xs">
            {header}
            {messageContent}
          </div>
          <div className="message__actions">
            <Reactions peer={peer} message={message}/>

            <div className={messageActionsMenuClassName} onClick={this.showActions}>
              <SvgIcon className="icon icon--dropdown" glyph="cog" />
            </div>

            {
              isExperimental
                ? <div className="message__actions__selector" onClick={this.toggleMessageSelection}>
                    <i className="icon material-icons icon-check"></i>
                  </div>
                : null
            }
          </div>
        </div>
      </div>
    );
  }
}

export default Container.create(MessageItem, { withProps: true });
