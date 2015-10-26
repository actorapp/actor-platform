/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';

import CreateGroupActionCreators from 'actions/CreateGroupActionCreators';

import CreateGroupStore from 'stores/CreateGroupStore';

import CreateGroupForm from './create-group/Form.react';

import Modal from 'react-modal';

import { KeyCodes } from 'constants/ActorAppConstants';

@ReactMixin.decorate(IntlMixin)
class CreateGroup extends Component {
  static getStores = () => [CreateGroupStore];
  static calculateState() {
    return {
      isOpen: CreateGroupStore.isModalOpen()
    };
  }

  componentWillUpdate(nextProps, nextState) {
    if (nextState.isOpen && !this.state.isOpen) {
      document.addEventListener('keydown', this.onKeyDown, false);
    } else if (!nextState.isOpen && this.state.isOpen) {
      document.removeEventListener('keydown', this.onKeyDown, false);
    }
  }

  render() {
    const { isOpen } = this.state;

    if (isOpen) {
      return (
        <Modal className="modal-new modal-new--create-group"
               closeTimeoutMS={150}
               isOpen={isOpen}
               style={{width: 350}}>

          <header className="modal-new__header">
            <a className="modal-new__header__close modal-new__header__icon material-icons" onClick={this.onClose}>clear</a>
            <h3 className="modal-new__header__title">{this.getIntlMessage('createGroupModalTitle')}</h3>
          </header>

          <CreateGroupForm/>

        </Modal>
      );
    } else {
      return null;
    }
  }

  onClose = () => CreateGroupActionCreators.closeModal();

  onKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  }
}

export default Container.create(CreateGroup, {pure: false});
