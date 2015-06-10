import React from 'react';

import _ from 'lodash';

import VisibilityStore from '../../stores/VisibilityStore';

import MessageActionCreators from '../../actions/MessageActionCreators';

import MessageItem from '../common/MessageItem.react';

var _delayed = [];

var flushDelayed = function() {
  _.forEach(_delayed, function(p) {
    MessageActionCreators.setMessageShown(p.peer, p.message);
  });

  _delayed = [];
};

var flushDelayedDebounced = _.debounce(flushDelayed, 30, 100);

export default React.createClass({
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

    return (
      <ul className="messages">
        {messages}
      </ul>
    );
  },

  _getMessagesListItem: function (message) {
    return (
      <MessageItem key={message.sortKey} peer={this.props.peer} message={message} onVisibilityChange={this._onMessageVisibilityChange}/>
    );
  },

  _onAppVisibilityChange: function() {
    if (VisibilityStore.isVisible) {
      flushDelayed();
    }
  },

  _onMessageVisibilityChange: function(message, isVisible) {
    if (isVisible) {
      _delayed.push({peer: this.props.peer, message: message});

      if (VisibilityStore.isVisible) {
        flushDelayedDebounced();
      }
    }
  }
});

