/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import Modal from 'react-modal';

import AboutActionCreators from '../../actions/AboutActionCreators';

class About extends Component {
  constructor(props, context) {
    super(props, context);
  }

  handleClose() {
    AboutActionCreators.close();
  }

  render() {
    console.debug('About render')
    return (
      <Modal
        overlayClassName="modal-overlay"
        className="modal"
        onRequestClose={this.handleClose}
        isOpen>

        <div className="about">
          <div className="modal__content">

            <header className="modal__header">
              <i className="modal__header__icon material-icons">person_add</i>
              <FormattedMessage id="invite.title" tagName="h1"/>
            </header>

            <div className="modal__body">
            </div>

          </div>
        </div>

      </Modal>
    );
  }
}

export default About
