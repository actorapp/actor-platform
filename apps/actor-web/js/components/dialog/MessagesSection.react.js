var React = require('react');
var _ = require('lodash');

var MessageItem = require('../common/MessageItem.react');

var getMessagesListItem = function (message) {
  return (
    <MessageItem message={message}/>
  );
};

var MessagesSection = React.createClass({
  propTypes: {
    messages: React.PropTypes.array.isRequired
  },

  render: function() {
    var messages = _.map(this.props.messages, getMessagesListItem);

    return(
      <ul className="messages">
        {messages}
      </ul>
    )
  }
});

module.exports = MessagesSection;
