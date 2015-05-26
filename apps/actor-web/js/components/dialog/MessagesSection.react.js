var React = require('react');
var _ = require('lodash');

var MessagesSection = React.createClass({
  propTypes: {
    messages: React.PropTypes.array.isRequired
  },

  render: function() {
    var messages = _.map(this.props.messages, function(message) {
      return(<p>{message.content.text}</p>);
    });

    return(
      <div>
        {messages}
      </div>
    )
  }
});

module.exports = MessagesSection;
