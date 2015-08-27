/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React from 'react';

import DialogActionCreators from 'actions/DialogActionCreators';
import KickUserActionCreators from 'actions/KickUserActionCreators';

import KickUserStore from 'stores/KickUserStore'

import AvatarItem from 'components/common/AvatarItem.react';
import * as Stateful from 'components/common/Stateful.react';

import { AsyncActionStates } from 'constants/ActorAppConstants';

const getStateFromStore = (uid) => {
  const kickUserState = KickUserStore.getKickUserState(uid);
  //console.debug('getStateFromStore kickUserState', uid, kickUserState);

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

    KickUserStore.addChangeListener(this.onChange);
  }

  componentWillUnmount() {
    const { peerInfo } = this.props;

    KickUserStore.resetKickUserState(peerInfo.peer.id);
    KickUserStore.removeChangeListener(this.onChange);
  };

  render() {
    const { peerInfo, canKick, gid } = this.props;
    const { kickUserState } = this.state;
    //console.debug('render kickUserState', peerInfo.peer.id, kickUserState);

    let controls;
    if (canKick) {
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

        <a onClick={() => this.onClick(peerInfo.peer.id)}>
          {peerInfo.title}
        </a>

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
    KickUserActionCreators.kickMember(gid, uid);
    //this.setState({kickUserState: AsyncActionStates.PROCESSING}); // Used for immediately set processing state
  };
}
