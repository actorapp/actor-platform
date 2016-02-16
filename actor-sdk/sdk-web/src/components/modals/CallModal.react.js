/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
// import { FormattedMessage } from 'react-intl';
import Modal from 'react-modal';

import { KeyCodes, CallTypes } from '../../constants/ActorAppConstants';

import CallActionCreators from '../../actions/CallActionCreators';

import CallStore from '../../stores/CallStore'

class CallModal extends Component {
  constructor(props) {
    super(props);
  }

  static getStores = () => [CallStore];

  static calculateState() {
    return {
      isOpen: CallStore.isOpen(),
      callId: CallStore.getCallId(),
      callType: CallStore.getCallType(),
      callMembers: CallStore.getCallMembers(),
      callPeer: CallStore.getCallPeer(),
      callState: CallStore.getCallState()
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
    console.debug('handleAnswer', callId);
    CallActionCreators.answerCall(callId);
  };

  handleDecline = () => {
    const { callId } = this.state;
    console.debug('handleDecline', callId);
  };

  handleEnd = () => {
    const { callId } = this.state;
    console.debug('handleEnd', callId);
    CallActionCreators.endCall(callId);
  };

  render() {
    const { isOpen, callType } = this.state;

    let modalFooter;
    switch (callType) {
      case CallTypes.INCOMING:
        modalFooter = (
          <div className="modal-new__footer">
            <button className="button button--rised button--wide" onClick={this.handleAnswer}>Answer</button>
            <button className="button button--rised button--wide" onClick={this.handleDecline}>Decline</button>
          </div>
        );
        break;
      case CallTypes.OUTGOING:
        modalFooter = (
          <div className="modal-new__footer">
            <button className="button button--rised button--wide" onClick={this.handleEnd}>End</button>
          </div>
        );
        break;
      default:
    }

    if (isOpen) {
      return (
        <Modal className="modal-new modal-new--call"
               closeTimeoutMS={150}
               isOpen={isOpen}>

          <div className="modal-new__header">
            <h3 className="modal-new__header__title">{`${callType} call`}</h3>
          </div>

          <div className="modal-new__body">
          </div>

          {modalFooter}
        </Modal>
      );
    } else {
      return null;
    }
  }
}

export default Container.create(CallModal, {pure: false});
