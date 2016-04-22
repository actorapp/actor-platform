/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import Modal from 'react-modal';
import { FormattedMessage } from 'react-intl';

import CreateGroupActionCreators from '../../actions/CreateGroupActionCreators';

import CreateGroupForm from './createGroup/Form.react';

class CreateGroup extends Component {
  constructor(props) {
    super(props);

    this.handleClose = this.handleClose.bind(this);
  }

  handleClose() {
    CreateGroupActionCreators.close()
  }

  render() {
    return (
      <Modal
        overlayClassName="modal-overlay"
        className="modal"
        onRequestClose={this.handleClose}
        isOpen>

        <div className="create-group">
          <div className="modal__content">

            <header className="modal__header">
              <FormattedMessage id="modal.createGroup.title" tagName="h1"/>
              <a className="modal__header__close material-icons"
                 onClick={this.handleClose}>clear</a>
            </header>

            <CreateGroupForm/>

          </div>
        </div>

      </Modal>
    );
  }
}

export default CreateGroup;
