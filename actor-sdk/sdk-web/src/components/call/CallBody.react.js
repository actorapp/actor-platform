/*
* Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
*/

import React, { Component, PropTypes } from 'react';
import classNames from 'classnames';

import CallAvatar from './CallAvatar.react';

class CallBody extends Component {
  static propTypes = {
    small: PropTypes.bool,
    peerInfo: PropTypes.object,
    callState: PropTypes.string.isRequired
  };

  render() {
    const { small, peerInfo, callState } = this.props;
    if (!peerInfo) {
      return null;
    }

    const titleClassName = classNames('call__title', {
      'call__title--small': small
    });

    return (
      <div className="call__body">
        <CallAvatar peerInfo={peerInfo} callState={callState} small={small} />
        <h3 className={titleClassName}>
          {peerInfo.name}
        </h3>
      </div>
    );
  }
}

export default CallBody;
