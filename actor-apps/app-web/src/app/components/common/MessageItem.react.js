import React from 'react';
import ReactMixin from 'react-mixin';
import addons from 'react/addons';

import classnames from 'classnames';

import VisibilitySensor from 'react-visibility-sensor';

import AvatarItem from 'components/common/AvatarItem.react';
import Text from './Text.react';
import Image from './Image.react';
import Document from './Document.react';
import State from './State.react';

import DialogActionCreators from 'actions/DialogActionCreators';
import { MessageContentTypes } from 'constants/ActorAppConstants';

let lastMessageSenderId = null,
    lastMessageContentType = null;

const {addons: { PureRenderMixin }} = addons;

@ReactMixin.decorate(PureRenderMixin)
class MessageItem extends React.Component {
  static propTypes = {
    peer: React.PropTypes.object.isRequired,
    message: React.PropTypes.object.isRequired,
    newDay: React.PropTypes.bool,
    index: React.PropTypes.number,
    onVisibilityChange: React.PropTypes.func
  };

  constructor(props) {
    super(props);

    this.state = {
      isActionsShown: false
    };
  }

  onClick = () => {
    DialogActionCreators.selectDialogPeerUser(this.props.message.sender.peer.id);
  };

  onVisibilityChange = (isVisible) => {
    this.props.onVisibilityChange(this.props.message, isVisible);
  };

  onDelete = () => {
    DialogActionCreators.deleteMessages(this.props.peer, [this.props.message.rid]);
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
    const { message, newDay } = this.props;
    const isFirstMessage = this.props.index === 0;

    let header,
        messageContent,
        visibilitySensor,
        leftBlock;

    let isSameSender = message.sender.peer.id === lastMessageSenderId &&
                       lastMessageContentType !== MessageContentTypes.SERVICE &&
                       message.content.content !== MessageContentTypes.SERVICE &&
                       !isFirstMessage &&
                       !newDay;

    let messageClassName = classnames({
      'message': true,
      'row': true,
      'message--same-sender': isSameSender
    });

    let actionsDropdownClassName = classnames({
      'dropdown': true,
      'dropdown--small': true,
      'dropdown--opened': this.state.isActionsShown
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
            <a onClick={this.onClick}>{message.sender.title}</a>
          </h3>
          <time className="message__timestamp">{message.date}</time>
          <State message={message}/>
        </header>
      );
    }

    if (message.content.content === MessageContentTypes.SERVICE) {
      leftBlock = null;
      header = null;
    }

    switch (message.content.content) {
      case MessageContentTypes.SERVICE:
        messageContent = <div className="message__content message__content--service">{message.content.text}</div>;
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

    if (this.props.onVisibilityChange) {
      visibilitySensor = <VisibilitySensor onChange={this.onVisibilityChange}/>;
    }

    lastMessageSenderId = message.sender.peer.id;
    lastMessageContentType = message.content.content;

    return (
      <li className={messageClassName}>
        {leftBlock}
        <div className="message__body col-xs">
          {header}
          {messageContent}
          {visibilitySensor}
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
      </li>
    );
  }
}

export default MessageItem;
