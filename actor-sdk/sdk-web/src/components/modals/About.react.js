/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import Modal from 'react-modal';
// import { FormattedMessage } from 'react-intl';
import SharedContainer from '../../utils/SharedContainer';

import AboutActionCreators from '../../actions/AboutActionCreators';

class About extends Component {
  constructor(props, context) {
    super(props, context);

    const SharedActor = SharedContainer.get();
    this.appName = SharedActor.appName ? SharedActor.appName : appName;
  }

  handleClose() {
    AboutActionCreators.close();
  }

  render() {
    const appTitle = `${this.appName} messenger`;

    return (
      <Modal
        overlayClassName="modal-overlay"
        className="modal"
        onRequestClose={this.handleClose}
        isOpen>

        <div className="about">
          <div className="modal__content">

            <div className="modal__body">
              <img
                className="about__logo"
                src="/assets/images/about_logo.png"
                alt={appTitle}/>

              <div className="about__title">{appTitle}</div>
              <div className="about__version"></div>
            </div>

          </div>
        </div>

      </Modal>
    );
  }
}

export default About
