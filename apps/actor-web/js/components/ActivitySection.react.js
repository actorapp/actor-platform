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
    var activity = this.state.activity;
    var data = activity.data;

    var activityClassName = classNames('activity', {
      'activity--shown': isShown
    });

    var activityHeader;
    var activityBody;

    switch (activity.type) {
      case ActivityTypes.USER_PROFILE:
        var addToContacts;
        //activityClassName = classNames(activityClassName, "activity--user");
        activityHeader = this._setActivityHeader(data);

        if (data.isContact == false) {
          addToContacts = <a onClick={this._addToContacts} className="button">Add to contacts</a>;
        } else {
          addToContacts = <a onClick={this._addToContacts} className="button">Remove from contacts</a>;
        }

        activityBody =
          <div className="activity__body">
            <ul className="activity__body__list activity__body__list--info">
              <li className="row">
                <i className="material-icons">smartphone</i>
                <div className="col-xs">
                  +75555555555
                  <span className="info">Mobile phone</span>
                </div>
              </li>
              <li className="row">
                <i className="material-icons">email</i>
                <div className="col-xs">
                  <a href="mailto:someone@domain.com">someone@domain.com</a>
                  <span className="info">Work email</span>
                </div>
              </li>
            </ul>
            <hr/>
            {addToContacts}
          </div>;
        break;
      case ActivityTypes.GROUP_PROFILE:
        //activityClassName = classNames(activityClassName, "activity--group");
        activityHeader = this._setActivityHeader(data);
        activityBody =
          <div className="activity__body">
            <ul className="activity__body__list activity__body__list--users">
              <li>
                <AvatarItem title={data.name}
                            image={data.avatar}
                            placeholder={data.placeholder}
                            size="tiny"/>
                {data.name}
              </li>
              <li>
                <AvatarItem title={data.name}
                            image={data.avatar}
                            placeholder={data.placeholder}
                            size="tiny"/>
                {data.name}
              </li>
              <li>
                <AvatarItem title={data.name}
                            image={data.avatar}
                            placeholder={data.placeholder}
                            size="tiny"/>
                {data.name}
              </li>
              <li>
                <AvatarItem title={data.name}
                            image={data.avatar}
                            placeholder={data.placeholder}
                            size="tiny"/>
                {data.name}
              </li>
            </ul>
            <hr/>
            <a className="button">Add participant</a>
            <a className="button">Leave conversation</a>
          </div>;
        break;
      default:
    }

    console.warn(data);

    return (
      <section className={activityClassName}>
        {activityHeader}
        {activityBody}
      </section>
    );
  },

  _setActivityClosed: function() {
    this.setState({isShown: false});
  },

  _setActivityHeader: function(data) {
    return (
      <header className="activity__header">
        <AvatarItem title={data.name}
                    image={data.avatar}
                    placeholder={data.placeholder}
                    size="square"/>

        <a className="activity__header__close" onClick={this._setActivityClosed}>
          <i className="material-icons">clear</i>
        </a>
        <span className="activity__header__title">{data.name}</span>
      </header>
    );
  },

  _addToContacts: function() {

  },

  _onChange: function() {
    this.setState(getStateFromStores());
    this.setState({isShown: true});
  }
});

module.exports = ActivitySection;
