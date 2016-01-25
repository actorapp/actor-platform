/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { map, debounce } from 'lodash';
import React, { Component } from 'react';
import { Container } from 'flux/utils';
import classNames from 'classnames';
import Modal from 'react-modal';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedMessage } from 'react-intl';
import { KeyCodes } from '../../constants/ActorAppConstants';

import AddContactActionCreators from '../../actions/AddContactActionCreators';

import AddContactStore from '../../stores/AddContactStore';

import TextField from '../common/TextField.react';
import ContactItem from './add-contact/ContactItem.react';

class AddContact extends Component {
  constructor(props) {
    super(props);
  }

  static getStores = () => [AddContactStore];

  static calculateState() {
    return {
      isOpen: AddContactStore.isOpen(),
      results: AddContactStore.getResults(),
      isSearching: AddContactStore.isSearching()
    };
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

  handleClose = () => AddContactActionCreators.close();

  handleQueryChange = (event) => {
    const query = event.target.value;
    this.setState({query});
    this.findUsers(query);
  };

  findUsers = debounce((query) => {
    AddContactActionCreators.findUsers(query);
  }, 300, {trailing: true});

  addContact = () => AddContactActionCreators.findUsers(this.state.query);

  handleKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.handleClose();
    } else if (event.keyCode === KeyCodes.ENTER) {
      event.preventDefault();
      this.addContact()
    }
  };

  handleSelect = (uid, isContact) => {
    AddContactActionCreators.addToContacts(uid, isContact);
    this.handleClose();
  };

  render() {
    const { isOpen, isSearching, query, results } = this.state;
    const isQueryEmpty = !query || query.length === '';

    const resultContacts = map(results, (result, index) => <ContactItem key={index} {...result} onSelect={this.handleSelect}/>);

    if (resultContacts.length === 0 && !isQueryEmpty) {
      resultContacts.push(
        <li className="add-contact__results__item add-contact__results__item--not-found">
          {this.getIntlMessage('modal.addContact.notFound')}
        </li>
      );
    }

    return (
      <Modal className="modal-new modal-new--add-contact add-contact"
             closeTimeoutMS={150}
             isOpen={isOpen}
             style={{width: 360}}>

        <header className="modal-new__header">
          <h3 className="modal-new__header__title">{this.getIntlMessage('modal.addContact.title')}</h3>
          <a className="modal-new__header__close modal-new__header__icon material-icons pull-right"
             onClick={this.handleClose}>clear</a>
        </header>

        <div className="modal-new__body">
          <TextField className="input__material--wide"
                     floatingLabel={this.getIntlMessage('modal.addContact.query')}
                     onChange={this.handleQueryChange}
                     ref="query"
                     value={query}/>
        </div>

        <footer className="modal-new__footer">
          <ul className="add-contact__results">
          {
            isQueryEmpty
              ? <li className="add-contact__results__item add-contact__results__item--searching">
                  {this.getIntlMessage('modal.addContact.empty')}
                </li>
              : resultContacts

              // Search is too fast for showing searching status.
              //: isSearching
              //  ? <li className="add-contact__results__item add-contact__results__item--searching">
              //      <FormattedMessage message={this.getIntlMessage('modal.addContact.searching')} query={query}/>
              //    </li>
              //  : resultContacts
          }
          </ul>
        </footer>

      </Modal>
    );
  }
}

ReactMixin.onClass(AddContact, IntlMixin);

export default Container.create(AddContact);
