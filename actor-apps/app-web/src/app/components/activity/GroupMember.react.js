/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React from 'react';

import DialogActionCreators from 'actions/DialogActionCreators';
import GroupProfileActionCreators from 'actions/GroupProfileActionCreators';

import GroupStore from 'stores/GroupStore'

import AvatarItem from 'components/common/AvatarItem.react';
import * as Stateful from 'components/common/Stateful.react';

import { AsyncActionStates } from 'constants/ActorAppConstants';

const getStateFromStore = (gid, uid) => {
  return {
    kickUserState: GroupStore.getKickUserState(gid, uid)
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

    this.state = getStateFromStore(props.gid, props.peerInfo.peer.id);
  }

  componentWillUnmount() {
    const {peerInfo, gid} = this.props;

    GroupStore.resetKickUserState(gid, peerInfo.peer.id);
    GroupStore.removeChangeListener(this.onChange);
  };

  render() {
    const { peerInfo, canKick, gid } = this.props;

    let controls;
    if (canKick) {
      const { kickUserState } = this.state;
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
    const {peerInfo, gid} = this.props;
    this.setState(getStateFromStore(gid, peerInfo.peer.id));
  };

  onClick = (id) => DialogActionCreators.selectDialogPeerUser(id);

  onKick = (gid, uid) => {
    GroupProfileActionCreators.kickMember(gid, uid);
    GroupStore.addChangeListener(this.onChange);
    this.setState({kickUserState: AsyncActionStates.PROCESSING}); // Used for immediately set processing state
  };
}
