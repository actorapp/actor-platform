/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { map } from 'lodash';

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedHTMLMessage } from 'react-intl';
import Modal from 'react-modal';
import classnames from 'classnames';
import isInside from '../../utils/isInside';

import { KeyCodes, PeerTypes } from '../../constants/ActorAppConstants';

import QuickSearchActionCreators from '../../actions/QuickSearchActionCreators';
import DialogActionCreators from '../../actions/DialogActionCreators';

import QuickSearchStore from '../../stores/QuickSearchStore';

import AvatarItem from '../common/AvatarItem.react';

const RESULT_ITEM_HEIGHT = 44;
let scrollIndex = 0;

class QuickSearch extends Component {
  constructor(props) {
    super(props);
  }

  static getStores = () => [QuickSearchStore];

  static calculateState() {
    return {
      isOpen: QuickSearchStore.isOpen(),
      results: QuickSearchStore.getResults(),
      selectedIndex: 0
    }
  }

  componentDidMount() {
    this.setFocus();
    document.addEventListener('keydown', this.handleKeyDown, false);
    document.addEventListener('click', this.handleDocumentClick, false);
  }

  componentWillUnmount() {
    document.removeEventListener('keydown', this.handleKeyDown, false);
    document.removeEventListener('click', this.handleDocumentClick, false);
  }

  render() {
    const { isOpen, results, selectedIndex, query } = this.state;

    const resultsList = map(results, (result, index) => {
      const resultClassName = classnames('results__item row', {
        'results__item--active': selectedIndex === index
      });

      return (
        <li className={resultClassName}
            key={index}
            onClick={() => this.handleDialogSelect(result.peerInfo.peer)}
            onMouseOver={() => this.setState({selectedIndex: index})}>
          <AvatarItem image={result.peerInfo.avatar}
                      placeholder={result.peerInfo.placeholder}
                      size="small"
                      title={result.peerInfo.title}/>
          <div className="title col-xs">
            <div className="hint pull-right">{this.getIntlMessage('modal.quickSearch.openDialog')}</div>
            {result.peerInfo.title}
          </div>
        </li>
      );
    });

    return (
      <Modal className="modal modal--quick-search"
             closeTimeoutMS={150}
             isOpen={isOpen}
             style={{width: 460}}>
        <div ref="modal">

          <header className="header">
            <div className="pull-left">{this.getIntlMessage('modal.quickSearch.title')}</div>
            <div className="pull-right"><strong>esc</strong>&nbsp; {this.getIntlMessage('modal.quickSearch.toClose')}</div>
            <div className="pull-right"><strong>↵</strong>&nbsp; {this.getIntlMessage('modal.quickSearch.toSelect')}</div>
            <div className="pull-right"><strong>tab</strong>&nbsp; or &nbsp;<strong>↑</strong><strong>↓</strong>&nbsp; {this.getIntlMessage('modal.quickSearch.toNavigate')}</div>
          </header>

          <div className="input">
            <input type="text"
                   placeholder={this.getIntlMessage('modal.quickSearch.placeholder')}
                   onChange={this.handleSearch}
                   value={query}
                   ref="query"/>
          </div>

          <ul className="results" ref="results">
            {
              resultsList.length > 0
                ? resultsList
                : <li className="results__item results__item--suggestion row">
                    <FormattedHTMLMessage
                      message={this.getIntlMessage('modal.quickSearch.notFound')}
                      query={query} />
                    <button className="button button--rised hide">Create new dialog {query}</button>
                  </li>
            }
          </ul>

        </div>
      </Modal>
    );
  }

  setFocus = () => {
    setTimeout(() => {
      React.findDOMNode(this.refs.query).focus();
    }, 0);
  };

  handleClose = () => QuickSearchActionCreators.hide();

  handleSearch = (event) => {
    const query = event.target.value;
    this.setState({query});
    QuickSearchActionCreators.search(query);
  };

  handleDialogSelect = (peer) => {
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

  handleDocumentClick = (event) => {
    const modal = React.findDOMNode(this.refs.modal);
    const modalRect = modal.getBoundingClientRect();
    const coords = {
      x: event.pageX || event.clientX,
      y: event.pageY || event.clientY
    };

    if (!isInside(coords, modalRect)) {
      this.handleClose();
    }
  };
}

ReactMixin.onClass(QuickSearch, IntlMixin);

export default Container.create(QuickSearch, {pure: false});
