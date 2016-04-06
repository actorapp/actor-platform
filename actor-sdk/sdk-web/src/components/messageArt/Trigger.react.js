/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classnames from 'classnames';

class Trigger extends Component {
  constructor(props) {
    super(props);

    this.handleMouseEnter = this.handleMouseEnter.bind(this);
    this.handleMouseLeave = this.handleMouseLeave.bind(this);
    this.handleClick = this.handleClick.bind(this);
  }

  static propTypes = {
    isActive: PropTypes.bool.isRequired,
    isDotVisible: PropTypes.bool.isRequired,
    className: PropTypes.string,
    onMouseEnter: PropTypes.func,
    onMouseLeave: PropTypes.func,
    onClick: PropTypes.func,
    children: PropTypes.node
  }

  handleMouseEnter(event) {
    // console.debug('handleMouseEnter(event)');
    const { onMouseEnter } = this.props;
    onMouseEnter && onMouseEnter(event);
  }

  handleMouseLeave(event) {
    // console.debug('handleMouseLeave(event)');
    const { onMouseLeave } = this.props;
    onMouseLeave && onMouseLeave(event);
  }

  handleClick(event) {
    // console.debug('handleClick(event)');
    const { onClick } = this.props;
    onClick && onClick(event);
  }

  render() {
    const { isActive, isDotVisible } = this.props;

    const triggerClassName = classnames('message-art__trigger', {
      'message-art__trigger--active': isActive,
      'message-art__trigger--with-dot': isDotVisible
    });

    return (
      <div
        className={triggerClassName}
        onMouseEnter={this.handleMouseEnter}
        onMouseLeave={this.handleMouseLeave}
        onClick={this.handleClick}
      >
        {this.props.children}
      </div>
    )
  }
}

export default Trigger;
