/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';

import DialogActionCreators from '../../actions/DialogActionCreators';
import MessageActionCreators from '../../actions/MessageActionCreators';

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
    MessageActionCreators.toggleSelected(rid);
  }

  onLoadMore() {
    DialogActionCreators.loadMoreMessages(this.props.peer);
  }

  render() {
    const { peer, isMember } = this.props;
    const {
      uid,
      messages: {
        messages, overlay, receiveDate, readDate, isLoaded, isLoading, count, selected
      }
    } = this.state;

    return (
      <MessagesList
        uid={uid}
        peer={peer}
        isMember={isMember}
        messages={messages}
        overlay={overlay}
        readDate={readDate}
        receiveDate={receiveDate}
        count={count}
        selected={selected}
        isLoaded={isLoaded}
        isLoading={isLoading}
        onSelect={this.onSelect}
        onLoadMore={this.onLoadMore}
      />
    );
  }
}

export default Container.create(MessagesSection, { withProps: true });
