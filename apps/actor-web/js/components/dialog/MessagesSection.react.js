'use strict';

var React = require('react');

var _ = require('lodash');

var MessageActionCreators = require('../../actions/MessageActionCreators');
var VisibilityStore = require('../../stores/VisibilityStore');

var VisibilitySensor = require('react-visibility-sensor');
var MessageItem = require('../common/MessageItem.react');

var _delayed = [];
var debouncedOnVisibleChange = _.debounce(function(isVisible) {
  if (isVisible) {
    if (VisibilityStore.isVisible) {
      MessageActionCreators.setMessageShown(this.props.peer, this.props.message)
    } else {
      _delayed.push({peer: this.props.peer, message: this.props.message})
    }
  }
}, 30, {maxWait: 100});

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
      _delayed.forEach(function(p) {
        MessageActionCreators.setMessageShown(p.peer, p.message)
      });

      _delayed = [];
    }
  }
});

module.exports = MessagesSection;
