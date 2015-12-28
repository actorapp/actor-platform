/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { forEach, map, debounce } from 'lodash';

import React, { Component, PropTypes } from 'react';
import ActorClient from '../../utils/ActorClient';

import { MessageContentTypes, PeerTypes } from '../../constants/ActorAppConstants';

import MessageActionCreators from '../../actions/MessageActionCreators';

import VisibilityStore from '../../stores/VisibilityStore';
import GroupStore from '../../stores/GroupStore';
import DialogStore from '../../stores/DialogStore';
import MessageStore from '../../stores/MessageStore';

import MessageItem from './messages/MessageItem.react';
import Welcome from './messages/Welcome.react';

let _delayed = [];

let flushDelayed = () => {
  forEach(_delayed, (p) => MessageActionCreators.setMessageShown(p.peer, p.message));
  _delayed = [];
};

let flushDelayedDebounced = debounce(flushDelayed, 30, 100);

let lastMessageDate = null,
    lastMessageSenderId = null;

const isOnlyOneDay = (messages) => {
  let _isOnlyOneDay = true;
  if (messages.length > 0) {
    let lastMessageDate = new Date(messages[0].fullDate);
    forEach(messages, (message) => {
      let currentMessageDate = new Date(message.fullDate);

      if (lastMessageDate.getDate() !== currentMessageDate.getDate()) {
        _isOnlyOneDay = false;
      }
      lastMessageDate = message.fullDate
    });
  }
  return _isOnlyOneDay;
};

class MessagesSection extends Component {
  static propTypes = {
    messages: PropTypes.array.isRequired,
    peer: PropTypes.object.isRequired,
    onScroll: PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.state = {
      isOnlyOneDay: isOnlyOneDay(props.messages),
      selectedMessages: MessageStore.getSelected()
    };

    lastMessageDate = new Date();

    VisibilityStore.addListener(this.onAppVisibilityChange);
    MessageStore.addListener(this.onMessagesChange);
  }

  componentWillReceiveProps(nextProps) {
    this.setState({isOnlyOneDay: isOnlyOneDay(nextProps.messages)});
    lastMessageDate = new Date();
  }

  getMessagesListItem = (message, index) => {
    const { isOnlyOneDay, selectedMessages } = this.state;
    const { messages } = this.props;
    const date = message.fullDate;

    const isFirstMessage = index === 0;
    const isThisLastMessage = index > (messages.length - 1) - 3;
    const isNewDay = date.getDate() !== lastMessageDate.getDate();

    let dateDivider = null;
    if (isNewDay && !isOnlyOneDay) {
      const dateDividerFormatOptions = { month: 'long', day: 'numeric' };
      const dateDividerContent = new Intl.DateTimeFormat(undefined, dateDividerFormatOptions).format(date);
      dateDivider = <li className="date-divider">{dateDividerContent}</li>
    }
    const isSameSender = message.sender.peer.id === lastMessageSenderId && !isFirstMessage && !isNewDay;

    const isSelected = selectedMessages.has(message.rid);

    const messageItem = (
      <MessageItem key={message.sortKey}
                   message={message}
                   isNewDay={isNewDay}
                   isSameSender={isSameSender}
                   isThisLastMessage={isThisLastMessage}
                   onSelect={this.handleMessageSelect}
                   isSelected={isSelected}
                   onVisibilityChange={this.onMessageVisibilityChange}
                   peer={this.props.peer}/>
    );

    lastMessageDate = date;
    lastMessageSenderId = message.sender.peer.id;

    return [dateDivider, messageItem];
  };

  onAppVisibilityChange = () => {
    if (VisibilityStore.isAppVisible()) {
      flushDelayed();
    }
  };

  onMessagesChange = () => this.setState({selectedMessages: MessageStore.getSelected()});

  handleMessageSelect = (rid) => {
    const { selectedMessages } = this.state;
    if (selectedMessages.has(rid)) {
      MessageActionCreators.setSelected(selectedMessages.remove(rid));
    } else {
      MessageActionCreators.setSelected(selectedMessages.add(rid));
    }
  };

  onMessageVisibilityChange = (message, isVisible) => {
    const { peer } = this.props;

    if (isVisible) {
      _delayed.push({peer, message});
      if (VisibilityStore.isAppVisible()) {
        flushDelayedDebounced();
      }
    }
  };

  handleScroll = () => {
    const { onScroll } = this.props;
    onScroll && onScroll();
  };

  render() {
    const { messages, peer } = this.props;
    const messagesList = map(messages, this.getMessagesListItem);
    const isMember = DialogStore.isMember();

    const messagesLoading = (
      <li className="message message--loading">
        <div className="message__body col-xs text-center">
          Loading messages from history
        </div>
      </li>
    );

    return (
      <ul className="messages__list" onScroll={this.handleScroll}>
        {
          isMember && messagesList.length < 30
            ? <Welcome peer={peer}/>
            : null
        }
        {
          messagesList.length >= 30
            ? messagesLoading
            : null
        }
        {messagesList}
      </ul>
    );
  }
}

export default MessagesSection;
