'use strict';

var _ = require('lodash');
var ActorAppConstants = require('../constants/ActorAppConstants');
var ActivityTypes = ActorAppConstants.ActivityTypes;
var ActivityStore = require('../stores/ActivityStore');
var AvatarItem = require('./common/AvatarItem.react');
var classNames = require('classnames');
var PureRenderMixin = require('react/addons').addons.PureRenderMixin;
var React = require('react');

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

    var activityTitle;
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

        activityTitle = "User information";

        activityBody =
          <div className="activity__body">
            <AvatarItem title={data.name}
                        image={data.bigAvatar}
                        placeholder={data.placeholder}
                        size="huge"/>

            <h3>{data.name}</h3>

            <ActivitySection.ContactInfo phones={data.phones}/>

            {addToContacts}
          </div>;
        break;
      case ActivityTypes.GROUP_PROFILE:
        //activityClassName = classNames(activityClassName, "activity--group");
        activityTitle = "Group information";
        activityBody =
          <div className="activity__body">
            <AvatarItem title={data.name}
                        image={data.avatar}
                        placeholder={data.placeholder}
                        size="huge"/>

            <h3>{data.name}</h3>

            <ActivitySection.Members members={data.members}/>

            <a className="button">Add participant</a>
            <a className="button">Leave conversation</a>
          </div>;
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

ActivitySection.ContactInfo = React.createClass({
  mixins: [PureRenderMixin],

  propTypes: {
    phones: React.PropTypes.array.isRequired
  },

  render: function () {
    var phones = this.props.phones;

    var contactInfo = _.map(phones, function(phone) {
      return (
        <li className="row">
          <i className="material-icons">call</i>
          <div className="col-xs">
            +{phone.number}
            <span className="title">{phone.title}</span>
          </div>
        </li>
      );
    });

    return (
      <ul className="activity__body__list activity__body__list--info">
        {contactInfo}
      </ul>
    );
  }
});

ActivitySection.Members = React.createClass({
  mixins: [PureRenderMixin],

  propTypes: {
    members: React.PropTypes.array.isRequired
  },

  render: function () {
    var members = this.props.members;

    var membersList = _.map(members, function(member) {
      return (
        <li>
          <AvatarItem title={member.peerInfo.title}
                      image={member.peerInfo.avatar}
                      placeholder={member.peerInfo.placeholder}
                      size="tiny"/>
          {member.peerInfo.title}
        </li>
      );
    });

    return (
      <ul className="activity__body__list activity__body__list--users">
        {membersList}
      </ul>
    );
  }
});

module.exports = ActivitySection;
