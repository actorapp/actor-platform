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
      this._scrollHeight = scrollHeight;
      this._scrollTop = scrollTop;
      this._shouldScrollBottom = scrollTop + offsetHeight === scrollHeight;
    } else {
      this._shouldScrollBottom = true;
    }
  }

  componentDidUpdate() {
    if (this.node.scrollHeight > this._scrollHeight) {
      this.node.scrollTop = this._scrollTop + (this.node.scrollHeight - this._scrollHeight);
    } else if (this._shouldScrollBottom) {
      this.scrollToBottom();
    }
  }

  scrollToBottom() {
    this.node.scrollTop = this.node.scrollHeight;
  }

  onRef(node) {
    this.node = node;
  }

  onScroll({target}) {
    const { scrollTop, offsetHeight } = target;
    if (scrollTop < offsetHeight) {
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
