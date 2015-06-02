'use strict';

var _ = require('lodash');

var React = require('react');
var PureRenderMixin = require('react/addons').addons.PureRenderMixin;

var ActorAppConstants = require('../constants/ActorAppConstants');
var ActivityTypes = ActorAppConstants.ActivityTypes;
var ActivityStore = require('../stores/ActivityStore');
var AvatarItem = require('./common/AvatarItem.react');
var UserProfile = require('./activity/UserProfile.react');
var GroupProfile = require('./activity/GroupProfile.react');
var classNames = require('classnames');

var getStateFromStores = function() {
  return({
    activity: ActivityStore.getActivity(),
    isShown: false
  })
};

var ActivitySection = React.createClass({
  getInitialState: function() {
    return (getStateFromStores());
  },

  componentDidMount: function() {
    ActivityStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    ActivityStore.removeChangeListener(this._onChange);
  },

  render: function() {
    var isShown = this.state.isShown;
    var activity = this.state.activity;

    var activityClassName = classNames('activity', {
      'activity--shown': isShown
    });

    var activityTitle;
    var activityBody;

    switch (activity.type) {
      case ActivityTypes.USER_PROFILE:
        activityTitle = "User information";
        activityBody = <UserProfile user={activity.user}/>;
        break;
      case ActivityTypes.GROUP_PROFILE:
        activityTitle = "Group information";
        activityBody = <GroupProfile group={activity.group}/>;
        break;
      default:
    }

    return (
      <section className={activityClassName}>
        <ActivitySection.Header title={activityTitle} close={this._setActivityClosed}/>
        {activityBody}
      </section>
    );
  },

  _setActivityClosed: function() {
    this.setState({isShown: false});
  },

  _onChange: function() {
    this.setState(getStateFromStores());
    this.setState({isShown: true});
  }
});

ActivitySection.Header = React.createClass({
  mixins: [PureRenderMixin],

  propTypes: {
    title: React.PropTypes.string,
    close: React.PropTypes.func
  },

  render: function() {
    var title = this.props.title;
    var close = this.props.close;

    var headerTitle;
    if (typeof title != "undefined") {
      headerTitle = <span className="activity__header__title">{title}</span>
    }

    return (
      <header className="activity__header">
        <a className="activity__header__close material-icons" onClick={close}>clear</a>
        {headerTitle}
      </header>
    );
  }
});

module.exports = ActivitySection;
