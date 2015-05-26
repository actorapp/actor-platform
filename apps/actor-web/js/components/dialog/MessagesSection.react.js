var React = require('react');
var _ = require('lodash');

var MessageItem = require('../common/MessageItem.react');

var MessagesSection = React.createClass({
  propTypes: {
    messages: React.PropTypes.array.isRequired
  },

  render: function() {
    var messages = _.map(this.props.messages, function(message) {
      return(
        <MessageItem message={message}></MessageItem>
      );
    });

    return(
      <section className="messages">
        <ul className="messages__list">
          {messages}
        </ul>
      </section>
    )
  }
});

module.exports = MessagesSection;
