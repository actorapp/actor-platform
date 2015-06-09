import React from 'react';
import { PureRenderMixin } from 'react/addons';

import ActivityActionCreators from '../actions/ActivityActionCreators';
import ActorAppConstants from '../constants/ActorAppConstants';
var ActivityTypes = ActorAppConstants.ActivityTypes;
import ActivityStore from '../stores/ActivityStore';
import UserProfile from './activity/UserProfile.react';
import GroupProfile from './activity/GroupProfile.react';
import classNames from 'classnames';

var getStateFromStores = function() {
  return {
    activity: ActivityStore.getActivity()
  };
};

var ActivitySection = React.createClass({
  getInitialState: function() {
    return getStateFromStores();
  },

  componentDidMount: function() {
    ActivityStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    ActivityStore.removeChangeListener(this._onChange);
  },

  render: function() {
    var activity = this.state.activity;

    if (activity !== null) {
      var activityTitle;
      var activityBody;
      var activityClassName = classNames('activity', {
        'activity--shown': true
      });

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
    } else {
      return (null);
    }
  },

  _setActivityClosed: function() {
    ActivityActionCreators.hide();
  },

  _onChange: function() {
    this.setState(getStateFromStores());
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
    if (typeof title !== "undefined") {
      headerTitle = <span className="activity__header__title">{title}</span>;
    }

    return (
      <header className="activity__header toolbar">
        <a className="activity__header__close material-icons" onClick={close}>clear</a>
        {headerTitle}
      </header>
    );
  }
});

export default ActivitySection;
