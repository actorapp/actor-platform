/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { isFunction, throttle } from 'lodash';

import React, { Component, PropTypes } from 'react';

import { MessageChangeReason } from '../../constants/ActorAppConstants';

import PeerUtils from '../../utils/PeerUtils';
import { getMessageState } from '../../utils/MessageUtils';

import Scroller from '../common/Scroller.react';

import DefaultMessageItem from './messages/MessageItem.react';
import DefaultWelcome from './messages/Welcome.react';
import Loading from './messages/Loading.react';

function isLastMessageMine(uid, { messages }) {
  const lastMessage = messages[messages.length - 1];
  return lastMessage && uid === lastMessage.sender.peer.id;
}

class MessagesList extends Component {
  static contextTypes = {
    delegate: PropTypes.object.isRequired
  };

  static propTypes = {
    uid: PropTypes.number.isRequired,
    peer: PropTypes.object.isRequired,
    messages: PropTypes.shape({
      messages: PropTypes.array.isRequired,
      overlay: PropTypes.array.isRequired,
      count: PropTypes.number.isRequired,
      isLoaded: PropTypes.bool.isRequired,
      receiveDate: PropTypes.number.isRequired,
      readDate: PropTypes.number.isRequired,
      readByMeDate: PropTypes.number.isRequired,
      selected: PropTypes.object.isRequired,
      changeReason: PropTypes.oneOf([
        MessageChangeReason.UNKNOWN,
        MessageChangeReason.PUSH,
        MessageChangeReason.UNSHIFT,
        MessageChangeReason.UPDATE
      ]).isRequired
    }).isRequired,
    isMember: PropTypes.bool.isRequired,
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

    this.state = {
      isScrollToBottomNeeded: false
    };

    this.dimensions = null;
    this.isLoading = false;

    this.onResize = this.onResize.bind(this);
    this.onScroll = throttle(this.onScroll.bind(this), 300);
    this.handleScrollToBottom = this.handleScrollToBottom.bind(this);
  }

  shouldComponentUpdate(nextProps, nextState) {
    return nextProps.peer !== this.props.peer ||
           nextProps.messages !== this.props.messages ||
           nextProps.isMember !== this.props.isMember ||
           nextState.isScrollToBottomNeeded !== this.state.isScrollToBottomNeeded;
  }

  componentDidMount() {
    this.restoreScroll();
  }

  componentWillReceiveProps(nextProps) {
    if (!PeerUtils.equals(nextProps.peer, this.props.peer)) {
      this.dimensions = null;
      this.isLoading = false;
    }
  }

  componentDidUpdate(prevProps, prevState) {
    if (prevState.isScrollToBottomNeeded !== this.state.isScrollToBottomNeeded) {
      return;
    }

    const { dimensions, refs: { scroller }, props: { uid, messages } } = this;

    if (messages.changeReason === MessageChangeReason.PUSH) {
      if (!dimensions || isLastMessageMine(uid, messages)) {
        scroller.scrollToBottom();
      }
    } else if (messages.changeReason === MessageChangeReason.UNSHIFT) {
      this.isLoading = false;
      if (dimensions) {
        const currDimensions = scroller.getDimensions();
        scroller.scrollTo(currDimensions.scrollHeight - dimensions.scrollHeight);
      } else {
        scroller.scrollToBottom();
      }
    } else {
      this.restoreScroll();
    }
  }

  onScroll() {
    const dimensions = this.refs.scroller.getDimensions();

    if (dimensions.scrollHeight === dimensions.scrollTop + dimensions.offsetHeight) {
      this.dimensions = null;
    } else {
      this.dimensions = dimensions;
    }

    if (!this.isLoading && dimensions.scrollTop < 100) {
      this.isLoading = true;
      this.props.onLoadMore();
    }

    this.setState({ isScrollToBottomNeeded: dimensions.scrollTop < dimensions.scrollHeight - (2 * dimensions.offsetHeight) });
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

  handleScrollToBottom() {
    this.refs.scroller.scrollToBottom();
  }

  renderHeader() {
    const { peer, isMember, messages } = this.props;

    if (!isMember) {
      return null;
    }

    if (messages.isLoaded) {
      const { Welcome } = this.components;
      return <Welcome peer={peer} key="header" />;
    }

    if (!messages.messages.length) {
      return null;
    }

    return <Loading key="header" />;
  }

  renderMessages() {
    const { uid, peer, messages: { messages, overlay, count, selected, receiveDate, readDate } } = this.props;
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

  renderScrollToBottomButton() {
    const { isScrollToBottomNeeded } = this.state;
    if (!isScrollToBottomNeeded) {
      return null;
    }

    return (
      <div className="chat__scroll-to-bottom" onClick={this.handleScrollToBottom}>
        <i className="material-icons">keyboard_arrow_down</i>
      </div>
    );
  }

  render() {
    return (
      <div className="chat__container">
        <Scroller
          className="chat__messages"
          ref="scroller"
          onScroll={this.onScroll}
          onResize={this.onResize}
        >
          {this.renderHeader()}
          {this.renderMessages()}
        </Scroller>
        {this.renderScrollToBottomButton()}
      </div>
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
