/** @jsx React.DOM */

var React = require('react');
var Avatar = require('./avatar.jsx');
var Message = require('./message.jsx');
var VisibilitySensor = require('react-visibility-sensor');

var ChatMessage = React.createClass({
  propTypes: {
    peer: React.PropTypes.object.isRequired,
    message: React.PropTypes.object.isRequired
  },
  render: function() {
    var peer = this.props.peer;
    var message = this.props.message;
    var onChange = function (isVisible) {
      if (isVisible) {
        window.messenger.onMessageShown(peer, message.sortKey, message.isOut);
      }
    };

    return(
      <VisibilitySensor onChange={onChange}>
        <div className="messages-list__item">
        <Avatar sender={message.sender}/>
        <Message message={message}/>
        </div>
      </VisibilitySensor>
    );
  }
});

angular
  .module('actorWeb')
  .factory('Messages', ['$filter', function($filter) {
  return React.createClass({
    propTypes: {
      peer: React.PropTypes.object.isRequired,
      messages: React.PropTypes.array.isRequired
    },
    render: function() {
      var peer = this.props.peer;

      return (
        <div>
          {
            this.props.messages.map(function(message) {
              return (
                <ChatMessage key={message.sortKey} peer={peer} message={message}/>
              );
            })
          }
        </div>
      );
    }
  });
}]);

module.exports = ChatMessage;