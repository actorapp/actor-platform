/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { map } from 'lodash';

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import Modal from 'react-modal';

import { KeyCodes } from 'constants/ActorAppConstants';

import FastSwitcherActionCreators from 'actions/FastSwitcherActionCreators';

import FastSwitcherStore from 'stores/FastSwitcherStore';

import AvatarItem from 'components/common/AvatarItem.react';

class FastSwitcher extends Component {
  static getStores = () => [FastSwitcherStore];

  static calculateState() {
    return {
      isOpen: FastSwitcherStore.isOpen(),
      results: FastSwitcherStore.getResults()
    }
  }

  componentDidMount() {
    this.setFocus();
    document.addEventListener('keydown', this.handleKeyDown, false);
  }

  componentWillUnmount() {
    document.removeEventListener('keydown', this.handleKeyDown, false);
  }

  render() {
    const { isOpen, results } = this.state;

    const resultsList = map(results, (result) => {
      return (
        <li className="results__item row">
          <AvatarItem image={result.peer.avatar}
                      placeholder={result.peer.placeholder}
                      size="small"
                      title={result.peer.title}/>
          <div className="title col-xs">{result.peer.title}</div>
        </li>
      )
    });

    console.debug(resultsList.length);

    if (isOpen) {
      return (
        <Modal className="modal modal--fast-switcher"
               closeTimeoutMS={150}
               isOpen={isOpen}
               style={{width: 400}}>

          <header className="header">
            <div className="pull-left">Jump to anywhere</div>
            <div className="pull-right"><strong>esc</strong>&nbsp; to close</div>
            <div className="pull-right"><strong>↵</strong>&nbsp; to select</div>
            <div className="pull-right"><strong>tab</strong>&nbsp; or &nbsp;<strong>↑</strong><strong>↓</strong>&nbsp; to navigate</div>
          </header>

          <div className="input">
            <input type="text"
                   placeholder="Start typing"
                   onChange={this.handleSearch}
                   ref="query"/>
          </div>

          <ul className="results">
            {resultsList}
          </ul>

        </Modal>
      );
    } else {
      return null;
    }
  }

  handleClose = () => FastSwitcherActionCreators.hide();
  handleSearch = (event) => FastSwitcherActionCreators.search(event.target.value);
  setFocus = () => React.findDOMNode(this.refs.query).focus();

  handleKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.handleClose();
    }
  };

}

export default Container.create(FastSwitcher, {pure: false});
