/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classNames from 'classnames';

import { LoggerTypes } from '../../constants/ActorAppConstants';

class LoggerRow extends Component {
  static propTypes = {
    tag: PropTypes.string.isRequired,
    type: PropTypes.oneOf([
      LoggerTypes.INFO,
      LoggerTypes.ERROR,
      LoggerTypes.WARNING,
      LoggerTypes.DEBUG
    ]).isRequired,
    message: PropTypes.string.isRequired
  };

  render() {
    const { tag, type, message } = this.props;

    const className = classNames('logger__row', {
      'logger__row--info': type === LoggerTypes.INFO,
      'logger__row--error': type === LoggerTypes.ERROR,
      'logger__row--warning': type === LoggerTypes.WARNING,
      'logger__row--debug': type === LoggerTypes.DEBUG
    });

    return (
      <div className={className}>
        <span className="logger__row__tag">{tag}</span>
        <span className="logger__row__message">{message}</span>
      </div>
    );
  }
}

export default LoggerRow;
