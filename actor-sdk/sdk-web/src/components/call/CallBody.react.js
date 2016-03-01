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
      <div>
        <AvatarItem
          size="big"
          image={peerInfo.avatar}
          title={peerInfo.name}
          placeholder={peerInfo.placeholder}
        />
        <h4 className="caller-name">{peerInfo.name}</h4>
      </div>
    );
  }
}

export default CallBody;
