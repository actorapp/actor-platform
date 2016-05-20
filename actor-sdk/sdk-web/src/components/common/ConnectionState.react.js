/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import { FormattedMessage } from 'react-intl';
import SharedContainer from '../../utils/SharedContainer';
import { appName, ConnectionStates } from '../../constants/ActorAppConstants';
import classnames from 'classnames';

import ConnectionStateStore from '../../stores/ConnectionStateStore';

class ConnectionState extends Component {
  constructor(props) {
    super(props);

    const SharedActor = SharedContainer.get();
    this.appName = SharedActor.appName ? SharedActor.appName : appName;
  }

  static getStores() {
    return [ConnectionStateStore];
  }

  static calculateState() {
    return {
      connectionState: ConnectionStateStore.getState()
    };
  }

  render() {
    const { connectionState } = this.state;

    const className = classnames('connection-state', {
      'connection-state--online': connectionState === ConnectionStates.ONLINE,
      'connection-state--connection': connectionState === ConnectionStates.CONNECTING
    });

    return (
      <div className={className}>
        {
          connectionState !== ConnectionStates.UPDATING
            ? <FormattedMessage id={`connectionState.${connectionState}`} values={{ appName: this.appName }}/>
            : null
        }
      </div>
    )
  }
}

export default Container.create(ConnectionState, { pure: false });
