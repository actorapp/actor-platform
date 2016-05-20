/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import Modal from 'react-modal';
import { findDOMNode } from 'react-dom';
import { Container } from 'flux/utils';
import { FormattedMessage, FormattedHTMLMessage } from 'react-intl';
import history from '../../utils/history';
import PeerUtils from '../../utils/PeerUtils';
import fuzzaldrin from 'fuzzaldrin';

import ModalCloseButton from './ModalCloseButton.react';
import { KeyCodes } from '../../constants/ActorAppConstants';

import GroupListActionCreators from '../../actions/GroupListActionCreators'

import GroupListStore from '../../stores/GroupListStore';

import Group from './groups/Group.react';

class GroupList extends Component {
  static contextTypes = {
    intl: PropTypes.object
  };

  static getStores() {
    return [GroupListStore];
  }

  static calculateState(prevState) {
    return {
      ...prevState,
      selectedIndex: 0,
      list: GroupListStore.getState()
    };
  }

  constructor(props) {
    super(props);

    this.state = {
      query: null,
      selectedIndex: 0
    }

    this.handleClose = this.handleClose.bind(this);
    this.handleSearchChange = this.handleSearchChange.bind(this);
    this.handleGroupSelect = this.handleGroupSelect.bind(this);
    this.handleKeyDown = this.handleKeyDown.bind(this);
    this.handleScroll = this.handleScroll.bind(this);
  }

  componentDidMount() {
    this.setFocus();
    document.addEventListener('keydown', this.handleKeyDown, false);
  }

  componentWillUnmount() {
    document.removeEventListener('keydown', this.handleKeyDown, false);
  }

  setFocus() {
    findDOMNode(this.refs.search).focus()
  }

  handleClose() {
    GroupListActionCreators.close();
  }

  handleSearchChange(event) {
    const query = event.target.value;
    this.setState({ query });
  }

  handleGroupSelect(peer) {
    const peerStr = PeerUtils.peerToString(peer);
    history.push(`/im/${peerStr}`);
    this.handleClose()
  }

  handleKeyDown(event) {
    const { selectedIndex } = this.state;
    const results = this.getList();
    const offset = 18;
    let index = selectedIndex;

    const selectNext = () => {
      if (index < results.length - 1) {
        index += 1;
      } else if (index === results.length - 1) {
        index = 0;
      }

      this.setState({ selectedIndex: index });

      const scrollContainerNode = findDOMNode(this.refs.results);
      const selectedNode = findDOMNode(this.refs.selected);
      const scrollContainerNodeRect = scrollContainerNode.getBoundingClientRect();
      const selectedNodeRect = selectedNode.getBoundingClientRect();

      if ((scrollContainerNodeRect.top + scrollContainerNodeRect.height) < (selectedNodeRect.top + selectedNodeRect.height)) {
        this.handleScroll(scrollContainerNode.scrollTop + (selectedNodeRect.top + selectedNodeRect.height) - (scrollContainerNodeRect.top + scrollContainerNodeRect.height) + offset);
      } else if (scrollContainerNodeRect.top > selectedNodeRect.top) {
        this.handleScroll(0);
      }
    };
    const selectPrev = () => {
      if (index > 0) {
        index -= 1;
      } else if (index === 0) {
        index = results.length - 1;
      }

      this.setState({ selectedIndex: index });

      const scrollContainerNode = findDOMNode(this.refs.results);
      const selectedNode = findDOMNode(this.refs.selected);
      const scrollContainerNodeRect = scrollContainerNode.getBoundingClientRect();
      const selectedNodeRect = selectedNode.getBoundingClientRect();

      if (scrollContainerNodeRect.top > selectedNodeRect.top) {
        this.handleScroll(scrollContainerNode.scrollTop + selectedNodeRect.top - scrollContainerNodeRect.top - offset);
      } else if (selectedNodeRect.top > (scrollContainerNodeRect.top + scrollContainerNodeRect.height)) {
       this.handleScroll(scrollContainerNode.scrollHeight);
      }
    };

    switch (event.keyCode) {
      case KeyCodes.ENTER:
        event.stopPropagation();
        event.preventDefault();
        this.handleGroupSelect(results[selectedIndex].peerInfo.peer);
        break;

      case KeyCodes.ARROW_UP:
        event.stopPropagation();
        event.preventDefault();
        selectPrev();
        break;
      case KeyCodes.ARROW_DOWN:
        event.stopPropagation();
        event.preventDefault();
        selectNext();
        break;
      case KeyCodes.TAB:
        event.stopPropagation();
        event.preventDefault();
        if (event.shiftKey) {
          selectPrev();
        } else {
          selectNext();
        }
        break;
      default:
    }
  }

  handleScroll(top) {
    findDOMNode(this.refs.results).scrollTop = top;
  }

  getList() {
    const { query, list } = this.state;

    if (!query || query === '') return list;

    return list.filter((group) => {
      return fuzzaldrin.score(group.peerInfo.title, query) > 0;
    });
  }

  renderSearchInput() {
    const { query } = this.state;
    const { intl } = this.context;

    return (
      <section className="large-search">
        <input className="input"
               onChange={this.handleSearchChange}
               placeholder={intl.messages['modal.groups.search']}
               type="search"
               ref="search"
               value={query}/>
      </section>
    );
  }

  renderList() {
    const { query, selectedIndex, list } = this.state;
    const results = this.getList();

    if (list.length === 0) {
      return (
        <div className="result-list__item result-list__item--empty text-center">
          <FormattedMessage id="modal.groups.loading"/>
        </div>
      );
    }

    if (results.length === 0) {
      return (
        <div className="result-list__item result-list__item--empty text-center">
          <FormattedHTMLMessage id="modal.groups.notFound" values={{ query }}/>
        </div>
      );
    }

    return results.map((result, index) => {
      return (
        <Group
          group={result}
          key={index}
          isSelected={selectedIndex === index}
          ref={selectedIndex === index ? 'selected' : null}
          onClick={this.handleGroupSelect}
          onMouseOver={() => this.setState({ selectedIndex: index })}
        />
      )
    });
  }

  render() {
    return (
      <Modal
        overlayClassName="modal-overlay modal-overlay--white"
        className="modal modal--fullscreen modal--without-scroll"
        onRequestClose={this.handleClose}
        shouldCloseOnOverlayClick={false}
        isOpen>

        <ModalCloseButton onClick={this.handleClose}/>

        <div className="group-list">
          <div className="modal__content">

            <header className="modal__header">
              <FormattedMessage id="modal.groups.title" tagName="h1"/>
            </header>

            {this.renderSearchInput()}

            <div className="modal__body" ref="results">
              <div className="result-list">
                {this.renderList()}
              </div>
            </div>

          </div>
        </div>

      </Modal>
    )
  }
}

export default Container.create(GroupList, { pure: false });
