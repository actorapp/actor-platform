import React from 'react';
import classNames from 'classnames';
import { ActivityTypes } from 'constants/ActorAppConstants';

//import ActivityActionCreators from 'actions/ActivityActionCreators';

import ActivityStore from 'stores/ActivityStore';

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
    const activity = this.state.activity;

    if (activity !== null) {
      const activityClassName = classNames('activity', {
        'activity--shown': this.state.isOpen
      });
      let activityBody;

      switch (activity.type) {
        case ActivityTypes.USER_PROFILE:
          activityBody = <UserProfile user={activity.user}/>;
          break;
        case ActivityTypes.GROUP_PROFILE:
          activityBody = <GroupProfile group={activity.group}/>;
          break;
        default:
      }

      return (
        <section className={activityClassName}>
          {activityBody}
        </section>
      );
    } else {
      return null;
    }
  }

  onChange = () => {
    this.setState(getStateFromStores());
  };
}

export default ActivitySection;
