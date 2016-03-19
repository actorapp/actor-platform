/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { forEach, map, debounce, isFunction } from 'lodash';

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';

import MessageActionCreators from '../../actions/MessageActionCreators';

import VisibilityStore from '../../stores/VisibilityStore';
import DialogStore from '../../stores/DialogStore';
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
    messages: PropTypes.array.isRequired,
    overlay: PropTypes.array.isRequired,
    count: PropTypes.number.isRequired,
    isMember: PropTypes.bool.isRequired,
    onLoadMore: PropTypes.func.isRequired
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

  componentDidUpdate() {
    const { isAppVisible } = this.state;
    if (isAppVisible) {
      flushDelayed();
    }
  };

  onMessageSelect = (rid) => {
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

  render() {
    const { peer, overlay, messages, count, isMember } = this.props;
    const { selectedMessages, isAllMessagesLoaded } = this.state;

    return (
      <MessagesList
        peer={peer}
        overlay={overlay}
        messages={messages}
        count={count}
        selectedMessages={selectedMessages}
        isMember={isMember}
        isAllMessagesLoaded={isAllMessagesLoaded}
        onSelect={this.onMessageSelect}
        onVisibilityChange={this.onMessageVisibilityChange}
        onLoadMore={this.props.onLoadMore}
      />
    );
  }
}

export default Container.create(MessagesSection, {pure: false, withProps: true});
