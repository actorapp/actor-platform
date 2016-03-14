/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import {shouldComponentUpdate} from 'react-addons-pure-render-mixin';
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

  constructor(props) {
    super(props);
    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
  }

  render() {
    const { tag, type, message } = this.props;

    const className = classNames('logger__container__row log-entry', {
      'log-entry--info': type === LoggerTypes.INFO,
      'log-entry--error': type === LoggerTypes.ERROR,
      'log-entry--warning': type === LoggerTypes.WARNING,
      'log-entry--debug': type === LoggerTypes.DEBUG
    });

    return (
      <div className={className}>
        <span className="log-entry__tag">{tag}</span>
        <span className="log-entry__message">{message}</span>
      </div>
    );
  }
}

export default LoggerRow;
