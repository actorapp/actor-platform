var React = require('react');
var _ = require('lodash');

var MessageStore = require('../../stores/MessageStore.react');

var getStateFromStore = function() {
  return({messages: MessageStore.getAll()});
};

var MessagesSection = React.createClass({
  getInitialState: function() {
    return(getStateFromStore());
  },

  componentWillMount: function() {
    MessageStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    MessageStore.removeChangeListener(this._onChange);
  },

  render: function() {
    var messages = _.map(this.state.messages, function(message) {
      return(<p>{message.content.text}</p>);
    });

    return(
      <div>
        {messages}
      </div>
    )
  },

  _onChange: function() {
    this.setState(getStateFromStore());
  }
});

module.exports = MessagesSection;
