var React = require('react');

var _ = require('lodash');

var MessageItem = require('../common/MessageItem.react');

var MessagesSection = React.createClass({
  propTypes: {
    messages: React.PropTypes.array.isRequired
  },

  render: function() {
    var messages = _.map(this.props.messages, this._getMessagesListItem);

    return(
      <ul className="messages">
        {messages}
      </ul>
    )
  },

  _getMessagesListItem: function (message, index) {
    return (
      <MessageItem key={index} message={message}/>
    );
  }

});

module.exports = MessagesSection;
