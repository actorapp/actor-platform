/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { findDOMNode } from 'react-dom';

import PeerUtils from '../../utils/PeerUtils';

/**
 * On which scrollTop value start loading older messages
 */
const MAX_LOAD_HEIGHT = 100;

class MessagesScroller extends Component {
  static propTypes = {
    peer: PropTypes.object.isRequired,
    className: PropTypes.string,
    children: PropTypes.node.isRequired,
    onLoadMore: PropTypes.func.isRequired,
  };

  constructor(props) {
    super(props);

    this.onRef = this.onRef.bind(this);
    this.onScroll = this.onScroll.bind(this);
  }

  componentDidMount() {
    this.scrollToBottom();
  }

  componentWillUpdate(nextProps) {
    const isSamePeer = PeerUtils.equals(nextProps.peer, this.props.peer);
    if (isSamePeer) {
      const { scrollTop, offsetHeight, scrollHeight } = this.node;
      this._scrollTop = scrollTop;
      this._scrollHeight = scrollHeight;
      this._shouldScrollBottom = scrollTop + offsetHeight === scrollHeight;
    } else {
      this._shouldScrollBottom = true;
    }
  }

  componentDidUpdate() {
    const { scrollHeight } = this.node;
    // check if container become bigger
    if (scrollHeight > this._scrollHeight) {
      requestAnimationFrame(() => {
        this.node.scrollTop = this._scrollTop + (scrollHeight - this._scrollHeight);
      });
      return;
    }

    const { scrollTop } = this.node;
    // check if scroll on top on container
    if (scrollTop === 0) {
      setImmediate(() => {
        this.props.onLoadMore();
      });
      return;
    }

    if (this._shouldScrollBottom) {
      this.scrollToBottom();
    }
  }

  scrollToBottom() {
    this.node.scrollTop = this.node.scrollHeight;
  }

  onRef(node) {
    this.node = node;
  }

  onScroll() {
    const { scrollTop } = this.node;
    if (scrollTop <= MAX_LOAD_HEIGHT) {
      this.props.onLoadMore();
    }
  }

  render() {
    return (
      <div className={this.props.className} onScroll={this.onScroll} ref={this.onRef}>
        {this.props.children}
      </div>
    );
  }
}

export default MessagesScroller;
