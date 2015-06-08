'use strict';

var _ = require('lodash');

var React = require('react');
var PureRenderMixin = require('react/addons').addons.PureRenderMixin;

var DialogActionCreators = require('../../actions/DialogActionCreators');
var LoginStore = require('../../stores/LoginStore.js');

var AvatarItem = require('../common/AvatarItem.react');

class GroupProfile extends React.Component {
  render() {
    var group = this.props.group;
    var myId = LoginStore.getMyId();

    var isAdmin = false;
    var adminControls;
    if (group.adminId == myId) {
      isAdmin = true;
      adminControls = <a className="button button--danger button--wide">Delete group</a>;
    }

    return(
      <div className="activity__body profile">
        <AvatarItem title={group.name}
                    image={group.avatar}
                    placeholder={group.placeholder}
                    size="huge"/>

        <h3 className="profile__name">{group.name}</h3>

        <GroupProfile.Members members={group.members} isAdmin={isAdmin}/>

        <footer className="profile__controls">
          <a className="button button--wide" onClick={this._onAddMemberClick}>Add member</a>
          <a className="button button--wide" onClick={this._onLeaveGroupClick}>Leave group</a>
          {adminControls}
        </footer>
      </div>
    );
  }

  _onAddMemberClick() {
    console.log("_onAddMemberClick");
  }

  _onLeaveGroupClick() {
    console.log("_onLeaveGroupClick");
  }
}

_.assign(GroupProfile, {
  propTypes: {
    group: React.PropTypes.object.isRequired
  }
});

GroupProfile.Members = React.createClass({
  mixins: [PureRenderMixin],

  propTypes: {
    members: React.PropTypes.array.isRequired,
    isAdmin: React.PropTypes.bool
  },

  render: function () {
    var members = this.props.members;
    var isAdmin = this.props.isAdmin;

    var membersList = _.map(members, function(member, index) {
      var controls;
      if (isAdmin == true) {
        controls = <a className="material-icons">clear</a>;
      }

      return (
        <li key={index} className="profile__list__item row">
          <a onClick={this._onClick.bind(this, member.peerInfo.peer.id)}>
            <AvatarItem title={member.peerInfo.title}
                      image={member.peerInfo.avatar}
                      placeholder={member.peerInfo.placeholder}
                      size="tiny"/>
          </a>

          <div className="col-xs">
            <a onClick={this._onClick.bind(this, member.peerInfo.peer.id)}>
              <span className="title">
                {member.peerInfo.title}
              </span>
            </a>
          </div>

          <div className="controls">
            {controls}
          </div>
        </li>
      );
    }, this);

    return (
      <ul className="profile__list profile__list--members">
        {membersList}
      </ul>
    );
  },

  _onClick: function(id) {
    DialogActionCreators.selectDialogPeerUser(id);
  }

});

module.exports = GroupProfile;
