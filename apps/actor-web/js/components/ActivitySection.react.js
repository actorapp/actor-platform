var React = require('react');

var ActorAppConstants = require('../constants/ActorAppConstants');
var AvatarItem = require('./common/AvatarItem.react');
var ActivityTypes = ActorAppConstants.ActivityTypes;

var ActivityStore = require('../stores/ActivityStore');

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
    var activity;
    var activityClassName = classNames('activity', {
      'activity--shown': isShown
    });
    var data = this.state.activity.data;

    switch (this.state.activity.type) {
      case ActivityTypes.USER_PROFILE:
        //activityClassName += " activity--shown";
        //activity = <span></span>; // TODO: real activity here
        activity =
          <header className="activity__header">
            <a className="activity__header__close" onClick={this._setActivityClosed}>
              <i className="material-icons">&#xE14C;</i>
            </a>
            <AvatarItem title={data.name} image={data.avatar} placeholder={data.placeholder} size="square"/>
            <span className="activity__header__title">{data.name}</span>
          </header>;
        break;
      case ActivityTypes.GROUP_PROFILE:
        //activityClassName += " activity--shown";
        activity = <span></span>; // TODO: real activity here
        break;
      default:
    }

    return (
      <section className={activityClassName}>
        {activity}
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

module.exports = ActivitySection;
