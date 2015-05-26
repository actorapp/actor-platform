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

  componentWillMount: function() {
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
        <MessagesSection messages={this.state.messages}></MessagesSection>
        <ComposeSection dialog={this.state.dialog}></ComposeSection>
      </section>
    )
  },

  _onChange: function() {
    this.setState(getStateFromStore());
  }
});

module.exports = DialogSection;
