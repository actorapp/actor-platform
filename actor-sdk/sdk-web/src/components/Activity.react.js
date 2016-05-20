/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';
import { Container } from 'flux/utils';
import { PeerTypes } from '../constants/ActorAppConstants';

import ActivityStore from '../stores/ActivityStore';
import DialogStore from '../stores/DialogStore';
import DialogInfoStore from '../stores/DialogInfoStore';

import UserProfile from './activity/UserProfile.react';
import GroupProfile from './activity/GroupProfile.react';

class ActivitySection extends Component {
  static getStores() {
    return [DialogStore, DialogInfoStore, ActivityStore];
  }

  static calculateState() {
    return {
      peer: DialogStore.getCurrentPeer(),
      info: DialogInfoStore.getState(),
      isOpen: ActivityStore.isOpen()
    };
  }

  constructor(props) {
    super(props);

    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
  }

  componentDidUpdate() {
    setImmediate(() => {
      window.dispatchEvent(new Event('resize'));
    });
  }

  renderBody() {
    const { peer, info } = this.state;

    switch (peer.type) {
      case PeerTypes.USER:
        return <UserProfile user={info} />;
      case PeerTypes.GROUP:
        return <GroupProfile group={info} />;
      default:
        return null;
    }
  }

  render() {
    const { peer, isOpen } = this.state;
    if (!isOpen || !peer) {
      return <section className="activity" />;
    }

    return (
      <section className="activity activity--shown">
        {this.renderBody()}
      </section>
    );
  }
}

export default Container.create(ActivitySection);
