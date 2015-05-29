var _ = require('lodash');

var React = require('react');

var MessagesSection = require('./dialog/MessagesSection.react');
var ComposeSection = require('./dialog/ComposeSection.react');

var DialogStore = require('../stores/DialogStore');
var MessageStore = require('../stores/MessageStore');

var _initialRenderMessagesCount = 20;
var _renderMessagesStep = 20;

var _renderMessagesCount = _initialRenderMessagesCount;

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
    DialogStore.addSelectListener(this._onSelectedDialogChange);
    MessageStore.addChangeListener(this._onMessagesChange);
  },

  componentWillUnmount: function() {
    MessageStore.removeChangeListener(this._onMessagesChange);
    DialogStore.removeSelectListener(this._onSelectedDialogChange);
  },

  componentDidUpdate: function() {
    this._fixScroll();
    this._loadMessagesByScroll();
  },

  render: function() {
    if (this.state.dialog) {
      return (
        <section className="dialog" onScroll={this._loadMessagesByScroll}>
          <MessagesSection peer={this.state.dialog.peer.peer}
                           messages={this.state.messagesToRender}
                           ref="MessagesSection"/>
          <ComposeSection dialog={this.state.dialog}/>
        </section>
      )
    } else {
      return(
        <section className="dialog row middle-xs center-xs">
          Select dialog or start new.
        </section>
      )
    }
  },

  _fixScroll: function() {
    var node = this.refs.MessagesSection.getDOMNode();
    node.scrollTop = node.scrollHeight - _lastScrolledFromBottom;
  },

  _onSelectedDialogChange: function() {
    _renderMessagesCount = _initialRenderMessagesCount;
  },

  _onMessagesChange: _.debounce(function() {
    this.setState(getStateFromStores());
  }, 10, {maxWait: 50, leading: true}),

  _loadMessagesByScroll: _.debounce(function() {
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
