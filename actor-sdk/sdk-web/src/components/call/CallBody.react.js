/*
* Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
*/

import React, {Component, PropTypes} from 'react';
import { FormattedMessage } from 'react-intl';

import AvatarItem from '../common/AvatarItem.react';

class CallBody extends Component {
  static propTypes = {
    peerInfo: React.PropTypes.object
  };

  render() {
    const {peerInfo} = this.props;
    if (!peerInfo) {
      return null;
    }

    return (
      <div className="call__body">
        <div className="call__avatar">
          <AvatarItem
            size="big"
            image={peerInfo.avatar}
            title={peerInfo.name}
            placeholder={peerInfo.placeholder}
          />
          <div className="call__avatar__rings">
            <div/><div/><div/>
          </div>
        </div>
        <h3 className="call__title">{peerInfo.name}</h3>
      </div>
    );
  }
}

export default CallBody;
