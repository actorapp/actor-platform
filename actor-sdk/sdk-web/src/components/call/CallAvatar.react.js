/*
* Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
*/

import React, { Component, PropTypes } from 'react';
import classNames from 'classnames';

import { CallStates } from '../../constants/ActorAppConstants';

import AvatarItem from '../common/AvatarItem.react';

class CallAvatar extends Component {
  static propTypes = {
    small: PropTypes.bool,
    peerInfo: PropTypes.shape({
      name: PropTypes.string.isRequired,
      avatar: PropTypes.string,
      placeholder: PropTypes.string.isRequired
    }).isRequired,
    callState: PropTypes.oneOf([
      CallStates.CALLING,
      CallStates.IN_PROGRESS,
      CallStates.CONNECTING,
      CallStates.ENDED
    ]).isRequired
  };

  renderAnimation() {
    const { callState, small } = this.props;

    if (callState !== CallStates.CALLING && callState !== CallStates.CONNECTING) {
      return null;
    }

    const className = classNames('call__avatar__rings', {
      'call__avatar__rings--small': small
    });

    return (
      <div className={className}>
        <div/><div/><div/>
      </div>
    );
  }

  render() {
    const { peerInfo, small } = this.props;

    return (
      <div className="call__avatar__container">
        <AvatarItem
          className="call__avatar"
          size={small ? 'large' : 'big'}
          title={peerInfo.name}
          image={peerInfo.avatar}
          placeholder={peerInfo.placeholder}
        />
        {this.renderAnimation()}
      </div>
    );
  }
}

export default CallAvatar;
