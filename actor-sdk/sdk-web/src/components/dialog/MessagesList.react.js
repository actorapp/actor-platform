/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { isFunction } from 'lodash';

import React, { Component, PropTypes } from 'react';
import {shouldComponentUpdate} from 'react-addons-pure-render-mixin';

import { getMessageState } from '../../utils/MessageUtils';

import MessagesScroller from './MessagesScroller.react';

import DefaultMessageItem from './messages/MessageItem.react';
import DefaultWelcome from './messages/Welcome.react';
import Loading from './messages/Loading.react';

class MessagesList extends Component {
  static contextTypes = {
    delegate: PropTypes.object.isRequired
  };

  static propTypes = {
    uid: PropTypes.number.isRequired,
    peer: PropTypes.object.isRequired,
    messages: PropTypes.array.isRequired,
    overlay: PropTypes.array.isRequired,
    count: PropTypes.number.isRequired,
    selected: PropTypes.object.isRequired,
    isMember: PropTypes.bool.isRequired,
    isLoaded: PropTypes.bool.isRequired,
    receiveDate: PropTypes.number.isRequired,
    readDate: PropTypes.number.isRequired,
    onSelect: PropTypes.func.isRequired,
    onLoadMore: PropTypes.func.isRequired
  };

  constructor(props, context) {
    super(props, context);

    const { dialog } = context.delegate.components;
    if (dialog && dialog.messages) {
      this.components = {
        MessageItem: isFunction(dialog.messages.message) ? dialog.messages.message : DefaultMessageItem,
        Welcome: isFunction(dialog.messages.welcome) ? dialog.messages.welcome : DefaultWelcome
      };
    } else {
      this.components = {
        MessageItem: DefaultMessageItem,
        Welcome: DefaultWelcome
      };
    }

    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
  }

  renderHeader() {
    const { peer, isMember, messages, isLoaded } = this.props;
    const { Welcome } = this.components;

    if (!isMember) {
      return null;
    }

    if (!isLoaded && messages.length >= 30) {
      return <Loading key="header" />;
    }

    return <Welcome peer={peer} key="header" />;
  }

  renderMessages() {
    const { uid, peer, messages, overlay, count, selected, receiveDate, readDate } = this.props;
    const { MessageItem } = this.components;

    const result = [];
    for (let index = messages.length - count; index < messages.length; index++) {
      const overlayItem = overlay[index];
      if (overlayItem && overlayItem.dateDivider) {
        result.push(
          <div className="date-divider" key={overlayItem.dateDivider}>
            {overlayItem.dateDivider}
          </div>
        );
      }

      const message = messages[index];

      result.push(
        <MessageItem
          peer={peer}
          message={message}
          state={getMessageState(message, uid, receiveDate, readDate)}
          isShort={overlayItem.useShort}
          isSelected={selected.has(message.rid)}
          onSelect={this.props.onSelect}
          key={message.sortKey}
        />
      );
    }

    return result;
  }

  render() {
    const { peer, onLoadMore } = this.props;

    return (
      <MessagesScroller className="messages" peer={peer} onLoadMore={onLoadMore}>
        <div className="messages__list">
          {this.renderHeader()}
          {this.renderMessages()}
        </div>
      </MessagesScroller>
    )
  }
}

export default MessagesList;
