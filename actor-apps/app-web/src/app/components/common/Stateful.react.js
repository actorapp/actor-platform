/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

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

    const currentStateChild = React.Children.map(children, (child) => {
      if (currentState === AsyncActionStates.PENDING && child.type === Pending) return child;
      if (currentState === AsyncActionStates.PROCESSING && child.type === Processing) return child;
      if (currentState === AsyncActionStates.SUCCESS && child.type === Success) return child;
      if (currentState === AsyncActionStates.FAILURE && child.type === Failure) return child;
    });

    return (
      <div className={className}>{currentStateChild}</div>
    );
  }
}

export class Pending extends React.Component {
  static propTypes = {
    children: React.PropTypes.array
  };

  render() {
    return this.props.children;
  }
}

export class Processing extends React.Component {
  static propTypes = {
    children: React.PropTypes.array
  };

  render() {
    return this.props.children;
  }
}

export class Success extends React.Component {
  static propTypes = {
    children: React.PropTypes.array
  };

  render() {
    return this.props.children;
  }
}

export class Failure extends React.Component {
  static propTypes = {
    children: React.PropTypes.array
  };

  render() {
    return this.props.children;
  }
}
