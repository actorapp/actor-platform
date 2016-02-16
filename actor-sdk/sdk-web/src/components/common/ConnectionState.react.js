/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import { FormattedMessage } from 'react-intl';
import SharedContainer from '../../utils/SharedContainer';
import { appName } from '../../constants/ActorAppConstants';
import classnames from 'classnames';

import ConnectionStateStore from '../../stores/ConnectionStateStore';

class ConnectionState extends Component {
  constructor(props) {
    super(props);

    const SharedActor = SharedContainer.get();
    this.appName = SharedActor.appName ? SharedActor.appName : appName;
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
      <div className={className}>
        <FormattedMessage id={`connectionState.${connectionState}`} values={{appName: this.appName}}/>
      </div>
    )
  }
}

export default Container.create(ConnectionState, {pure: false});
