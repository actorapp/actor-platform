/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';
import classNames from 'classnames';
import noop from 'lodash/noop';

const raf = window.requestAnimationFrame;

class CustomScroller extends Component {
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
      top: 0,
      height: 0,
      draging: false
    };

    this.onScroll = this.onScroll.bind(this);
    this.onResize = this.onResize.bind(this);
    this.onMouseDown = this.onMouseDown.bind(this);
    this.onMouseMove = this.onMouseMove.bind(this);
    this.onMouseUp = this.onMouseUp.bind(this);
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

  onMouseDown(event) {
    this.setState({ dragging: true });
    this.lastPageY = event.pageY;

    document.onselectstart = () => false;
    document.addEventListener('mousemove', this.onMouseMove);
    document.addEventListener('mouseup', this.onMouseUp);

    return false;
  }

  onMouseMove(event) {
    const delta = event.pageY - this.lastPageY;
    this.lastPageY = event.pageY;

    raf(() => this.container.scrollTop += delta / this.scrollRatio);
  }

  onMouseUp() {
    this.setState({ dragging: false });

    document.onselectstart = undefined;
    document.removeEventListener('mousemove', this.onMouseMove);
    document.removeEventListener('mouseup', this.onMouseUp);
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

  getThumbStyle() {
    const { scrollTop, scrollHeight, offsetHeight } = this.getDimensions();

    if (scrollHeight === 0 || scrollHeight <= offsetHeight) {
      return { top: 0, height: 0 };
    }

    const height = Math.max(30, (offsetHeight / scrollHeight) * offsetHeight);
    const offsetAvailable = scrollHeight - offsetHeight;
    const offsetPercent = offsetAvailable === 0 ? 0 : (scrollTop / offsetAvailable);
    const offset = (offsetHeight - height) * offsetPercent;

    return {
      top: offset,
      height: height
    };
  }

  render() {
    const { top, height, dragging } = this.state;

    const className = classNames('scroller__container', this.props.className);
    const scrollbarClassName = classNames('scroller__scrollbar', {
      'scroller__scrollbar--active': dragging
    });

    return (
      <div className="scroller__wrapper scroller__wrapper--custom">
        <div className={className} ref={this.onReference} onScroll={this.onScroll}>
          {this.props.children}
        </div>
        <div className={scrollbarClassName}>
          <div
            className="scroller__thumb"
            style={{ top, height }}
            onMouseDown={this.onMouseDown}
          />
        </div>
      </div>
    );
  }

  updateState(shouldUpdate, callback) {
    this.shouldUpdate = shouldUpdate;

    raf(() => {
      const { scrollHeight, clientHeight } = this.container;
      this.scrollRatio = clientHeight / scrollHeight;

      this.setState(this.getThumbStyle(), callback);
    });
  }

  scrollTo(offset) {
    this.container.scrollTop = offset;
  }

  scrollToBottom() {
    this.scrollTo(this.container.scrollHeight);
  }
}

export default CustomScroller;
