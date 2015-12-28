/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';

import classnames from 'classnames';

import ConnectionStateStore from '../../stores/ConnectionStateStore';

class ConnectionState extends Component {
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

    return (
      <div className={className}>{this.getIntlMessage(`connectionState.${connectionState}`)}</div>
    )
  }
}

ReactMixin.onClass(ConnectionState, IntlMixin);

export default Container.create(ConnectionState, {pure: false});
