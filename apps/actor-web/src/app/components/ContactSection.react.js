'use strict';

var React = require('react');

var ContactActionCreators = require('../actions/ContactActionCreators');
var ContactStore = require('../stores/ContactStore');

var classNames = require('classnames');
var Modal = require('react-modal');

var appElement = document.getElementById('actor-web-app');
Modal.setAppElement(appElement);

var getStateFromStores = function() {
  return({
    contacts: ContactStore.getContacts(),
    isShown: ContactStore.isContactsOpen()
  })
};

var ContactSection = React.createClass({
  getInitialState: function() {
    return (getStateFromStores());
  },

  componentWillMount: function() {
    ContactStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    ContactStore.removeChangeListener(this._onChange);
  },

  render: function() {
    var contacts = this.state.contacts;
    var isShown = this.state.isShown;

    var contactList;

    if (contacts !== null) {
      return(
        <Modal closeTimeoutMS={150}
               isOpen={isShown}>

          <header className="ReactModal__Content__header">
            <a className="ReactModal__Content__header__close material-icons" onClick={this._onClose}>clear</a>
            <h3>Contact list</h3>
          </header>

          <div className="ReactModal__Content__body">
            {contactList}
          </div>
        </Modal>
      );
    }
    //var modal = this.state.modal;
    //
    //if (modal !== null) {
    //  var modalTitle = modal.title;
    //  var modalContent = modal.content;
    //
    //  var modalClassName = classNames('modal', 'row', 'center-xs', 'middle-xs', {
    //    'modal--shown': modal !== null
    //  });
    //  return (
    //    <section className={modalClassName}>
    //      <div className="modal__window">
    //        <header className="modal__window__header">
    //          <a className="modal__window__header__close material-icons" onClick={this._onClose}>clear</a>
    //
    //          <h3>{modalTitle}</h3>
    //        </header>
    //
    //        <div className="modal__window__body">
    //          {modalContent}
    //        </div>
    //      </div>
    //    </section>
    //  );
    //} else {
    //  return (null);
    //}
  },

  _onChange: function() {
    this.setState(getStateFromStores());
  },

  _onClose: function() {
    ContactActionCreators.hideContactList();
  }
});

module.exports = ContactSection;
