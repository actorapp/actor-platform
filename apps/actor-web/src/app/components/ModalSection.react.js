'use strict';

var React = require('react');

var ModalActionCreators = require('../actions/ModalActionCreators');
var ModalStore = require('../stores/ModalStore');

var classNames = require('classnames');
var Modal = require('react-modal');

var appElement = document.getElementById('actor-web-app');
Modal.setAppElement(appElement);

var getStateFromStores = function() {
  return({
    isModalOpen: ModalStore.isModalOpen()
  })
};

var ModalSection = React.createClass({
  getInitialState: function() {
    return (getStateFromStores());
  },

  componentWillMount: function() {
    ModalStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    ModalStore.removeChangeListener(this._onChange);
  },

  render: function() {
    return(
      <Modal
        closeTimeoutMS={150}
        isOpen={this.state.isModalOpen}>
          <header className="ReactModal__Content__header">
            <a className="ReactModal__Content__header__close material-icons" onClick={this._onClose}>clear</a>
            <h3>Header</h3>
          </header>
          <div className="ReactModal__Content__body">
            Content
          </div>
      </Modal>
    );
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
    ModalActionCreators.hide();
  }
});

module.exports = ModalSection;
