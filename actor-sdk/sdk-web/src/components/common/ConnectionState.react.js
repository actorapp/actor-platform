/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';

import ConnectionStateStore from '../../stores/ConnectionStateStore';

class ConnectionState extends Component {
  constructor(props) {
    super(props);
  }

  static contextTypes = {
    intl: PropTypes.object
  };

  static getStores = () => [ConnectionStateStore];

  static calculateState() {
    return {
      connectionState: ConnectionStateStore.getState()
    };
  }

  render() {
    const { connectionState } = this.state;
    const { intl } = this.context;

    const className = classnames('connection-state', {
      'connection-state--online': connectionState === 'online',
      'connection-state--connection': connectionState === 'connecting'
    });

    return (
      <div className={className}>{intl.messages[`connectionState.${connectionState}`]}</div>
    )
  }
}

export default Container.create(ConnectionState, {pure: false});
