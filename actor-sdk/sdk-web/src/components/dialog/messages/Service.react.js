/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

/**
 * Class that represents a component for display service message content
 * @param {String} text Service message text
 * @param {String} className Component class name
 */
class Service extends Component {
  static propTypes = {
    text: PropTypes.string.isRequired,
    className: PropTypes.string
  };

  constructor(props) {
    super(props);
  }

  render() {
    const { text, className } = this.props;

    return (
      <div className={className}>
        <div className="service">{text}</div>
      </div>
    );
  }
}

export default Service;
