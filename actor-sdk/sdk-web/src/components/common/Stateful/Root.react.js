/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { find } from 'lodash';
import React, { Component, PropTypes } from 'react';
import { AsyncActionStates } from '../../../constants/ActorAppConstants';

import Pending from './Pending.react';
import Processing from './Processing.react';
import Success from './Success.react';
import Failure from './Failure.react';

export default class Root extends Component {
  constructor(props) {
    super(props);
  }

  static propTypes = {
    className: PropTypes.string,
    currentState: PropTypes.number.isRequired,
    children: PropTypes.array
  };

  render() {
    const { currentState, className, children } = this.props;

    const equalsState = (state, type) => {
      return (state === AsyncActionStates.PENDING && type === Pending) ||
        (state === AsyncActionStates.PROCESSING && type === Processing) ||
        (state === AsyncActionStates.SUCCESS && type === Success) ||
        (state === AsyncActionStates.FAILURE && type === Failure)
    };

    const currentStateChild = find(children, (child) => {
      if (equalsState(currentState, child.type)) return child;
    });

    return (
      <div className={className}>{currentStateChild}</div>
    )
  }
}
