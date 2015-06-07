'use strict';

var React = require('react');
var PureRenderMixin = require('react/addons').addons.PureRenderMixin;

var classNames = require('classnames');

var ModalsSection = React.createClass({
  mixins: [PureRenderMixin],

  propTypes: {},

  getInitialState: function() {
    return({isShown: true});
  },

  render: function() {
    var isShown = this.state.isShown;

    var modalClassName = classNames('modals', 'row', 'center-xs', 'middle-xs', {
      'modals--shown': isShown
    });

    return (
      <section className={modalClassName}>
        <div className="modal">
          <header className="modal__header">
            <a className="modal__header__close material-icons" onClick={this._onClose}>clear</a>
            <h3>ModalsSection</h3>
          </header>
        </div>
      </section>
    );
  },

  _onClose: function() {
    console.warn('Close modal');
    this.setState({isShown: !this.state.isShown});
  }
});

module.exports = ModalsSection;
