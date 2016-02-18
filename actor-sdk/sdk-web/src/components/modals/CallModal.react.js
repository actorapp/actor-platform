/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import Modal from 'react-modal';
// import { FormattedMessage } from 'react-intl';
import AvatarItem from '../common/AvatarItem.react';

import { KeyCodes, CallStates } from '../../constants/ActorAppConstants';

import CallActionCreators from '../../actions/CallActionCreators';

import CallStore from '../../stores/CallStore';
import UserStore from '../../stores/UserStore';

class CallModal extends Component {
  constructor(props) {
    super(props);
  }

  static getStores = () => [CallStore];

  static calculateState() {
    return {
      isOpen: CallStore.isOpen(),
      isOutgoing: CallStore.isOutgoing(),
      callId: CallStore.getId(),
      callMembers: CallStore.getMembers(),
      callPeer: CallStore.getPeer(),
      callState: CallStore.getState()
    };
  }

  handleClose = () => CallActionCreators.hide();

  onKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.handleClose();
    }
  };

  handleAnswer = () => {
    const { callId } = this.state;
    CallActionCreators.answerCall(callId);
  };

  handleEnd = () => {
    const { callId } = this.state;
    CallActionCreators.endCall(callId);
  };

  handleMute = () => {
    console.debug('handleMute')
  };

  render() {
    const { isOpen, isOutgoing, callPeer, callMembers, callState } = this.state;
    console.debug(this.state);

    if (isOpen) {
      const modalStyles = {
        content : {
          position: null,
          top: null,
          left: null,
          right: null,
          bottom: null,
          border: null,
          background: null,
          overflow: null,
          outline: null,
          padding: null,
          borderRadius: null,
          width: 300
        }
      };

      const peerInfo = callPeer ? UserStore.getUser(callPeer.id) : null;

      return (
        <Modal className="modal-new modal-new--call"
               closeTimeoutMS={150}
               style={modalStyles}
               isOpen={isOpen}>

          <div className="modal-new__header">
            <h3 className="modal-new__header__title">{callState}</h3>
          </div>

          <div className="modal-new__body">
            {
              peerInfo
                ? <AvatarItem image={peerInfo.avatar} placeholder={peerInfo.placeholder}
                              size="huge" title={peerInfo.name}/>
                : null
            }
          </div>

          <div className="modal-new__footer">
            {
              callState === CallStates.IN_PROGRESS
                ? <button className="button button--rised button--wide" onClick={this.handleMute}>Mute</button>
                : null
            }
            {
              !isOutgoing && callState === CallStates.CALLING
                ? <button className="button button--rised button--wide" onClick={this.handleAnswer}>Answer</button>
                : null
            }
            <button className="button button--rised button--wide" onClick={this.handleEnd}>
              {
                isOutgoing
                  ? 'Cancel'
                  : 'Decline'
              }
            </button>
          </div>
        </Modal>
      );
    } else {
      return null;
    }
  }
}

export default Container.create(CallModal, {pure: false});
