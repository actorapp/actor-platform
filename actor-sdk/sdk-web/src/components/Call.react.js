/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import classNames from 'classnames';
import { Container } from 'flux/utils';
import { FormattedMessage } from 'react-intl';

import { PeerTypes } from '../constants/ActorAppConstants';

import CallActionCreators from '../actions/CallActionCreators';

import CallStore from '../stores/CallStore';
import UserStore from '../stores/UserStore';
import GroupStore from '../stores/GroupStore';

import CallHeader from './call/CallHeader.react';
import CallBody from './call/CallBody.react';
import CallControls from './call/CallControls.react';
import ContactDetails from './common/ContactDetails.react';

class Call extends Component {
  static getStores = () => [CallStore];

  static calculatePeerInfo() {
    const peer = CallStore.getPeer();
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
    return {
      isOpen: CallStore.isOpen(),
      isOutgoing: CallStore.isOutgoing(),
      isMuted: CallStore.isMuted(),
      callId: CallStore.getId(),
      callMembers: CallStore.getMembers(),
      callPeer: CallStore.getPeer(),
      callState: CallStore.getState(),
      peerInfo: Call.calculatePeerInfo()
    };
  }

  constructor(props) {
    super(props);
    this.onAnswer = this.onAnswer.bind(this);
    this.onEnd = this.onEnd.bind(this);
    this.onMuteToggle = this.onMuteToggle.bind(this);
    this.onClose = this.onClose.bind(this);
  }

  onAnswer() {
    CallActionCreators.answerCall(this.state.callId);
  }

  onEnd() {
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
    const { peerInfo } = this.state;
    if (!peerInfo) return null

    return <ContactDetails peerInfo={peerInfo}/>
  }

  renderContent() {
    const {isOpen, callState, peerInfo, isOutgoing, isMuted} = this.state;
    if (!isOpen) {
      return null;
    }

    return (
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
        <div className="call__info">
          <div>{this.renderContactInfo()}</div>
        </div>
      </div>
    );
  }

  render() {
    const className = classNames('activity', {
      'activity--shown': this.state.isOpen
    });

    return (
      <section className={className}>
        {this.renderContent()}
      </section>
    );
  }
}

export default Container.create(Call);
