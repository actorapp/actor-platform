/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { isFunction, noop } from 'lodash';
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
import GroupStore from '../../../stores/GroupStore';

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
  static contextTypes = {
    delegate: PropTypes.object,
    isExperimental: PropTypes.bool
  }

  static propTypes = {
    peer: PropTypes.object.isRequired,
    message: PropTypes.object.isRequired,
    state: PropTypes.string.isRequired,
    isShort: PropTypes.bool.isRequired,
    isEditing: PropTypes.bool.isRequired,
    isSelected: PropTypes.bool.isRequired,
    onSelect: PropTypes.func.isRequired
  }

  static defaultProps = {
    isSelected: false,
    onSelect: noop
  };

  static getStores() {
    return [DropdownStore];
  }

  static calculateState(prevState, props) {
    return {
      isHighlighted: props && props.message ? DropdownStore.isMessageDropdownOpen(props.message.rid) : false
    }
  }

  constructor(props, context) {
    super(props, context);

    this.onClick = this.onClick.bind(this);
  }

  componentWillMount() {
    const { dialog } = this.context.delegate.components;
    if (dialog && dialog.messages) {
      this.components = {
        Service: isFunction(dialog.messages.service) ? dialog.messages.service : DefaultService,
        Text: isFunction(dialog.messages.text) ? dialog.messages.text : DefaultText,
        Modern: isFunction(dialog.messages.modern) ? dialog.messages.modern : DefaultModern,
        Photo: isFunction(dialog.messages.photo) ? dialog.messages.photo : DefaultPhoto,
        Document: isFunction(dialog.messages.document) ? dialog.messages.document : DefaultDocument,
        Voice: isFunction(dialog.messages.voice) ? dialog.messages.voice : DefaultVoice,
        Contact: isFunction(dialog.messages.contact) ? dialog.messages.contact : DefaultContact,
        Location: isFunction(dialog.messages.location) ? dialog.messages.location : DefaultLocation,
        Sticker:  isFunction(dialog.messages.sticker) ? dialog.messages.sticker : DefaultSticker
      };
    } else {
      this.components = {
        Service: DefaultService,
        Text: DefaultText,
        Modern: DefaultModern,
        Photo: DefaultPhoto,
        Document: DefaultDocument,
        Voice: DefaultVoice,
        Contact: DefaultContact,
        Location: DefaultLocation,
        Sticker: DefaultSticker
      };
    }
  }

  shouldComponentUpdate(nextProps) {
    return this.props.message !== nextProps.message ||
           this.props.isShort !== nextProps.isShort;
  }

  onClick() {
    const { message, peer } = this.props;

    if (PeerUtils.equals(peer, message.sender.peer)) {
      ActivityActionCreators.show();
    } else {
      DialogActionCreators.selectDialogPeerUser(message.sender.peer.id);
    }
  }

  showActions = (event) => {
    const { message } = this.props;
    DropdownActionCreators.openMessageActions(event.target.getBoundingClientRect(), message);
  };

  toggleMessageSelection = () => {
    const { message, onSelect } = this.props;
    onSelect(message.rid);
  };

  renderTitle() {
    const { message, peer } = this.props;

    if (PeerUtils.isGroupBot(message.sender)) {
      const group = GroupStore.getGroup(peer.id);
      return (
        <span className="message__sender__name" dangerouslySetInnerHTML={{ __html: escapeWithEmoji(group.name) }}/>
      );
    } else {
      return (
        <span className="message__sender__name" dangerouslySetInnerHTML={{ __html: escapeWithEmoji(message.sender.title) }}/>
      );
    }
  }

  renderHeader() {
    const { isShort, message, state } = this.props;

    if (isShort) {
      return null;
    }

    return (
      <header className="message__header">
        <h3 className="message__sender">
          <a onClick={this.onClick}>
            {this.renderTitle()}
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

  renderLeftBlock() {
    const { isShort, message, state, peer } = this.props

    if (isShort) {
      return (
        <div className="message__info">
          <time className="message__timestamp">{message.date}</time>
          <State state={state} />
        </div>
      );
    } else {
      if (PeerUtils.isGroupBot(message.sender)) {
        const group = GroupStore.getGroup(peer.id);
        return (
          <div className="message__info message__info--avatar">
            <AvatarItem
              className="message__avatar"
              image={group.avatar}
              placeholder={group.placeholder}
              title={group.name}
              onClick={this.onClick}
            />
          </div>
        )
      } else {
        return (
          <div className="message__info message__info--avatar">
            <AvatarItem
              className="message__avatar"
              image={message.sender.avatar}
              placeholder={message.sender.placeholder}
              title={message.sender.title}
              onClick={this.onClick}
            />
          </div>
        )
      }
    }
  }

  renderContent() {
    const { message } = this.props;
    const { Service, Text, Photo, Document, Voice, Contact, Location, Modern, Sticker } = this.components;

    switch (message.content.content) {
      case MessageContentTypes.SERVICE:
        return (
          <Service
            {...message.content}
            className="message__content message__content--service"
          />
        );
      case MessageContentTypes.TEXT:
        return (
          <Text
            {...message.content}
            className="message__content message__content--text"
          />
        );
      case MessageContentTypes.PHOTO:
        return (
          <Photo
            {...message.content}
            className="message__content message__content--photo"
            loadedClassName="message__content--photo--loaded"
          />
        );
      case MessageContentTypes.DOCUMENT:
        return (
          <Document
            {...message.content}
            className="message__content message__content--document"
          />
        );
      case MessageContentTypes.VOICE:
        return (
          <Voice
            {...message.content}
            className="message__content message__content--voice"
          />
        );
      case MessageContentTypes.CONTACT:
        return (
          <Contact
            {...message.content}
            className="message__content message__content--contact"
          />
        );
      case MessageContentTypes.LOCATION:
        return (
          <Location
            {...message.content}
            className="message__content message__content--location"
          />
        );
      case MessageContentTypes.TEXT_MODERN:
        return (
          <Modern
            {...message.content}
            className="message__content message__content--modern"
          />
        );
      case MessageContentTypes.STICKER:
        return (
          <Sticker
            {...message.content}
            className="message__content message__content--sticker"
          />
        );
      default:
        return null;
    }
  }

  renderActions() {
    const { peer, message } = this.props;
    const { isHighlighted } = this.state;
    const { isExperimental } = this.context;

    const messageActionsMenuClassName = classnames('message__actions__menu', {
      'message__actions__menu--opened': isHighlighted
    });

    return (
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
    );
  }

  render() {
    const { isShort, isSelected, isEditing } = this.props;
    const { isHighlighted } = this.state;

    const messageClassName = classnames('message', {
      'message--short': isShort,
      'message--active': isHighlighted,
      'message--selected': isSelected,
      'message--editing': isEditing
    });

    return (
      <div className={messageClassName}>
        {this.renderLeftBlock()}
        <div className="message__body">
          {this.renderHeader()}
          {this.renderContent()}
        </div>
        {this.renderActions()}
      </div>
    );
  }
}

export default Container.create(MessageItem, { withProps: true });
