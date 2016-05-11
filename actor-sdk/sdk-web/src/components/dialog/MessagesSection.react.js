/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';

import DialogActionCreators from '../../actions/DialogActionCreators';
import MessageActionCreators from '../../actions/MessageActionCreators';

import MessageStore from '../../stores/MessageStore';

import MessagesList from './MessagesList.react';

class MessagesSection extends Component {
  static propTypes = {
    uid: PropTypes.number.isRequired,
    peer: PropTypes.object.isRequired,
    isMember: PropTypes.bool.isRequired
  };

  static getStores() {
    return [MessageStore];
  }

  static calculateState() {
    return MessageStore.getState();
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
    const { uid, peer, isMember } = this.props;

    return (
      <MessagesList
        uid={uid}
        peer={peer}
        messages={this.state}
        isMember={isMember}
        onSelect={this.onSelect}
        onLoadMore={this.onLoadMore}
        onEdit={this.onEdit}
      />
    );
  }
}

export default Container.create(MessagesSection);
