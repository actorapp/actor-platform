var React = require('react');
var _ = require('lodash');

var AvatarItem = require('../common/AvatarItem.react');
var ConversationActionCreators = require('../../actions/ConversationActionCreators');

var RecentSection = React.createClass({
  propTypes: {
    messenger: React.PropTypes.object.isRequired
  },

  getInitialState: function() {
    return({dialogs: []});
  },

  componentWillMount: function() {
    var messenger = this.props.messenger;

    messenger.bindDialogs(this._setDialogs);
  },

  render: function() {
    var dialogs = _.map(this.state.dialogs, function(dialog, index) {
      return(
        <li key={index}
            className="sidebar__list__item"
            onClick={this._selectPeer}>
          <AvatarItem title={dialog.peer.title}
                      image={dialog.peer.avatar}
                      placeholder={dialog.peer.placeholder}
                      size="tiny"/>
          <span>{dialog.peer.title}</span>
        </li>
      )
    }, this);

    return(
      <ul className="sidebar__list">
        <li className="sidebar__list__title">
          Recent
        </li>
        {dialogs}
      </ul>
    );
  },

  _setDialogs: function(dialogs) {
    window.di = dialogs;
    this.setState({dialogs: dialogs})
  },

  _selectPeer: function() {
    ConversationActionCreators.selectPeer();
  }
});

module.exports = RecentSection;
