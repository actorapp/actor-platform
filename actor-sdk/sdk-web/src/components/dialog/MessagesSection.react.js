/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { forEach, map, debounce } from 'lodash';

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import ActorClient from '../../utils/ActorClient';
import Scrollbar from '../common/Scrollbar.react';
import { MessageContentTypes, PeerTypes } from '../../constants/ActorAppConstants';

import MessageActionCreators from '../../actions/MessageActionCreators';

import VisibilityStore from '../../stores/VisibilityStore';
//import GroupStore from '../../stores/GroupStore';
import DialogStore from '../../stores/DialogStore';
import MessageStore from '../../stores/MessageStore';

import MessageItem from './messages/MessageItem.react';
import Welcome from './messages/Welcome.react';
import Loading from './messages/Loading.react';

let _delayed = [];

let flushDelayed = () => {
  forEach(_delayed, (p) => MessageActionCreators.setMessageShown(p.peer, p.message));
  _delayed = [];
};

let flushDelayedDebounced = debounce(flushDelayed, 30, {maxWait: 100});

class MessagesSection extends Component {
  static propTypes = {
    messages: PropTypes.array.isRequired,
    overlay: PropTypes.array.isRequired,
    peer: PropTypes.object.isRequired,
    onScroll: PropTypes.func.isRequired
  };

  static getStores() {
    return [MessageStore, VisibilityStore]
  }

  static calculateState() {
    return {
      selectedMessages: MessageStore.getSelected(),
      isAllMessagesLoaded: MessageStore.isLoaded(),
      isAppVisible: VisibilityStore.isAppVisible()
    }
  }

  constructor(props) {
    super(props);
  }

  getMessagesListItem = (message, index) => {
    const { selectedMessages } = this.state;
    const { peer, overlay } = this.props;

    const dateDivider = (overlay[index] && overlay[index].dateDivider)
      ? <li className="date-divider">{overlay[index].dateDivider}</li>
      : null;

    const isSelected = selectedMessages.has(message.rid);

    const messageItem = (
      <MessageItem key={message.sortKey}
                   message={message}
                   overlay={overlay[index]}
                   onSelect={this.handleMessageSelect}
                   isSelected={isSelected}
                   onVisibilityChange={this.onMessageVisibilityChange}
                   peer={peer}/>
    );

    return dateDivider ? [dateDivider, messageItem] : messageItem;
  };

  componentDidUpdate() {
    const { isAppVisible } = this.state;
    if (isAppVisible) {
      flushDelayed();
    }
  };

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
    const { isAllMessagesLoaded } = this.state;
    const isMember = DialogStore.isMember();
    const messagesList = map(messages, this.getMessagesListItem);

    return (
      <Scrollbar onScroll={this.handleScroll} ref="messagesScroll">
        <ul className="messages__list">
          {
            (isMember && isAllMessagesLoaded) || (isMember && messagesList.length < 30)
              ? <Welcome peer={peer}/>
              : null
          }
          {
            !isAllMessagesLoaded && messagesList.length >= 30
              ? <Loading/>
              : null
          }
          {messagesList}
        </ul>
      </Scrollbar>
    );
  }
}

export default Container.create(MessagesSection, {pure: false, withProps: true});
