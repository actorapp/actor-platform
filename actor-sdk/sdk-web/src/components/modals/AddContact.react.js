/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { debounce } from 'lodash';
import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import Modal from 'react-modal';
import { FormattedMessage } from 'react-intl';
import { KeyCodes } from '../../constants/ActorAppConstants';

import AddContactActionCreators from '../../actions/AddContactActionCreators';

import AddContactStore from '../../stores/AddContactStore';

import TextField from '../common/TextField.react';
import ContactItem from './addContact/ContactItem.react';

class AddContact extends Component {
  static getStores() {
    return [AddContactStore];
  }

  static calculateState() {
    return {
      results: AddContactStore.getResults(),
      isSearching: AddContactStore.isSearching()
    };
  }

  static contextTypes = {
    intl: PropTypes.object
  };

  constructor(props, context) {
    super(props, context);

    this.handleClose = this.handleClose.bind(this);
    this.handleQueryChange = this.handleQueryChange.bind(this);
    this.findUsers = debounce(this.findUsers, 300, { trailing: true });
    this.addContact = this.addContact.bind(this);
    this.handleKeyDown = this.handleKeyDown.bind(this);
    this.handleSelect = this.handleSelect.bind(this);
  }

  componentWillMount() {
    document.addEventListener('keydown', this.handleKeyDown, false);
  }

  componentDidMount() {
    this.refs.query.focus();
  }

  componentWillUnmount() {
    document.removeEventListener('keydown', this.handleKeyDown, false);
  }

  handleClose() {
    AddContactActionCreators.close();
  }

  handleQueryChange(event) {
    const query = event.target.value;
    this.setState({ query });
    this.findUsers(query);
  }

  findUsers(query) {
    AddContactActionCreators.findUsers(query);
  }

  addContact() {
    AddContactActionCreators.findUsers(this.state.query)
  }

  handleKeyDown(event) {
    if (event.keyCode === KeyCodes.ENTER) {
      event.preventDefault();
      this.addContact()
    }
  }

  handleSelect(uid, isContact) {
    AddContactActionCreators.addToContacts(uid, isContact);
    this.handleClose();
  }

  renderUserSearchInput() {
    const { query } = this.state;
    return (
      <TextField
        className="input__material--wide"
        floatingLabel={<FormattedMessage id="modal.addContact.query"/>}
        onChange={this.handleQueryChange}
        ref="query"
        value={query}/>
    );
  }

  renderUserSearchResults() {
    const { query, results } = this.state;

    if (!query || query.length === '') {
      return (
        <li className="add-contact__results__item add-contact__results__item--searching">
          <FormattedMessage id="modal.addContact.empty"/>
        </li>
      );
    }

    // Disabled becouse searching is very fast and this message is blinking
    // if (isSearching) {
    //   return (
    //     <li className="add-contact__results__item add-contact__results__item--searching">
    //       <FormattedMessage id="modal.addContact.searching" values={{query}}/>
    //     </li>
    //   );
    // }

    if (results.length === 0) {
      return (
        <li className="add-contact__results__item add-contact__results__item--not-found">
          <FormattedMessage id="modal.addContact.notFound"/>
        </li>
      );
    }

    return results.map((result, index) => {
      return <ContactItem key={index} {...result} onSelect={this.handleSelect}/>
    });
  }

  render() {
    return (
      <Modal
        overlayClassName="modal-overlay"
        className="modal"
        onRequestClose={this.handleClose}
        isOpen>

        <div className="add-contact">
          <div className="modal__content">

            <header className="modal__header">
              <FormattedMessage id="modal.addContact.title" tagName="h1"/>
              <a className="modal__header__close material-icons"
                 onClick={this.handleClose}>clear</a>
            </header>

            <div className="modal__body">
              {this.renderUserSearchInput()}
            </div>

            <footer className="modal__footer">
              <ul className="add-contact__results">
                {this.renderUserSearchResults()}
              </ul>
            </footer>

          </div>
        </div>

      </Modal>
    );
  }
}

export default Container.create(AddContact);
