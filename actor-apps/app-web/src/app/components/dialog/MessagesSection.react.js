/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import _ from 'lodash';

import React, { Component } from 'react';
import { Container } from 'flux/utils';

import { MessageContentTypes } from 'constants/ActorAppConstants';

import MessageActionCreators from 'actions/MessageActionCreators';

import VisibilityStore from 'stores/VisibilityStore';

import MessageItem from './messages/MessageItem.react';

let _delayed = [];

let flushDelayed = () => {
  _.forEach(_delayed, (p) => MessageActionCreators.setMessageShown(p.peer, p.message));
  _delayed = [];
};

let flushDelayedDebounced = _.debounce(flushDelayed, 30, 100);

let lastMessageDate = null,
    lastMessageSenderId = null;

class MessagesSection extends Component {
  static propTypes = {
    messages: React.PropTypes.array.isRequired,
    peer: React.PropTypes.object.isRequired
  };

  static getStores = () => [VisibilityStore];
  static calculateState = () => {};

  constructor(props) {
    super(props);
    VisibilityStore.addListener(this.onAppVisibilityChange);
  }

  getMessagesListItem = (message, index) => {
    const date = message.fullDate;//new Date(message.fullDate);

    if (lastMessageDate === null) {
      lastMessageDate = new Date(message.fullDate);
    }

    const isFirstMessage = index === 0;
    const isNewDay = date.getDate() !== lastMessageDate.getDate();

    let dateDivider = null;
    if (isNewDay) {
      const dateDividerFormatOptions = { month: 'long', day: 'numeric' };
      const dateDividerContent = new Intl.DateTimeFormat(undefined, dateDividerFormatOptions).format(date);
      dateDivider = <li className="date-divider">{dateDividerContent}</li>
    }
    const isSameSender = message.sender.peer.id === lastMessageSenderId && !isFirstMessage && !isNewDay;

    const messageItem = (
      <MessageItem key={message.sortKey}
                   message={message}
                   isNewDay={isNewDay}
                   isSameSender={isSameSender}
                   onVisibilityChange={this.onMessageVisibilityChange}
                   peer={this.props.peer}/>
    );

    lastMessageDate = date; //new Date(message.fullDate);
    lastMessageSenderId = message.sender.peer.id;

    return [dateDivider, messageItem];
  };

  onAppVisibilityChange = () => {
    if (VisibilityStore.isAppVisible()) {
      flushDelayed();
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

  render() {
    const { messages } = this.props;
    const messagesList = _.map(messages, this.getMessagesListItem);

    return (
      <ul className="messages__list">
        {messagesList}
      </ul>
    );
  }
}

export default Container.create(MessagesSection);
