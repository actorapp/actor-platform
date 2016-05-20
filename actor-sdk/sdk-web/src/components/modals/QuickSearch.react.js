/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { findDOMNode } from 'react-dom';
import { Container } from 'flux/utils';
import { FormattedMessage, FormattedHTMLMessage } from 'react-intl';
import EventListener from 'fbjs/lib/EventListener';
import fuzzaldrin from 'fuzzaldrin';
import Modal from 'react-modal';
import classnames from 'classnames';
import history from '../../utils/history';
import PeerUtils from '../../utils/PeerUtils';

import { KeyCodes } from '../../constants/ActorAppConstants';

import QuickSearchActionCreators from '../../actions/QuickSearchActionCreators';

import QuickSearchStore from '../../stores/QuickSearchStore';

import AvatarItem from '../common/AvatarItem.react';

const RESULT_ITEM_HEIGHT = 44;
let scrollIndex = 0;

class QuickSearch extends Component {
  static getStores() {
    return [QuickSearchStore];
  }

  static calculateState() {
    return {
      list: QuickSearchStore.getList(),
      selectedIndex: 0
    }
  }

  static contextTypes = {
    intl: PropTypes.object.isRequired
  }

  constructor(props, context) {
    super(props, context);

    this.setFocus = this.setFocus.bind(this);
    this.handleClose = this.handleClose.bind(this);
    this.handleSearch = this.handleSearch.bind(this);
    this.handleDialogSelect = this.handleDialogSelect.bind(this);
    this.handleKeyDown = this.handleKeyDown.bind(this);
    this.handleScroll = this.handleScroll.bind(this);
  }

  componentDidMount() {
    this.setFocus();
    this.setListeners();
  }

  componentWillUnmount() {
    this.cleanListeners();
  }

  setListeners() {
    this.cleanListeners();
    this.listeners = [
      EventListener.listen(document, 'keydown', this.handleKeyDown)
    ];
  }

  cleanListeners() {
    if (this.listeners) {
      this.listeners.forEach((listener) => listener.remove());
      this.listeners = null;
    }
  }

  setFocus() {
    setImmediate(() => findDOMNode(this.refs.query).focus());
  }

  handleClose() {
    QuickSearchActionCreators.hide();
  }

  handleSearch(event) {
    this.setState({ query: event.target.value });
  }

  handleDialogSelect(peer) {
    const peerStr = PeerUtils.peerToString(peer);
    history.push(`/im/${peerStr}`);
    this.handleClose();
  }

  handleKeyDown(event) {
    const { selectedIndex } = this.state;
    const results = this.getResults();
    const visibleItems = 8;
    let index = selectedIndex;

    switch (event.keyCode) {
      case KeyCodes.ENTER:
        event.stopPropagation();
        event.preventDefault();
        this.handleDialogSelect(results[selectedIndex].peerInfo.peer);
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
        this.setState({ selectedIndex: index });
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
        this.setState({ selectedIndex: index });
        break;

      default:
    }
  }

  handleScroll(top) {
    findDOMNode(this.refs.results).scrollTop = top;
  }

  getResults() {
    const { list, query } = this.state;
    if (!query || query === '') return list;

    return list.filter((result) => {
      return fuzzaldrin.score(result.peerInfo.title, query) > 0 ||
             fuzzaldrin.score(result.peerInfo.userName, query) > 0;
    });
  }

  renderResults() {
    const { selectedIndex, query } = this.state;
    const results = this.getResults();

    if (!results.length) {
      return (
        <li className="results__item results__item--suggestion row">
          <FormattedHTMLMessage id="modal.quickSearch.notFound" values={{ query }}/>
          <button className="button button--rised hide">Create new dialog {query}</button>
        </li>
      )
    }

    return results.map((result, index) => {
      const resultClassName = classnames('results__item row', {
        'results__item--active': selectedIndex === index
      });

      return (
        <li
          className={resultClassName} key={`r${index}`}
          onClick={() => this.handleDialogSelect(result.peerInfo.peer)}
          onMouseOver={() => this.setState({ selectedIndex: index })}>
          <AvatarItem
            className="quick-search__avatar"
            size="small"
            image={result.peerInfo.avatar}
            placeholder={result.peerInfo.placeholder}
            title={result.peerInfo.title}
          />
          <div className="title col-xs">
            <div className="hint pull-right"><FormattedMessage id="modal.quickSearch.openDialog"/></div>
            {result.peerInfo.title}
          </div>
        </li>
      );
    });
  }

  renderHeader() {
    return (
      <header className="header">
        <div className="pull-left"><FormattedMessage id="modal.quickSearch.title"/></div>
        <div className="pull-right"><strong>esc</strong>&nbsp; <FormattedMessage id="modal.quickSearch.toClose"/></div>
        <div className="pull-right"><strong>↵</strong>&nbsp; <FormattedMessage id="modal.quickSearch.toSelect"/></div>
        <div className="pull-right"><strong>tab</strong>&nbsp; or &nbsp;<strong>↑</strong><strong>↓</strong>&nbsp; <FormattedMessage id="modal.quickSearch.toNavigate"/></div>
      </header>
    );
  }

  renderSearchInput() {
    const { query } = this.state;
    const { intl } = this.context;

    return (
      <div className="large-search">
        <input
          className="input"
          type="text"
          placeholder={intl.messages['modal.quickSearch.placeholder']}
          onChange={this.handleSearch}
          value={query}
          ref="query"/>
      </div>
    );
  }

  render() {
    return (
      <Modal
        overlayClassName="modal-overlay"
        className="modal"
        onRequestClose={this.handleClose}
        isOpen>

        <div className="quick-search">
          <div className="modal__content">

            {this.renderHeader()}

            {this.renderSearchInput()}

            <ul className="results" ref="results">
              {this.renderResults()}
            </ul>

          </div>
        </div>

      </Modal>
    );
  }
}

export default Container.create(QuickSearch, { pure: false });
