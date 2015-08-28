/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import _ from 'lodash';

import React from 'react';
import ReactMixin from 'react-mixin';
import addons from 'react/addons';
import { MessageContentTypes } from 'constants/ActorAppConstants';

import MessageActionCreators from 'actions/MessageActionCreators';

import VisibilityStore from 'stores/VisibilityStore';

import MessageItem from './messages/MessageItem.react';

const {addons: { PureRenderMixin }} = addons;

let _delayed = [];

let flushDelayed = () => {
  _.forEach(_delayed, (p) => {
    MessageActionCreators.setMessageShown(p.peer, p.message);
  });

  _delayed = [];
};

let flushDelayedDebounced = _.debounce(flushDelayed, 30, 100);

let lastMessageDate = null,
    lastMessageSenderId = null;

@ReactMixin.decorate(PureRenderMixin)
class MessagesSection extends React.Component {
  static propTypes = {
    messages: React.PropTypes.array.isRequired,
    peer: React.PropTypes.object.isRequired
  };

  constructor(props) {
    super(props);

    VisibilityStore.addChangeListener(this.onAppVisibilityChange);
  }

  componentWillUnmount() {
    VisibilityStore.removeChangeListener(this.onAppVisibilityChange);
  }

  getMessagesListItem = (message, index) => {
    let date = new Date(message.fullDate);

    const month = [
      'January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'
    ];

    if (lastMessageDate === null) {
      lastMessageDate = new Date(message.fullDate);
    }

    const isFirstMessage = index === 0;
    const isNewDay = date.getDate() !== lastMessageDate.getDate();

    const dateDivider = isNewDay ? <li className="date-divider">{month[date.getMonth()]} {date.getDate()}</li> : null;
    const isSameSender = message.sender.peer.id === lastMessageSenderId && !isFirstMessage && !isNewDay;

    const messageItem = (
      <MessageItem key={message.sortKey}
                   message={message}
                   isNewDay={isNewDay}
                   isSameSender={isSameSender}
                   onVisibilityChange={this.onMessageVisibilityChange}
                   peer={this.props.peer}/>
    );

    lastMessageDate = new Date(message.fullDate);
    lastMessageSenderId = message.sender.peer.id;

    return [dateDivider, messageItem];
  };

  onAppVisibilityChange = () => {
    if (VisibilityStore.isVisible) {
      flushDelayed();
    }
  };

  onMessageVisibilityChange = (message, isVisible) => {
    if (isVisible) {
      _delayed.push({peer: this.props.peer, message: message});

      if (VisibilityStore.isVisible) {
        flushDelayedDebounced();
      }
    }
  };

  render() {
    const messages = _.map(this.props.messages, this.getMessagesListItem);

    return (
      <ul className="messages__list">
        {messages}
      </ul>
    );
  }
}

export default MessagesSection;
