/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import { FormattedMessage } from 'react-intl';
import confirm from '../../utils/confirm'
import { escapeWithEmoji } from '../../utils/EmojiUtils'
import ActorClient from '../../utils/ActorClient'

import DialogActionCreators from '../../actions/DialogActionCreators';
import KickUserActionCreators from '../../actions/KickUserActionCreators';

import KickUserStore from '../../stores/KickUserStore'

import AvatarItem from '../common/AvatarItem.react';
import Stateful from '../common/Stateful.react';

class GroupMember extends Component {
  constructor(props) {
    super(props);
  }

  static propTypes = {
    peerInfo: PropTypes.object.isRequired,
    canKick: PropTypes.bool.isRequired,
    gid: PropTypes.number.isRequired
  };

  static getStores() {
    return [KickUserStore];
  }

  static calculateState(prevState, nextProps) {
    return {
      kickUserState: KickUserStore.getKickUserState(nextProps.peerInfo.peer.id)
    };
  }

  onClick = (id) => DialogActionCreators.selectDialogPeerUser(id);

  onKick = (gid, uid) => {
    const { peerInfo } = this.props;

    confirm(<FormattedMessage id="modal.confirm.kick" values={{ name: peerInfo.title }}/>).then(
      () => KickUserActionCreators.kickMember(gid, uid),
      () => {}
    );
  };

  renderControls() {
    const { peerInfo, canKick, gid } = this.props;
    const { kickUserState } = this.state;
    const myId = ActorClient.getUid();

    if (!canKick || peerInfo.peer.id === myId) return <div/>;

    return (
      <Stateful
        currentState={kickUserState}
        pending={<a onClick={() => this.onKick(gid, peerInfo.peer.id)}><FormattedMessage id="kick"/></a>}
        processing={<i className="material-icons spin">autorenew</i>}
        success={<i className="material-icons">check</i>}
        failure={<i className="material-icons">warning</i>}
      />
    );
  }


  render() {
    const { peerInfo } = this.props;

    return (
      <li className="group_profile__members__list__item">
        <AvatarItem
          className="group_profile__avatar"
          image={peerInfo.avatar}
          placeholder={peerInfo.placeholder}
          title={peerInfo.title}
          onClick={() => this.onClick(peerInfo.peer.id)}
        />

        <a onClick={() => this.onClick(peerInfo.peer.id)}
           dangerouslySetInnerHTML={{ __html: escapeWithEmoji(peerInfo.title) }}/>

        <div className="controls pull-right">
          {this.renderControls()}
        </div>
      </li>
    )
  }
}

export default Container.create(GroupMember, { pure: false, withProps: true });
