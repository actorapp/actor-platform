/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';
import Modal from 'react-modal';
import { KeyCodes } from '../../../constants/ActorAppConstants';

import CreateGroupActionCreators from '../../../actions/CreateGroupActionCreators';

import CreateGroupStore from '../../../stores/CreateGroupStore';

import CreateGroupForm from './Form.react';

class CreateGroup extends Component {
  constructor(props) {
    super(props);
  }

  static getStores = () => [CreateGroupStore];
  static calculateState() {
    return {
      isOpen: CreateGroupStore.isModalOpen()
    };
  }

  componentWillMount() {
    document.addEventListener('keydown', this.handleKeyDown, false);
  }

  componentWillUnmount() {
    document.removeEventListener('keydown', this.handleKeyDown, false);
  }

  handleClose = () => CreateGroupActionCreators.close();

  handleKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.handleClose();
    }
  };

  render() {
    const { isOpen } = this.state;

    return (
      <Modal className="modal-new modal-new--create-group"
             closeTimeoutMS={150}
             isOpen={isOpen}
             style={{width: 350}}>

        <header className="modal-new__header">
          <h3 className="modal-new__header__title">{this.getIntlMessage('modal.createGroup.title')}</h3>
          <a className="modal-new__header__close modal-new__header__icon material-icons pull-right"
             onClick={this.handleClose}>clear</a>
        </header>

        <CreateGroupForm/>

      </Modal>
    );
  }
}

ReactMixin.onClass(CreateGroup, IntlMixin);

export default Container.create(CreateGroup);
