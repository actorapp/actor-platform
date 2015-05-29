var React = require('react');

var ActorAppConstants = require('../constants/ActorAppConstants');
var ActivityTypes = ActorAppConstants.ActivityTypes;

var ActivityStore = require('../stores/ActivityStore');

var getStateFromStores = function() {
  return({
    activity: ActivityStore.getActivity()
  })
};

var ActivitySection = React.createClass({
  getInitialState: function() {
    return(getStateFromStores())
  },

  componentDidMount: function() {
    ActivityStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    ActivityStore.removeChangeListener(this._onChange);
  },

  render: function() {
    var activity;

    switch (this.state.activity.type) {
      case ActivityTypes.USER_PROFILE:
        activity = <span></span>; // TODO: real activity here
        break;
      case ActivityTypes.GROUP_PROFILE:
        activity = <span></span>; // TODO: real activity here
        break;
      default:
    }

    return(<div>{activity}</div>)
  },

  _onChange: function() {
    this.setState(getStateFromStores());
  }
});

module.exports = ActivitySection;
