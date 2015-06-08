var React = require('react');
var _ = require('lodash');

var DialogStore = require('../../stores/DialogStore');

var RecentSectionItem = require('./RecentSectionItem.react');
var AvatarItem = require('../common/AvatarItem.react');

var getStateFromStore = function() {
  return({
    dialogs: DialogStore.getAll()
  });
};

var RecentSection = React.createClass({
  getInitialState: function() {
    return(getStateFromStore());
  },

  componentWillMount: function() {
    DialogStore.addChangeListener(this._onChange);
    DialogStore.addSelectListener(this._onChange);
  },

  componentWillUnmount: function() {
    DialogStore.removeChangeListener(this._onChange);
    DialogStore.removeSelectListener(this._onChange);
  },

  render: function() {
    var dialogs = _.map(this.state.dialogs, function(dialog, index) {
      return(
        <RecentSectionItem key={index} dialog={dialog}/>
      )
    }, this);

    return(
      <ul className="sidebar__list">
        {dialogs}
      </ul>
    );
  },

  _onChange: function() {
    this.setState(getStateFromStore());
  }
});

module.exports = RecentSection;
