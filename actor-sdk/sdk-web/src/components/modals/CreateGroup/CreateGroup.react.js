/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
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

  static contextTypes = {
    intl: PropTypes.object
  };

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
    const { intl } = this.context;

    const modalStyle = {
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
        width: 350
      }
    };

    return (
      <Modal className="modal-new modal-new--create-group"
             closeTimeoutMS={150}
             isOpen={isOpen}
             style={modalStyle}>

        <header className="modal-new__header">
          <h3 className="modal-new__header__title">{intl.messages['modal.createGroup.title']}</h3>
          <a className="modal-new__header__close modal-new__header__icon material-icons pull-right"
             onClick={this.handleClose}>clear</a>
        </header>

        <CreateGroupForm/>

      </Modal>
    );
  }
}

export default Container.create(CreateGroup);
