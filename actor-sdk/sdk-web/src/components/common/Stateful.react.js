/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { AsyncActionStates } from '../../constants/ActorAppConstants';

export default class Stateful extends Component {
  static propTypes = {
    className: PropTypes.string,
    currentState: PropTypes.oneOf([
      AsyncActionStates.PENDING,
      AsyncActionStates.PROCESSING,
      AsyncActionStates.SUCCESS,
      AsyncActionStates.FAILURE
    ]).isRequired,
    processing: PropTypes.oneOfType([
      PropTypes.element,
      PropTypes.node
    ]),
    pending: PropTypes.oneOfType([
      PropTypes.element,
      PropTypes.node
    ]),
    success: PropTypes.oneOfType([
      PropTypes.element,
      PropTypes.node
    ]),
    failure: PropTypes.oneOfType([
      PropTypes.element,
      PropTypes.node
    ])
  };

  shouldComponentUpdate(nextProps) {
    return this.props.currentState !== nextProps.currentState
  }

  renderState() {
    const { currentState, processing, pending, success, failure } = this.props;
    switch (currentState) {
      case AsyncActionStates.PENDING: return pending || null;
      case AsyncActionStates.PROCESSING: return processing || null;
      case AsyncActionStates.SUCCESS: return success || null;
      case AsyncActionStates.FAILURE: return failure || null;
      default: return null;
    }
  }

  render() {
    const { className } = this.props;

    return (
      <div className={className}>
        {this.renderState()}
      </div>
    )
  }
}
