var _ = require('lodash');

var React = require('react');

var MessagesSection = require('./dialog/MessagesSection.react');
var ComposeSection = require('./dialog/ComposeSection.react');

var DialogStore = require('../stores/DialogStore');
var MessageStore = require('../stores/MessageStore');

var _renderMessagesStep = 100;
var _renderMessagesCount = 100;

var getStateFromStores = function() {
  var messages = MessageStore.getAll();

  var messagesToRender;

  if (messages.length > _renderMessagesCount) {
    messagesToRender = messages.slice(messages.length - _renderMessagesCount);
  } else {
    messagesToRender = messages;
  }

  return({
    dialog: DialogStore.getSelectedDialog(),
    messages: messages,
    messagesToRender: messagesToRender
  });
};

var _lastScrolledFromBottom = 0;

var DialogSection = React.createClass({
  getInitialState: function() {
    return(getStateFromStores());
  },

  componentDidMount: function() {
    DialogStore.addSelectListener(this._onChange);
    MessageStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    MessageStore.removeChangeListener(this._onChange);
    DialogStore.removeSelectListener(this._onChange);
  },

  componentDidUpdate: function() {
    this._fixScroll()
  },

  render: function() {
    if (this.state.dialog) {
      return (
        <section className="dialog" onScroll={this._onScroll}>
          <MessagesSection peer={this.state.dialog.peer.peer}
                           messages={this.state.messagesToRender}
                           ref="MessagesSection"/>
          <ComposeSection dialog={this.state.dialog}/>
        </section>
      )
    } else {
      return(
        <section className="dialog"></section>
      )
    }
  },

  _fixScroll: function() {
    var node = this.refs.MessagesSection.getDOMNode();
    node.scrollTop = node.scrollHeight - _lastScrolledFromBottom;
  },

  _onChange: _.debounce(function() {
    this.setState(getStateFromStores());
  }, 10, {maxWait: 50, leading: true}),

  _onScroll: _.debounce(function() {
    var node = this.refs.MessagesSection.getDOMNode();

    var scrollTop = node.scrollTop;
    _lastScrolledFromBottom = node.scrollHeight - scrollTop;

    if (node.scrollTop == 0) {
      if (this.state.messages.length > this.state.messagesToRender.length) {
        _renderMessagesCount += _renderMessagesStep;

        if (_renderMessagesCount > this.state.messages.length) {
          _renderMessagesCount = this.state.messages.length;
        }

        this.setState(getStateFromStores());
      }
    }
  }, 10)
});

module.exports = DialogSection;
