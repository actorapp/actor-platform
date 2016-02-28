/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import Modal from 'react-modal';
 import { FormattedMessage } from 'react-intl';
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
    //this.handleClose();
  };

  handleMute = () => {
    console.debug('handleMute');
  };

  render() {
    const { isOpen, isOutgoing, callPeer, callMembers, callState } = this.state;
    const peerInfo = callPeer ? ( callPeer.type == "user" ? UserStore.getUser(callPeer.id) : null) : null;

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
        width: 240,
        minWidth: 240
      }
    };

    const modalBody = peerInfo ? (
      <div>
        <AvatarItem image={peerInfo.avatar} placeholder={peerInfo.placeholder}
                    size="big" title={peerInfo.name}/>
        <h4 className="caller-name">{peerInfo.name}</h4>
      </div>
    ) : null;

    let modalFooter;
    switch (callState) {
      case CallStates.CALLING:
        modalFooter = (
          <div>
            {
              isOutgoing
                ? null
                : <button className="button button--rised button--wide" onClick={this.handleAnswer}>
                    <FormattedMessage id="call.answer"/>
                  </button>
            }
            <button className="button button--rised button--wide" onClick={this.handleEnd}>
              {
                isOutgoing
                  ? <FormattedMessage id="button.cancel"/>
                  : <FormattedMessage id="call.decline"/>
              }
            </button>
          </div>
        );
        break;
      case CallStates.IN_PROGRESS:
      case CallStates.CONNECTING:
        modalFooter = (
          <div>
            <button className="button button--rised button--wide" onClick={this.handleMute}>
              <FormattedMessage id="call.mute"/>
            </button>
            <button className="button button--rised button--wide" onClick={this.handleEnd}>
              <FormattedMessage id="call.end"/>
            </button>
          </div>
        );
        break;
      case CallStates.ENDED:
        modalFooter = (
          <div>
            <button className="button button--rised button--wide" onClick={this.handleClose}>
              <FormattedMessage id="button.close"/>
            </button>
          </div>
        );
        break;
      default:
    }

    if (isOpen) {
      return (
        <Modal className="modal-new modal-new--call"
               closeTimeoutMS={150}
               style={modalStyles}
               isOpen={isOpen}>

          <div className="modal-new__header">
            <h3 className="modal-new__header__title">
              {
                isOutgoing
                  ? <FormattedMessage id="call.outgoing"/>
                  : <FormattedMessage id="call.incoming"/>
              }
            </h3>
          </div>

          <div className="modal-new__body">
            {/* <small>STATE: {callState}</small> */}
            {modalBody}
          </div>

          <div className="modal-new__footer">
            {modalFooter}
          </div>
        </Modal>
      );
    } else {
      return null;
    }
  }
}

export default Container.create(CallModal, {pure: false});
