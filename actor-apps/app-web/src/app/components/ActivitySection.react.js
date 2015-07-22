import React from 'react';
import classNames from 'classnames';
import { ActivityTypes } from 'constants/ActorAppConstants';

import ActivityActionCreators from 'actions/ActivityActionCreators';

import ActivityStore from 'stores/ActivityStore';

import ActivityHeader from 'components/activity/ActivityHeader.react';
import UserProfile from 'components/activity/UserProfile.react';
import GroupProfile from 'components/activity/GroupProfile.react';

const getStateFromStores = () => {
  return {
    activity: ActivityStore.getActivity(),
    isOpen: ActivityStore.isOpen()
  };
};

class ActivitySection extends React.Component {
  constructor(props) {
    super(props);

    this.state = getStateFromStores();

    ActivityStore.addChangeListener(this.onChange);
  }

  componentWillUnmount() {
    ActivityStore.removeChangeListener(this.onChange);
  }

  render() {
    let activity = this.state.activity;

    if (activity !== null) {
      let activityTitle;
      let activityBody;
      let activityClassName = classNames('activity', {
        'activity--shown': this.state.isOpen
      });

      switch (activity.type) {
        case ActivityTypes.USER_PROFILE:
          activityTitle = 'User information';
          activityBody = <UserProfile user={activity.user}/>;
          break;
        case ActivityTypes.GROUP_PROFILE:
          activityTitle = 'Group information';
          activityBody = <GroupProfile group={activity.group}/>;
          break;
        default:
      }

      return (
        <section className={activityClassName}>
          <ActivityHeader close={this.setActivityClosed} title={activityTitle}/>
          {activityBody}
        </section>
      );
    } else {
      return (null);
    }
  }

  setActivityClosed = () => {
    ActivityActionCreators.hide();
  }

  onChange = () => {
    this.setState(getStateFromStores());
  }
}

export default ActivitySection;
