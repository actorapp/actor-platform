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
    onScroll: noop
  };

  constructor(props) {
    super(props);

    this.onReference = this.onReference.bind(this);
    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
  }

  componentDidMount() {
    this.props.onUpdate();
    window.addEventListener('resize', this.props.onResize, false);
  }

  componentDidUpdate() {
    this.props.onUpdate();
  }

  componentWillUnmount() {
    window.removeEventListener('resize', this.props.onResize, false);
  }

  onReference(node) {
    this.container = node;
  }

  render() {
    const className = classNames('scroller__container', this.props.className);

    return (
      <div className="scroller__wrapper">
        <div className={className} ref={this.onReference} onScroll={this.props.onScroll}>
          {this.props.children}
        </div>
      </div>
    );
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
