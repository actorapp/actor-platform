/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';

import DialogActionCreators from '../../actions/DialogActionCreators';
import MessageActionCreators from '../../actions/MessageActionCreators';

import UserStore from '../../stores/UserStore';
import MessageStore from '../../stores/MessageStore';
import EditMessageStore from '../../stores/EditMessageStore';

import MessagesList from './MessagesList.react';

class MessagesSection extends Component {
  static propTypes = {
    peer: PropTypes.object.isRequired,
    isMember: PropTypes.bool.isRequired
  };

  static getStores() {
    return [MessageStore, EditMessageStore];
  }

  static calculateState() {
    return {
      uid: UserStore.getMyId(),
      messages: MessageStore.getState(),
      editMessage: EditMessageStore.getState()
    };
  }

  constructor(props) {
    super(props);

    this.onEdit = this.onEdit.bind(this);
    this.onSelect = this.onSelect.bind(this);
    this.onLoadMore = this.onLoadMore.bind(this);
  }

  onEdit(message, text) {
    const { peer } = this.props;
    MessageActionCreators.endEdit(peer, message, text);
  }

  onSelect(rid) {
    MessageActionCreators.toggleSelected(rid);
  }

  onLoadMore() {
    DialogActionCreators.loadMoreMessages(this.props.peer);
  }

  render() {
    const { peer, isMember } = this.props;
    const { uid, messages, editMessage } = this.state;

    return (
      <MessagesList
        uid={uid}
        peer={peer}
        messages={messages}
        editMessage={editMessage}
        isMember={isMember}
        onSelect={this.onSelect}
        onLoadMore={this.onLoadMore}
        onEdit={this.onEdit}
      />
    );
  }
}

export default Container.create(MessagesSection, { withProps: true });
