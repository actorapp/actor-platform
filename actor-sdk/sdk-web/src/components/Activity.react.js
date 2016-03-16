/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import shallowCompare from 'react-addons-shallow-compare';
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

  static getStores() {
    return [DialogStore, DialogInfoStore, ActivityStore];
  }

  static calculateState() {
    return {
      peer: DialogStore.getCurrentPeer(),
      info: DialogInfoStore.getInfo(),
      isOpen: ActivityStore.isOpen()
    };
  }

  shouldComponentUpdate(nextProps, nextState) {
    if (!nextState.isOpen) {
      return false;
    }

    return shallowCompare(this, nextProps, nextState);
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
        return <GroupProfile group={info}/>;
      default:
        return null;
    }
  }

  render() {
    const { peer, info, isOpen } = this.state;
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
