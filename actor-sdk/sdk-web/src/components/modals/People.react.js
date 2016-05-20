/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import Modal from 'react-modal';
import { findDOMNode } from 'react-dom';
import { Container } from 'flux/utils';
import { FormattedMessage } from 'react-intl';
import fuzzaldrin from 'fuzzaldrin';

import { KeyCodes } from '../../constants/ActorAppConstants';

import ContactActionCreators from '../../actions/ContactActionCreators';
import DialogActionCreators from '../../actions/DialogActionCreators';

import PeopleStore from '../../stores/PeopleStore';

import People from './peoples/PeopleItem.react';
import ModalCloseButton from './ModalCloseButton.react';

class PeopleList extends Component {
  static contextTypes = {
    intl: PropTypes.object
  };

  static getStores() {
    return [PeopleStore];
  }

  static calculateState(prevState) {
    return {
      ...prevState,
      selectedIndex: 0,
      contacts: PeopleStore.getState()
    };
  }

  constructor(props) {
    super(props);

    this.state = {
      query: null,
      selectedIndex: 0
    };

    this.handleClose = this.handleClose.bind(this);
    this.handleSearchChange = this.handleSearchChange.bind(this);
    this.handleContactSelect = this.handleContactSelect.bind(this);
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
    findDOMNode(this.refs.search).focus();
  }

  handleClose() {
    ContactActionCreators.close();
  }

  handleSearchChange(event) {
    const query = event.target.value;
    this.setState({ query });
  }

  handleContactSelect(contact) {
    DialogActionCreators.selectDialogPeerUser(contact.uid);
    this.handleClose()
  }

  handleKeyDown(event) {
    const { selectedIndex } = this.state;
    const results = this.getPeople();
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
        this.handleContactSelect(results[selectedIndex]);
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

  handleScroll(top)  {
    const scrollContainerNode = findDOMNode(this.refs.results);
    scrollContainerNode.scrollTop = top;
  }

  getPeople() {
    const { query, contacts } = this.state;
    if (!query || query === '') return contacts;

    return contacts.filter((contact) => {
      return fuzzaldrin.score(contact.name, query) > 0;
    });
  }

  renderPeople() {
    const { selectedIndex, contacts } = this.state;
    const people = this.getPeople();

    if (contacts.length === 0) {
      return (
        <li className="result-list__item result-list__item--empty text-center">
          <FormattedMessage id="modal.contacts.loading"/>
        </li>
      );
    }

    if (!people.length) {
      return (
        <li className="result-list__item result-list__item--empty text-center">
          <FormattedMessage id="modal.contacts.notFound"/>
        </li>
      );
    }

    return people.map((contact, index) => (
      <People
        contact={contact}
        key={contact.uid}
        onClick={this.handleContactSelect}
        isSelected={selectedIndex === index}
        ref={selectedIndex === index ? 'selected' : null}
        onMouseOver={() => this.setState({ selectedIndex: index })}
      />
    ));
  }

  renderSearch() {
    const { query } = this.state;
    const { intl } = this.context;

    return (
      <section className="large-search">
        <input className="input"
               onChange={this.handleSearchChange}
               placeholder={intl.messages['modal.contacts.search']}
               type="search"
               ref="search"
               value={query}/>
      </section>
    );
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

        <div className="people-list">
          <div className="modal__content">

            <header className="modal__header">
              <FormattedMessage id="modal.contacts.title" tagName="h1"/>
            </header>

            {this.renderSearch()}

            <div className="modal__body" ref="results">
              <ul className="result-list">
                {this.renderPeople()}
              </ul>
            </div>

          </div>
        </div>

      </Modal>
    )
  }
}

export default Container.create(PeopleList, { pure: false });
