var React = require('react');

var _ = require('lodash');

var MessageActionCreators = require('../../actions/MessageActionCreators');

var VisibilitySensor = require('react-visibility-sensor');
var MessageItem = require('../common/MessageItem.react');

var debouncedOnVisibleChange = _.debounce(function(isVisible) {
  if (isVisible) {
    MessageActionCreators.setMessageShown(this.props.peer, this.props.message)
  }
}, 10, {maxWait: 50});

var ReadableMessage = React.createClass({
  propTypes: {
    peer: React.PropTypes.object.isRequired,
    message: React.PropTypes.object.isRequired
  },

  render: function() {
    return(
      <VisibilitySensor onChange={this._onVisibilityChange}>
        <MessageItem message={this.props.message}/>
      </VisibilitySensor>
    )
  },

  _onVisibilityChange: debouncedOnVisibleChange
});

var MessagesSection = React.createClass({
  propTypes: {
    messages: React.PropTypes.array.isRequired,
    peer: React.PropTypes.object.isRequired
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
      <ReadableMessage key={message.sortKey} peer={this.props.peer} message={message}/>
    );
  }
});

module.exports = MessagesSection;
