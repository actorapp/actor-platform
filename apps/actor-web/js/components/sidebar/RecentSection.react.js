var React = require('react');
var _ = require('lodash');
var RecentSectionItem = require('./RecentSectionItem.react');

var AvatarItem = require('../common/AvatarItem.react');

var RecentSection = React.createClass({
  getInitialState: function() {
    return({dialogs: []});
  },

  componentWillMount: function() {
    window.messenger.bindDialogs(this._setDialogs);
  },

  render: function() {
    var dialogs = _.map(this.state.dialogs, function(dialog, index) {
      return(
        <RecentSectionItem key={index} dialog={dialog}/>
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
    this.setState({dialogs: dialogs})
  }
});

module.exports = RecentSection;
