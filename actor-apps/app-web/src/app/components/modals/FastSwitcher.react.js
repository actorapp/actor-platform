/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import Modal from 'react-modal';

import { KeyCodes } from 'constants/ActorAppConstants';

import FastSwitcherActionCreators from 'actions/FastSwitcherActionCreators';

import FastSwitcherStore from 'stores/FastSwitcherStore';

class FastSwitcher extends Component {
  static getStores = () => [FastSwitcherStore];

  static calculateState() {
    return {
      isOpen: FastSwitcherStore.isOpen()
    }
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

    const results = [];

    if (isOpen) {
      return (
        <Modal className="modal modal--fast-switcher"
               closeTimeoutMS={150}
               isOpen={isOpen}
               style={{width: 400}}>

          <header className="input">
            <input type="text" placeholder="Start typing" onChange={this.onSearch}/>
          </header>

          <section className="results">{results}</section>

        </Modal>
      );
    } else {
      return null;
    }
  }

  onClose = () => FastSwitcherActionCreators.hide();

  onKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  };

  onSearch = (event) => {
    console.debug(event.target.value);
  };

}

export default Container.create(FastSwitcher, {pure: false});
