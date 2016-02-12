/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';
import confirm from '../../utils/confirm'
import { escapeWithEmoji } from '../../utils/EmojiUtils'
import ActorClient from '../../utils/ActorClient'

import DialogActionCreators from '../../actions/DialogActionCreators';
import KickUserActionCreators from '../../actions/KickUserActionCreators';

import KickUserStore from '../../stores/KickUserStore'

import AvatarItem from '../common/AvatarItem.react';
import Stateful from '../common/Stateful';

const getStateFromStore = (uid) => {
  const kickUserState = KickUserStore.getKickUserState(uid);

  return {
    kickUserState: kickUserState
  }
};

class GroupMember extends Component {
  static propTypes = {
    peerInfo: PropTypes.object.isRequired,
    canKick: PropTypes.bool.isRequired,
    gid: PropTypes.number.isRequired
  };

  static contextTypes = {
    intl: PropTypes.object
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

  onChange = () => {
    const { peerInfo } = this.props;
    this.setState(getStateFromStore(peerInfo.peer.id));
  };

  onClick = (id) => DialogActionCreators.selectDialogPeerUser(id);

  onKick = (gid, uid) => {
    const { peerInfo } = this.props;
    const { intl } = this.context;

    confirm(<FormattedMessage id="modal.confirm.kick" values={{name: peerInfo.title}}/>, {
      abortLabel: intl.messages['button.cancel'],
      confirmLabel: intl.messages['button.ok']
    }).then(
      () => {
        KickUserStore.addChangeListener(this.onChange);
        KickUserActionCreators.kickMember(gid, uid);
      },
      () => {}
    );
  };

  render() {
    const { peerInfo, canKick, gid } = this.props;
    const { kickUserState } = this.state;
    const { intl } = this.context;
    const myId = ActorClient.getUid();

    let controls;
    if (canKick && peerInfo.peer.id !== myId) {
      controls = (
        <div className="controls pull-right">
          <Stateful.Root currentState={kickUserState}>
            <Stateful.Pending>
              <a onClick={() => this.onKick(gid, peerInfo.peer.id)}>{intl.messages['kick']}</a>
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
}

export default GroupMember;
