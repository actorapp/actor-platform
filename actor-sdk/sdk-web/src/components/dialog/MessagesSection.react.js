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

class MessagesSection extends Component {
  static propTypes = {
    messages: PropTypes.array.isRequired,
    overlay: PropTypes.array.isRequired,
    peer: PropTypes.object.isRequired,
    onScroll: PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.state = {
      selectedMessages: MessageStore.getSelected()
    };

    VisibilityStore.addListener(this.onAppVisibilityChange);
    MessageStore.addListener(this.onMessagesChange);
  }

  getMessagesListItem = (message, index) => {
    const { selectedMessages } = this.state;
    const { overlay } = this.props;

    let dateDivider = null;
    if (overlay[index].dateDivider) {
      dateDivider = <li className="date-divider">{overlay[index].dateDivider}</li>
    }
    const isShortMessage = overlay[index].useShort;

    const isSelected = selectedMessages.has(message.rid);

    const messageItem = (
      <MessageItem key={message.sortKey}
                   message={message}
                   isShortMessage={isShortMessage}
                   onSelect={this.handleMessageSelect}
                   isSelected={isSelected}
                   onVisibilityChange={this.onMessageVisibilityChange}
                   peer={this.props.peer}/>
    );

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
