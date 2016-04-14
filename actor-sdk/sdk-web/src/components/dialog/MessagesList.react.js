/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { isFunction, last, debounce } from 'lodash';

import React, { Component, PropTypes } from 'react';
import {shouldComponentUpdate} from 'react-addons-pure-render-mixin';

import { getMessageState } from '../../utils/MessageUtils';
import PeerUtils from '../../utils/PeerUtils';

import Scroller from '../common/Scroller.react';

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
    isLoading: PropTypes.bool.isRequired,
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

    this.dimensions = null;

    this.onScroll = this.onScroll.bind(this);
    this.onResize = this.onResize.bind(this);
    this.onLoadMore = debounce(this.onLoadMore.bind(this), 60, {
      maxWait: 180
    });
    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
  }

  componentDidMount() {
    this.restoreScroll();
  }

  componentWillReceiveProps(nextProps) {
    if (!PeerUtils.equals(nextProps.peer, this.props.peer)) {
      this.dimensions = null;
    }
  }

  componentDidUpdate(prevProps) {
    const { dimensions, refs: { scroller }, props: { uid, messages, count } } = this;

    const lastMessage = last(messages);
    const isPush = lastMessage && lastMessage !== last(prevProps.messages);
    if (isPush) {
      const isMyMessage = uid === lastMessage.sender.peer.id;
      if (isMyMessage || !dimensions) {
        scroller.scrollToBottom();
      }
    } else {
      const isFirstMessageChanged = prevProps.count !== count || (messages[0] !== prevProps.messages[0] && prevProps.isLoading);
      if (isFirstMessageChanged && dimensions) {
        const currDimensions = scroller.getDimensions();
        scroller.scrollTo(currDimensions.scrollHeight - dimensions.scrollHeight);
      }
    }
  }

  onLoadMore() {
    const dimensions = this.refs.scroller.getDimensions();
    if (dimensions.scrollTop < dimensions.offsetHeight && !this.props.isLoading) {
      this.props.onLoadMore();
    }
  }

  onScroll() {
    const dimensions = this.refs.scroller.getDimensions();
    if (dimensions.scrollHeight === dimensions.scrollTop + dimensions.offsetHeight) {
      this.dimensions = null;
    } else {
      this.dimensions = dimensions;
    }

    this.onLoadMore();
  }

  onResize() {
    const { dimensions, refs: { scroller } } = this;
    if (dimensions) {
      const ratio = dimensions.scrollTop / dimensions.scrollHeight;
      const nextDimensions = scroller.getDimensions();
      scroller.scrollTo(ratio * nextDimensions.scrollHeight);
      this.dimensions = nextDimensions;
    } else {
      scroller.scrollToBottom();
    }
  }

  renderHeader() {
    const { peer, isMember, isLoaded } = this.props;

    if (!isMember) {
      return null;
    }

    if (isLoaded) {
      const { Welcome } = this.components;
      return <Welcome peer={peer} key="header" />;
    }

    return <Loading key="header" />;
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
    return (
      <Scroller
        className="chat__messages"
        ref="scroller"
        onScroll={this.onScroll}
        onResize={this.onResize}
      >
        {this.renderHeader()}
        {this.renderMessages()}
      </Scroller>
    )
  }

  restoreScroll() {
    const { dimensions, refs: { scroller } } = this;

    if (dimensions) {
      scroller.scrollTo(dimensions.scrollTop);
    } else {
      scroller.scrollToBottom();
    }
  }
}

export default MessagesList;
