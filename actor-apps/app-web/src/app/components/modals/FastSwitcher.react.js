/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { map } from 'lodash';

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import Modal from 'react-modal';
import classnames from 'classnames';

import { KeyCodes } from 'constants/ActorAppConstants';

import FastSwitcherActionCreators from 'actions/FastSwitcherActionCreators';
import DialogActionCreators from 'actions/DialogActionCreators';

import FastSwitcherStore from 'stores/FastSwitcherStore';

import AvatarItem from 'components/common/AvatarItem.react';

const RESULT_ITEM_HEIGHT = 44;
let scrollIndex = 0;

class FastSwitcher extends Component {
  static getStores = () => [FastSwitcherStore];

  static calculateState() {
    return {
      isOpen: FastSwitcherStore.isOpen(),
      results: FastSwitcherStore.getResults(),
      selectedIndex: 0
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
    const { isOpen, results, selectedIndex } = this.state;

    const resultsList = map(results, (result, index) => {
      const resultClassName = classnames('results__item row', {
        'results__item--active': selectedIndex === index
      });

      return (
        <li className={resultClassName}
            key={index}
            onClick={() => this.handleSelect(result.peer.peer)}
            onMouseOver={() => this.setState({selectedIndex: index})}>
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

          <ul className="results" ref="results">
            {resultsList}
          </ul>

        </Modal>
      );
    } else {
      return null;
    }
  }

  setFocus = () => {
    setTimeout(() => {
      React.findDOMNode(this.refs.query).focus();
    }, 0);
  };

  handleClose = () => FastSwitcherActionCreators.hide();

  handleSearch = (event) => FastSwitcherActionCreators.search(event.target.value);

  handleSelect = (peer) => {
    DialogActionCreators.selectDialogPeer(peer);
    this.handleClose();
  };

  handleKeyDown = (event) => {
    const { results, selectedIndex } = this.state;
    const visibleItems = 8;
    let index = selectedIndex;

    switch (event.keyCode) {
      case KeyCodes.ENTER:
        event.stopPropagation();
        event.preventDefault();
        this.handleSelect(results[selectedIndex].peer.peer);
        break;

      case KeyCodes.ARROW_UP:
        event.stopPropagation();
        event.preventDefault();

        if (index > 0) {
          index -= 1;
        } else if (index === 0) {
          index = results.length - 1;
        }

        if (scrollIndex > index) {
          scrollIndex = index;
        } else if (index === results.length - 1) {
          scrollIndex = results.length - visibleItems;
        }

        this.handleScroll(scrollIndex * RESULT_ITEM_HEIGHT);
        this.setState({selectedIndex: index});
        break;
      case KeyCodes.ARROW_DOWN:
      case KeyCodes.TAB:
        event.stopPropagation();
        event.preventDefault();

        if (index < results.length - 1) {
          index += 1;
        } else if (index === results.length - 1) {
          index = 0;
        }

        if (index + 1 > scrollIndex + visibleItems) {
          scrollIndex = index + 1 - visibleItems;
        } else if (index === 0) {
          scrollIndex = 0;
        }

        this.handleScroll(scrollIndex * RESULT_ITEM_HEIGHT);
        this.setState({selectedIndex: index});
        break;

      case KeyCodes.ESC:
        event.preventDefault();
        this.handleClose();
        break;

      default:
    }
  };

  handleScroll = (top) => {
    const resultsNode = React.findDOMNode(this.refs.results);
    resultsNode.scrollTop = top;
  };
}

export default Container.create(FastSwitcher, {pure: false});
