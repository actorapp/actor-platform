/** @jsx React.DOM */

_ = require('lodash');
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
    var message = this.props.message;
    var avatar = null;

    if (message.content.content !== 'service') {
      avatar = <Avatar sender={message.sender}/>;
    }

    return(
      <VisibilitySensor onChange={this._onChange}>
        <div className="message row">
          {avatar}
          <Message message={message}/>
        </div>
      </VisibilitySensor>
    );
  },

  _onChange: _.debounce(function (isVisible) {
    if (isVisible) {
      window.messenger.onMessageShown(this.props.peer, this.props.message.sortKey, this.props.message.isOut);
    }
  }, 30)
});

var div = React.createFactory('div');

angular
  .module('actorWeb')
  .factory('Messages', ['$filter', function($filter) {
  return React.createClass({
    _minMessageHeight: 32,
    _additionaMessagesNumber: 30,

    _lastScrolledFromBottom: 0,

    propTypes: {
      peer: React.PropTypes.object,
      messages: React.PropTypes.array.isRequired,
      typing: React.PropTypes.object.isRequired
    },

    getInitialState: function() {
      return({messagesToRender: []});
    },

    render: function() {
      var peer = this.props.peer;
      var typing = null;

      if (this.props.typing !== null) {
        typing = <div className="messages-list__typing">
          <img src="assets/img/icons/ic_keyboard_control_24px.svg"></img>
          {this.props.typing}
        </div>
      }

      var chatMessages = _.map(this.state.messagesToRender, function (message) {
        return (
          <ChatMessage key={message.sortKey} peer={peer} message={message}/>
        );
      });

      return (
        <div className="messages-list" onScroll={this._onScroll}>
          {chatMessages}
          {typing}
        </div>
      );
    },

    componentWillReceiveProps: function(props) {
      this._setMessagesToRender(props.messages);
    },

    componentDidUpdate: function() {
      this._fixScroll();
    },

    _fixScroll: function() {
      if (this._lastScrolledFromBottom == 0) {
        this._scrollToBottom();
      } else {
        var self = this.getDOMNode();
        self.scrollTop = self.scrollHeight - self.clientHeight - this._lastScrolledFromBottom;
      }
    },

    _setMessagesToRender: function(messages) {
      if (messages.length > 0) {

        var self = this.getDOMNode();

        var vpHeight = self.clientHeight;

        var messagesToRender;

        if (messages.length * this._minMessageHeight > vpHeight) {
          var count;
          var vpMessagesCount = Math.round(vpHeight / this._minMessageHeight);
          if (this._lastScrolledFromBottom == 0) {
            count = vpMessagesCount + this._additionaMessagesNumber;
          } else {
            var scrolledMessagesCount = Math.round(this._lastScrolledFromBottom / this._minMessageHeight);
            count = vpMessagesCount + scrolledMessagesCount + this._additionaMessagesNumber;
          }
          messagesToRender = _.takeRight(messages, count);
        } else {
          messagesToRender = messages;
        }

        if (messagesToRender.length > 0) {
          this.setState({messagesToRender: messagesToRender});
        }
      }
    },

    _onScroll: _.debounce(function() {
      var self = this.getDOMNode();

      this._lastScrolledFromBottom = this._scrolledFromBottom();

      if (self.scrollTop < (this._minMessageHeight * 20)) {
        this._setMessagesToRender(this.props.messages);
      }
    }, 30, {maxWait: 60}),

    _scrollToBottom: function() {
      var self = this.getDOMNode();
      self.scrollTop = self.scrollHeight;
    },

    _scrolledFromBottom: function() {
      var self = this.getDOMNode();
      return(self.scrollHeight - self.scrollTop - self.clientHeight);
    },

    _isScrolledToBottom: function() {
      return(this._scrolledFromBottom() == 0);
    },

    _isScrolledToTop: function() {
      return(this.getDOMNode().scrollTop == 0);
    }
  });
}]);

module.exports = ChatMessage;
