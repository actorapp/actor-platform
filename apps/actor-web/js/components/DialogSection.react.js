var _ = require('lodash');

var React = require('react');

var MessagesSection = require('./dialog/MessagesSection.react');
var ComposeSection = require('./dialog/ComposeSection.react');

var DialogStore = require('../stores/DialogStore');
var MessageStore = require('../stores/MessageStore');

var initialMessagesCount = 100;

var getStateFromStores = function() {
  var messages = MessageStore.getAll();

  var messagesToRender;

  if (messages.length > initialMessagesCount) {
    messagesToRender = messages.slice(messages.length - initialMessagesCount);
  } else {
    messagesToRender = messages;
  }

  return({
    dialog: DialogStore.getSelectedDialog(),
    messages: messages,
    messagesToRender: messagesToRender
  });
};

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

  render: function() {
    return(
      <section className="dialog">
        <MessagesSection messages={this.state.messagesToRender} ref="MessagesSection"/>
        <ComposeSection dialog={this.state.dialog}/>
      </section>
    )
  },

  _scrollToBottom: function() {
    var ul = this.refs.MessagesSection.getDOMNode();
    ul.scrollTop = ul.scrollHeight;
  },

  _scrollToBottomDebounced: _.debounce(function() {
    this._scrollToBottom();
  }, 50),

  _scrolledToBottom: function() {
    var self = this.getDOMNode();

    return(self.scrollHeight - self.scrollTop - self.clientHeight == 0);
  },

  _onChange: _.debounce(function() {
    this.setState(getStateFromStores());

    if (this._scrolledToBottom()) {
      this._scrollToBottomDebounced()
    }
  }, 30, 300)
});

module.exports = DialogSection;
