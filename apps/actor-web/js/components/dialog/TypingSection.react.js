'use strict';

var React = require('react');
var PureRenderMixin = require('react/addons').addons.PureRenderMixin;

var classNames = require('classnames');

var DialogStore = require('../../stores/DialogStore');

var TypingSection = React.createClass({
  mixins: [PureRenderMixin],

  getInitialState: function() {
    return {typing: null};
  },
  componentDidMount: function() {
    DialogStore.addTypingListener(this._onTypingChange);
  },

  componentWillUnmount: function() {
    DialogStore.removeTypingListener(this._onTypingChange);
  },

  render: function() {
    var typing = this.state.typing;
    var typingClassName = classNames('typing', {
      'typing--hidden': typing == null
    });

    return (
      <div className={typingClassName}>
        <i className="material-icons">keyboard</i>
        <span>{typing}</span>
      </div>
    );
  },

  _onTypingChange: function() {
    var typing = DialogStore.getSelectedDialogTyping();
    this.setState({typing: typing});
  }

});

module.exports = TypingSection;
