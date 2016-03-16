/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import shallowCompare from 'react-addons-shallow-compare';
import classNames from 'classnames';
import { Container } from 'flux/utils';
import { FormattedMessage } from 'react-intl';

import { PeerTypes } from '../constants/ActorAppConstants';
import PeerUtils from '../utils/PeerUtils';

import CallActionCreators from '../actions/CallActionCreators';

import CallStore from '../stores/CallStore';
import DialogStore from '../stores/DialogStore';
import UserStore from '../stores/UserStore';
import GroupStore from '../stores/GroupStore';

import CallDraggable from './call/CallDraggable.react';
import CallHeader from './call/CallHeader.react';
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
    const dialogPeer = DialogStore.getCurrentPeer();
    const callPeer = CallStore.getPeer();

    return {
      isOpen: CallStore.isOpen(),
      isOutgoing: CallStore.isOutgoing(),
      isMuted: CallStore.isMuted(),
      callId: CallStore.getId(),
      callMembers: CallStore.getMembers(),
      callPeer: CallStore.getPeer(),
      callState: CallStore.getState(),
      peerInfo: Call.calculatePeerInfo(callPeer),
      isSameDialog: PeerUtils.equals(dialogPeer, callPeer),
      isFloating: CallStore.isFloating()
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
  }

  shouldComponentUpdate(nextProps, nextState) {
    if (!nextState.isOpen) {
      return false;
    }

    return shallowCompare(this, nextProps, nextState);
  }

  onAnswer() {
    CallActionCreators.answerCall(this.state.callId);
  }

  onEnd() {
    console.log(this.state.callId);
    CallActionCreators.endCall(this.state.callId);
  }

  onMuteToggle() {
    CallActionCreators.toggleCallMute(this.state.callId);
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
    const { callPeer, peerInfo } = this.state;
    if (!peerInfo || callPeer.type === PeerTypes.GROUP) return null;

    return (
      <section className="call__info">
        <ContactDetails peerInfo={peerInfo}/>
      </section>
    )
  }

  render() {
    const {isOpen, callState, peerInfo, isOutgoing, isMuted, isSameDialog, isFloating} = this.state;
    if (!isOpen) {
      return <section className="activity" />;
    }

    if (!isSameDialog || isFloating) {
      return (
        <CallDraggable
          peerInfo={peerInfo}
          callState={callState}
          isOutgoing={isOutgoing}
          isMuted={isMuted}
          onEnd={this.onEnd}
          onAnswer={this.onAnswer}
          onMuteToggle={this.onMuteToggle}
          onFullscreen={this.onFullscreen}
          onUserAdd={this.onUserAdd}
          onVideo={this.onVideo}
          onClose={this.onClose}
        />
      );
    }

    return (
      <section className="activity activity--shown">
        <div className="activity__body call">
          <section className="call__container">
            <CallBody peerInfo={peerInfo} callState={callState}/>
            <CallControls
              callState={callState}
              isOutgoing={isOutgoing}
              isMuted={isMuted}
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
