/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { forEach, debounce } from 'lodash';

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';

import MessageActionCreators from '../../actions/MessageActionCreators';
import DialogActionCreators from '../../actions/DialogActionCreators';

import VisibilityStore from '../../stores/VisibilityStore';
import MessageStore from '../../stores/MessageStore';

import MessagesList from './MessagesList.react';


let _delayed = [];
let flushDelayed = () => {
  forEach(_delayed, (p) => MessageActionCreators.setMessageShown(p.peer, p.message));
  _delayed = [];
};

let flushDelayedDebounced = debounce(flushDelayed, 30, {maxWait: 100});

class MessagesSection extends Component {
  static propTypes = {
    peer: PropTypes.object.isRequired,
    isMember: PropTypes.bool.isRequired
  };

  static getStores() {
    return [MessageStore, VisibilityStore];
  }

  static calculateState() {
    return {
      messages: MessageStore.getMessages(),
      overlay: MessageStore.getOverlay(),
      messagesCount: MessageStore.getRenderMessagesCount(),
      selectedMessages: MessageStore.getSelected(),
      isAllMessagesLoaded: MessageStore.isLoaded(),
      isAppVisible: VisibilityStore.isAppVisible()
    };
  }

  constructor(props) {
    super(props);

    this.onSelect = this.onSelect.bind(this);
    this.onLoadMore = this.onLoadMore.bind(this);
    this.onVisibilityChange = this.onVisibilityChange.bind(this);
  }

  componentDidUpdate() {
    const { isAppVisible } = this.state;
    if (isAppVisible) {
      flushDelayed();
    }
  }

  onSelect(rid) {
    const { selectedMessages } = this.state;
    if (selectedMessages.has(rid)) {
      MessageActionCreators.setSelected(selectedMessages.remove(rid));
    } else {
      MessageActionCreators.setSelected(selectedMessages.add(rid));
    }
  }

  onLoadMore() {
    const { peer } = this.props;
    DialogActionCreators.loadMoreMessages(peer);
  }

  onVisibilityChange(message, isVisible) {
    const { peer } = this.props;

    if (isVisible) {
      _delayed.push({peer, message});
      if (VisibilityStore.isAppVisible()) {
        flushDelayedDebounced();
      }
    }
  }

  render() {
    const { peer, isMember } = this.props;
    const { messages, overlay, messagesCount, selectedMessages, isAllMessagesLoaded } = this.state;

    return (
      <MessagesList
        peer={peer}
        overlay={overlay}
        messages={messages}
        count={messagesCount}
        selectedMessages={selectedMessages}
        isMember={isMember}
        isAllMessagesLoaded={isAllMessagesLoaded}
        onSelect={this.onSelect}
        onLoadMore={this.onLoadMore}
        onVisibilityChange={this.onVisibilityChange}
      />
    );
  }
}

export default Container.create(MessagesSection, {withProps: true});
