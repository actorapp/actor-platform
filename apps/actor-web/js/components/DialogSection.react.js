var React = require('react');

var MessagesSection = require('./dialog/MessagesSection.react');
var ComposeSection = require('./dialog/ComposeSection.react');

var DialogStore = require('../stores/DialogStore');
var MessageStore = require('../stores/MessageStore');

var getStateFromStore = function() {
  return({
    dialog: DialogStore.getSelectedDialog(),
    messages: MessageStore.getAll()
  });
};

var DialogSection = React.createClass({
  getInitialState: function() {
    return(getStateFromStore());
  },

  componentDidMount: function() {
    DialogStore.addSelectListener(this._onChange);
    MessageStore.addChangeListener(this._onChange);
    MessageStore.addChangeListener(this._scrollToBottom);
  },

  componentWillUnmount: function() {
    MessageStore.removeChangeListener(this._onChange);
    DialogStore.removeSelectListener(this._onChange);
  },

  render: function() {
    return(
      <section className="dialog">
        <MessagesSection messages={this.state.messages} ref="MessagesSection"/>
        <ComposeSection dialog={this.state.dialog}/>
      </section>
    )
  },

  _scrollToBottom: function() {
    var ul = this.refs.MessagesSection.getDOMNode();
    ul.scrollTop = ul.scrollHeight;
  },

  _onChange: function() {
    this.setState(getStateFromStore());
    setTimeout(function() { this._scrollToBottom() }, 0);
  }
});

module.exports = DialogSection;
