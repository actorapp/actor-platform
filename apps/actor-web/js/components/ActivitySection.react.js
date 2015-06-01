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

        if (data.isContact == false) {
          addToContacts = <a onClick={this._addToContacts} className="button">Add to contacts</a>;
        } else {
          addToContacts = <a onClick={this._removeFromContacts} className="button">Remove from contacts</a>;
        }

        activityHeader =
          <header className="activity__header">
            <a className="activity__header__close material-icons" onClick={this._setActivityClosed}>clear</a>
            <span className="activity__header__title">User information</span>
          </header>;

        activityBody =
          <div className="activity__body">
            <AvatarItem title={data.name}
                        image={data.avatar}
                        placeholder={data.placeholder}
                        size="huge"/>

            <h3>{data.name}</h3>

            <ul className="activity__body__list activity__body__list--info">
              <li className="row">
                <i className="material-icons">call</i>
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
            {addToContacts}
          </div>;
        break;
      case ActivityTypes.GROUP_PROFILE:
        //activityClassName = classNames(activityClassName, "activity--group");
        activityHeader =
          <header className="activity__header">
            <a className="activity__header__close material-icons" onClick={this._setActivityClosed}>clear</a>
            <span className="activity__header__title">Group information</span>
          </header>;

        activityBody =
          <div className="activity__body">
            <AvatarItem title={data.name}
                        image={data.avatar}
                        placeholder={data.placeholder}
                        size="huge"/>

            <h3>{data.name}</h3>

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
            <a className="button">Add participant</a>
            <a className="button">Leave conversation</a>
          </div>;
        break;
      default:
    }

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

  _addToContacts: function() {
    console.warn('_addToContacts');
  },

  _removeFromContacts: function() {
    console.warn('_removeFromContacts');
  },

  _onChange: function() {
    this.setState(getStateFromStores());
    this.setState({isShown: true});
  }
});

module.exports = ActivitySection;
