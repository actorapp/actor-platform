/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';
import classNames from 'classnames';
import noop from 'lodash/noop';

class Scroller extends Component {
  static propTypes = {
    className: PropTypes.string,
    children: PropTypes.node.isRequired,
    onUpdate: PropTypes.func.isRequired,
    onScroll: PropTypes.func.isRequired,
    onResize: PropTypes.func.isRequired
  };

  static defaultProps = {
    onUpdate: noop,
    onScroll: noop,
    onResize: noop
  };

  constructor(props) {
    super(props);

    this.state = {
      scrollTop: 0,
      scrollHeight: 0,
      offsetHeight: 0
    };

    this.onScroll = this.onScroll.bind(this);
    this.onResize = this.onResize.bind(this);
    this.onReference = this.onReference.bind(this);
    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
  }

  componentDidMount() {
    this.updateState(true, this.props.onUpdate);
    window.addEventListener('resize', this.onResize, false);
  }

  componentWillUnmount() {
    window.removeEventListener('resize', this.onResize, false);
  }

  componentDidUpdate() {
    if (this.shouldUpdate) {
      this.updateState(true, this.props.onUpdate);
    }
  }

  onReference(node) {
    this.container = node;
  }

  onScroll() {
    this.updateState(false, this.props.onScroll);
  }

  onResize() {
    this.updateState(false, this.props.onResize);
  }

  getThumbStyle() {
    const { scrollTop, scrollHeight, offsetHeight } = this.state;

    if (scrollHeight === 0 || scrollHeight <= offsetHeight) {
      return { top: 0, height: 0 };
    }

    const height = (offsetHeight / scrollHeight) * offsetHeight;
    const offsetAvailable = scrollHeight - offsetHeight;
    const offsetPercent = offsetAvailable === 0 ? 0 : (scrollTop / offsetAvailable);
    const offset = (offsetHeight - height) * offsetPercent;

    return {
      top: offset,
      height: height
    };
  }

  render() {
    const className = classNames('scroller__container', this.props.className);

    return (
      <div className="scroller__wrapper">
        <div className={className} ref={this.onReference} onScroll={this.onScroll}>
          {this.props.children}
        </div>
        <div className="scroller__scrollbar">
          <div
            className="scroller__thumb"
            style={this.getThumbStyle()}
          />
        </div>
      </div>
    );
  }

  updateState(shouldUpdate, callback) {
    this.shouldUpdate = shouldUpdate;

    window.requestAnimationFrame(() => {
      this.setState(this.getDimensions(), callback);
    });
  }

  scrollTo(offset) {
    this.container.scrollTop = offset;
  }

  scrollToBottom() {
    this.scrollTo(this.container.scrollHeight);
  }

  getDimensions() {
    return {
      scrollTop: this.container.scrollTop,
      scrollHeight: this.container.scrollHeight,
      offsetHeight: this.container.offsetHeight
    };
  }

  getBoundingClientRect() {
    return this.container.getBoundingClientRect();
  }
}

export default Scroller;
