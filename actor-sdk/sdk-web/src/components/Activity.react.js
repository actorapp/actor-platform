/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import { PeerTypes } from '../constants/ActorAppConstants';

import ActivityStore from '../stores/ActivityStore';
import DialogStore from '../stores/DialogStore';
import DialogInfoStore from '../stores/DialogInfoStore';

import UserProfile from './activity/UserProfile.react';
import GroupProfile from './activity/GroupProfile.react';

class ActivitySection extends Component {
  constructor(props) {
    super(props);
  }

  static getStores = () => [DialogStore, DialogInfoStore, ActivityStore];

  static calculateState() {
    return {
      peer: DialogStore.getCurrentPeer(),
      info: DialogInfoStore.getInfo(),
      isOpen: ActivityStore.isOpen()
    };
  }

  render() {
    const { peer, info, isOpen } = this.state;

    if (peer !== null) {
      const activityClassName = classnames('activity', {
        'activity--shown': isOpen
      });
      let activityBody;

      switch (peer.type) {
        case PeerTypes.USER:
          activityBody = <UserProfile user={info}/>;
          break;
        case PeerTypes.GROUP:
          activityBody = <GroupProfile group={info}/>;
          break;
        default:
      }

      return (
        <section className={activityClassName}>{activityBody}</section>
      );
    } else {
      return null;
    }
  }
}

export default Container.create(ActivitySection, {pure: false});
