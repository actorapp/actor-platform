/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import _ from 'lodash';
import React from 'react';
import { AsyncActionStates } from 'constants/ActorAppConstants';

export class Root extends React.Component {
  static propTypes = {
    className: React.PropTypes.string,
    currentState: React.PropTypes.number.isRequired,
    children: React.PropTypes.array
  };

  constructor(props) {
    super(props);
  }

  render() {
    const { currentState, className, children } = this.props;

    const equalsState = (state, type) => {
      return (state === AsyncActionStates.PENDING && type === Pending) ||
             (state === AsyncActionStates.PROCESSING && type === Processing) ||
             (state === AsyncActionStates.SUCCESS && type === Success) ||
             (state === AsyncActionStates.FAILURE && type === Failure)
    };

    const currentStateChild = _.find(children, (child) => {
      if (equalsState(currentState, child.type)) return child;
    });

    return (
      <div className={className}>{currentStateChild}</div>
    )
  }
}

export class Pending extends React.Component {
  static propTypes = {
    children: React.PropTypes.node
  };

  render() {
    return this.props.children;
  }
}

export class Processing extends React.Component {
  static propTypes = {
    children: React.PropTypes.node
  };

  render() {
    return this.props.children;
  }
}

export class Success extends React.Component {
  static propTypes = {
    children: React.PropTypes.node
  };

  render() {
    return this.props.children;
  }
}

export class Failure extends React.Component {
  static propTypes = {
    children: React.PropTypes.node
  };

  render() {
    return this.props.children;
  }
}
