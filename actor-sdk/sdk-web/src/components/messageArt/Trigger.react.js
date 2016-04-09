/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classnames from 'classnames';

class Trigger extends Component {
  static propTypes = {
    className: PropTypes.string,
    children: PropTypes.node,
    isActive: PropTypes.bool.isRequired,
    isDotVisible: PropTypes.bool.isRequired
  }

  render() {
    const { className, children, isActive, isDotVisible, ...props } = this.props;

    const triggerClassName = classnames('message-art__trigger', className, {
      'message-art__trigger--active': isActive,
      'message-art__trigger--with-dot': isDotVisible
    });

    return (
      <div {...props} className={triggerClassName}>
        {children}
      </div>
    )
  }
}

export default Trigger;
