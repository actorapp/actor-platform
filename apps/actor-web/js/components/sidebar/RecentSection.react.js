var React = require('react');
var _ = require('lodash');

var DialogsStore = require('../../stores/DialogStore.react');

var RecentSectionItem = require('./RecentSectionItem.react');
var AvatarItem = require('../common/AvatarItem.react');

var getStateFromStore = function() {
  return({dialogs: DialogsStore.getAll()});
};

var RecentSection = React.createClass({
  getInitialState: function() {
    return(getStateFromStore());
  },

  componentWillMount: function() {
    DialogsStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    DialogsStore.removeChangeListener(this._onChange);
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

  _onChange: function() {
    this.setState(getStateFromStore());
  }
});

module.exports = RecentSection;
