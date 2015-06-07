'use strict';

var React = require('react');

var ModalActionCreators = require('../actions/ModalActionCreators');
var ModalStore = require('../stores/ModalStore');

var classNames = require('classnames');

var getStateFromStores = function() {
  return({
    modal: ModalStore.getModal()
  })
};

var ModalSection = React.createClass({
  getInitialState: function() {
    return (getStateFromStores());
  },

  componentDidMount: function() {
    ModalStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    ModalStore.removeChangeListener(this._onChange);
  },

  render: function() {
    var modal = this.state.modal;

    if (modal !== null) {
      var modalTitle = modal.title;
      var modalContent = modal.content;

      var modalClassName = classNames('modal', 'row', 'center-xs', 'middle-xs', {
        'modal--shown': modal !== null
      });
      return (
        <section className={modalClassName}>
          <div className="modal__window">
            <header className="modal__window__header">
              <a className="modal__window__header__close material-icons" onClick={this._onClose}>clear</a>

              <h3>{modalTitle}</h3>
            </header>

            <div className="modal__window__body">
              {modalContent}
            </div>
          </div>
        </section>
      );
    } else {
      return (null);
    }
  },

  _onChange: function() {
    this.setState(getStateFromStores());
  },

  _onClose: function() {
    ModalActionCreators.hide();
  }
});

module.exports = ModalSection;
