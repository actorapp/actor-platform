/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React from 'react';

import confirm from 'utils/confirm'
import { escapeWithEmoji } from 'utils/EmojiUtils'
import ActorClient from 'utils/ActorClient'

import { AsyncActionStates } from 'constants/ActorAppConstants';

import DialogActionCreators from 'actions/DialogActionCreators';
import KickUserActionCreators from 'actions/KickUserActionCreators';

import KickUserStore from 'stores/KickUserStore'

import AvatarItem from 'components/common/AvatarItem.react';
import * as Stateful from 'components/common/Stateful.react';

const getStateFromStore = (uid) => {
  const kickUserState = KickUserStore.getKickUserState(uid);

  return {
    kickUserState: kickUserState
  }
};

export default class GroupMember extends React.Component {
  static propTypes = {
    peerInfo: React.PropTypes.object.isRequired,
    canKick: React.PropTypes.bool.isRequired,
    gid: React.PropTypes.number.isRequired
  };

  constructor(props) {
    super(props);

    this.state = getStateFromStore(props.peerInfo.peer.id);
  }

  componentWillUnmount() {
    const { peerInfo } = this.props;

    KickUserStore.resetKickUserState(peerInfo.peer.id);
    KickUserStore.removeChangeListener(this.onChange);
  };

  render() {
    const { peerInfo, canKick, gid } = this.props;
    const { kickUserState } = this.state;
    const myId = ActorClient.getUid();

    let controls;
    if (canKick && peerInfo.peer.id !== myId) {
      controls = (
        <div className="controls pull-right">
          <Stateful.Root currentState={kickUserState}>
            <Stateful.Pending>
              <a onClick={() => this.onKick(gid, peerInfo.peer.id)}>Kick</a>
            </Stateful.Pending>
            <Stateful.Processing>
              <i className="material-icons spin">autorenew</i>
            </Stateful.Processing>
            <Stateful.Success>
              <i className="material-icons">check</i>
            </Stateful.Success>
            <Stateful.Failure>
              <i className="material-icons">warning</i>
            </Stateful.Failure>
          </Stateful.Root>
        </div>
      );
    } else {
      controls = null;
    }

    return (
      <li className="group_profile__members__list__item">
        <a onClick={() => this.onClick(peerInfo.peer.id)}>
          <AvatarItem image={peerInfo.avatar}
                      placeholder={peerInfo.placeholder}
                      title={peerInfo.title}/>
        </a>

        <a onClick={() => this.onClick(peerInfo.peer.id)}
           dangerouslySetInnerHTML={{__html: escapeWithEmoji(peerInfo.title)}}/>

        {controls}
      </li>
    )
  }

  onChange = () => {
    const { peerInfo } = this.props;
    this.setState(getStateFromStore(peerInfo.peer.id));
  };

  onClick = (id) => DialogActionCreators.selectDialogPeerUser(id);

  onKick = (gid, uid) => {
    const { peerInfo } = this.props;
    const confirmText = 'Are you sure you want kick ' + peerInfo.title;

    confirm(confirmText).then(
      () => {
        KickUserStore.addChangeListener(this.onChange);
        KickUserActionCreators.kickMember(gid, uid);
      }
    );
  };
}
