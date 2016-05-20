/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';
import { Container } from 'flux/utils';

import { PeerTypes } from '../constants/ActorAppConstants';
import PeerUtils from '../utils/PeerUtils';

import CallActionCreators from '../actions/CallActionCreators';

import CallStore from '../stores/CallStore';
import DialogStore from '../stores/DialogStore';
import UserStore from '../stores/UserStore';
import GroupStore from '../stores/GroupStore';

import CallBody from './call/CallBody.react';
import CallControls from './call/CallControls.react';
import ContactDetails from './common/ContactDetails.react';

class Call extends Component {
  static getStores() {
    return [CallStore, DialogStore];
  }

  static calculatePeerInfo(peer) {
    if (peer) {
      if (peer.type === PeerTypes.USER) {
        return UserStore.getUser(peer.id);
      }

      if (peer.type === PeerTypes.GROUP) {
        return GroupStore.getGroup(peer.id);
      }
    }

    return null;
  }

  static calculateState() {
    const call = CallStore.getState();
    if (!call.isOpen || call.isFloating) {
      return { isOpen: false };
    }

    const dialogPeer = DialogStore.getCurrentPeer();
    const isSameDialog = PeerUtils.equals(dialogPeer, call.peer);
    if (!isSameDialog) {
      return { isOpen: false };
    }

    return {
      call,
      isOpen: true,
      peerInfo: Call.calculatePeerInfo(call.peer)
    };
  }

  constructor(props) {
    super(props);

    this.onAnswer = this.onAnswer.bind(this);
    this.onEnd = this.onEnd.bind(this);
    this.onMuteToggle = this.onMuteToggle.bind(this);
    this.onClose = this.onClose.bind(this);
    this.onFullscreen = this.onFullscreen.bind(this);
    this.onUserAdd = this.onUserAdd.bind(this);
    this.onVideo = this.onVideo.bind(this);
    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
  }

  onAnswer() {
    CallActionCreators.answerCall(this.state.call.id);
  }

  onEnd() {
    CallActionCreators.endCall(this.state.call.id);
  }

  onMuteToggle() {
    CallActionCreators.toggleCallMute(this.state.call.id);
  }

  onClose() {
    CallActionCreators.hide();
  }

  onFullscreen() {
    console.debug('onFullscreen');
  }

  onUserAdd() {
    console.debug('onUserAdd');
  }

  onVideo() {
    console.debug('onVideo');
  }

  renderContactInfo() {
    const { call, peerInfo } = this.state;
    if (!peerInfo || call.peer.type === PeerTypes.GROUP) return null;

    return (
      <section className="call__info">
        <ContactDetails peerInfo={peerInfo}/>
      </section>
    )
  }

  render() {
    const { isOpen, call, peerInfo } = this.state;
    if (!isOpen) {
      return <section className="activity" />;
    }

    return (
      <section className="activity activity--shown">
        <div className="activity__body call">
          <section className="call__container">
            <CallBody peerInfo={peerInfo} callState={call.state}/>
            <CallControls
              callState={call.state}
              isOutgoing={call.isOutgoing}
              isMuted={call.isMuted}
              onEnd={this.onEnd}
              onAnswer={this.onAnswer}
              onMuteToggle={this.onMuteToggle}
              onFullscreen={this.onFullscreen}
              onUserAdd={this.onUserAdd}
              onVideo={this.onVideo}
              onClose={this.onClose}
            />
          </section>
          {this.renderContactInfo()}
        </div>
      </section>
    );
  }
}

export default Container.create(Call);
