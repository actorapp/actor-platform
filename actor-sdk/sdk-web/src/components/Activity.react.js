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

  renderBody() {
    const { isOpen, peer, info } = this.state;
    if (!isOpen) {
      return null;
    }

    switch (peer.type) {
      case PeerTypes.USER:
        return <UserProfile user={info} />;
      case PeerTypes.GROUP:
        return <GroupProfile group={info}/>;
      default:
        return null;
    }
  }

  render() {
    const { peer, info, isOpen } = this.state;
    if (peer === null) {
      return null;
    }

    setImmediate(() => {
      window.dispatchEvent(new Event('resize'));
    });

    const activityClassName = classnames('activity', {
      'activity--shown': isOpen
    });

    return (
      <section className={activityClassName}>
        {this.renderBody()}
      </section>
    );
  }
}

export default Container.create(ActivitySection, {pure: false});
