'use strict';

var React = require('react');

var _ = require('lodash');

var MessageActionCreators = require('../../actions/MessageActionCreators');
var VisibilityStore = require('../../stores/VisibilityStore');

var VisibilitySensor = require('react-visibility-sensor');
var MessageItem = require('../common/MessageItem.react');

var _delayed = [];

var flushDelayed = function() {
  _.forEach(_delayed, function(p) {
    MessageActionCreators.setMessageShown(p.peer, p.message)
  });

  _delayed = [];
};

var flushDelayedDebounced = _.debounce(flushDelayed, 30, 100);

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

  _onVisibilityChange: function(isVisible) {
    if (isVisible) {
      _delayed.push({peer: this.props.peer, message: this.props.message});

      if (VisibilityStore.isVisible) {
        flushDelayedDebounced();
      }
    }
  }
});

var MessagesSection = React.createClass({
  propTypes: {
    messages: React.PropTypes.array.isRequired,
    peer: React.PropTypes.object.isRequired
  },

  componentDidMount: function() {
    VisibilityStore.addChangeListener(this._onAppVisibilityChange);
  },

  componentWillUnmount: function() {
    VisibilityStore.removeChangeListener(this._onAppVisibilityChange);
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
  },

  _onAppVisibilityChange: function() {
    if (VisibilityStore.isVisible) {
      flushDelayed()
    }
  }
});

module.exports = MessagesSection;
