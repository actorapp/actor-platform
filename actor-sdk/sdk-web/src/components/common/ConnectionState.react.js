/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';

import ConnectionStateStore from '../../stores/ConnectionStateStore';

class ConnectionState extends React.Component {
  constructor(props) {
    super(props);
  }

  static getStores = () => [ConnectionStateStore];

  static calculateState() {
    return {
      connectionState: ConnectionStateStore.getState()
    };
  }

  render() {
    const { connectionState } = this.state;

    const className = classnames('connection-state', {
      'connection-state--online': connectionState === 'online',
      'connection-state--connection': connectionState === 'connecting'
    });

    switch (connectionState) {
      case 'online':
        return (
          <div className={className}>You're back online!</div>
        );
      case 'connecting':
        return (
          <div className={className}>
            Houston, we have a problem! Connection to Actor server is lost. Trying to reconnect now...
          </div>
        );
      default:
        return null;
    }
  }
}

export default Container.create(ConnectionState, {pure: false});
