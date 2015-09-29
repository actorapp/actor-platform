/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React from 'react';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';

import CreateGroupActionCreators from 'actions/CreateGroupActionCreators';
import CreateGroupStore from 'stores/CreateGroupStore';

import CreateGroupForm from './create-group/Form.react';

import Modal from 'react-modal';

import { KeyCodes } from 'constants/ActorAppConstants';

const getStateFromStores = () => {
  return {
    isOpen: CreateGroupStore.isModalOpen()
  };
};

@ReactMixin.decorate(IntlMixin)
class CreateGroup extends React.Component {
  constructor(props) {
    super(props);

    this.state = getStateFromStores();

    CreateGroupStore.addChangeListener(this.onChange);
    document.addEventListener('keydown', this.onKeyDown, false);
  }

  componentWillUnmount() {
    CreateGroupStore.removeChangeListener(this.onChange);
    document.removeEventListener('keydown', this.onKeyDown, false);
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

  onChange = () => this.setState(getStateFromStores());

  onClose = () => CreateGroupActionCreators.closeModal();

  onKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  }
}

export default CreateGroup;
