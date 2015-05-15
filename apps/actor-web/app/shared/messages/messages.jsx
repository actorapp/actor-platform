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

var div = React.createFactory('div');

angular
  .module('actorWeb')
  .factory('Messages', ['$filter', function($filter) {
  return React.createClass({
    propTypes: {
      peer: React.PropTypes.object,
      messages: React.PropTypes.array.isRequired,
      typing: React.PropTypes.object.isRequired
    },

    componentDidMount: function() {
      this._scrollToBottom();
    },

    render: function() {
      var peer = this.props.peer;
      var typing = null;

      if (this.props.typing !== null) {
        typing = <div className="messages-list__typing">
          <p>
            <img src="assets/img/icons/ic_keyboard_control_24px.svg"></img>
            {this.props.typing}
          </p>
        </div>
      }

      return (
        <div onScroll={this._onScroll}>
          {
            this.props.messages.map(function (message) {
              return (
                <ChatMessage key={message.sortKey} peer={peer} message={message}/>
              );
            })
          }
          {typing}
        </div>
      );
    },

    componentDidUpdate: function() {
      this._scrollToBottom();
    },

    _onScroll: function() {
      console.error('scroll');
    },

    _scrollToBottom: function() {
      var self = this.getDOMNode();
      self.scrollTop = self.scrollHeight;
    }
  });
}]);

module.exports = ChatMessage;