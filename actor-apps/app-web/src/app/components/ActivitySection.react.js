import React from 'react';
import { PureRenderMixin } from 'react/addons';

import ActivityActionCreators from 'actions/ActivityActionCreators';
import ActorAppConstants from 'constants/ActorAppConstants';

import ActivityStore from 'stores/ActivityStore';
import UserProfile from 'components/activity/UserProfile.react';
import GroupProfile from 'components/activity/GroupProfile.react';
import classNames from 'classnames';

const ActivityTypes = ActorAppConstants.ActivityTypes;

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
          <ActivitySection.Header close={this.setActivityClosed} title={activityTitle}/>
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

ActivitySection.Header = React.createClass({
  propTypes: {
    close: React.PropTypes.func,
    title: React.PropTypes.string
  },

  mixins: [PureRenderMixin],

  render() {
    let title = this.props.title;
    let close = this.props.close;

    var headerTitle;
    if (typeof title !== 'undefined') {
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
