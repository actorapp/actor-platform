/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';

import Modal from 'react-modal';

import { KeyCodes } from '../../constants/ActorAppConstants';

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

  render() {
    const { isOpen, callType } = this.state;

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

          <div className="modal-new__footer">
            <button className="button button--rised button--wide">Answer</button>
            <button className="button button--rised button--wide">Decline</button>
          </div>
        </Modal>
      );
    } else {
      return null;
    }
  }
}

ReactMixin.onClass(CallModal, IntlMixin);

export default Container.create(CallModal, {pure: false});
