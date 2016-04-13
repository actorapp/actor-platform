/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';

import MessageActionCreators from '../../actions/MessageActionCreators';
import DialogActionCreators from '../../actions/DialogActionCreators';

import UserStore from '../../stores/UserStore';
import MessageStore from '../../stores/MessageStore';

import MessagesList from './MessagesList.react';

class MessagesSection extends Component {
  static propTypes = {
    peer: PropTypes.object.isRequired,
    isMember: PropTypes.bool.isRequired
  };

  static getStores() {
    return [MessageStore];
  }

  static calculateState() {
    return {
      uid: UserStore.getMyId(),
      messages: MessageStore.getState()
    };
  }

  constructor(props) {
    super(props);

    this.onSelect = this.onSelect.bind(this);
    this.onLoadMore = this.onLoadMore.bind(this);
  }

  onSelect(rid) {
    const { selected } = this.state.messages;
    if (selected.has(rid)) {
      MessageActionCreators.setSelected(selected.remove(rid));
    } else {
      MessageActionCreators.setSelected(selected.add(rid));
    }
  }

  onLoadMore() {
    const { peer } = this.props;
    DialogActionCreators.loadMoreMessages(peer);
  }

  render() {
    const { peer, isMember } = this.props;
    const { uid, messages: {
      messages, overlay, isLoaded, receiveDate, readDate, count, selected
    } } = this.state;

    return (
      <MessagesList
        uid={uid}
        peer={peer}
        isMember={isMember}
        messages={messages}
        overlay={overlay}
        count={count}
        selected={selected}
        isLoaded={isLoaded}
        receiveDate={receiveDate}
        readDate={readDate}
        onSelect={this.onSelect}
        onLoadMore={this.onLoadMore}
      />
    );
  }
}

export default Container.create(MessagesSection, { withProps: true });
