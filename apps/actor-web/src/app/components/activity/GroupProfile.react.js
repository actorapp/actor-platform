import _ from 'lodash';

import React from 'react';
import { PureRenderMixin } from 'react/addons';

import DialogActionCreators from '../../actions/DialogActionCreators';

import LoginStore from '../../stores/LoginStore';
import InviteUserActions from '../../actions/InviteUserActions';

import AvatarItem from '../common/AvatarItem.react';
import InviteUser from '../modals/InviteUser.react';

class GroupProfile extends React.Component {
  render() {
    var group = this.props.group;
    var myId = LoginStore.getMyId();

    var adminControls;
    if (group.adminId === myId) {
      adminControls = <a className="button button--danger button--wide hide">Delete group</a>;
    }

    return (
      <div className="activity__body profile">
        <AvatarItem title={group.name}
                    image={group.avatar}
                    placeholder={group.placeholder}
                    size="huge"/>

        <h3 className="profile__name">{group.name}</h3>

        <GroupProfile.Members members={group.members} groupId={group.id}/>

        <footer className="profile__controls">
          <a className="button button--wide" onClick={this._onAddMemberClick.bind(this, group)}>Add member</a>
          <a className="button button--wide" onClick={this._onLeaveGroupClick.bind(this, group.id)}>Leave group</a>
          {adminControls}
        </footer>

        <InviteUser/>
      </div>
    );
  }

  _onAddMemberClick(group) {
    InviteUserActions.modalOpen(group);
  }

  _onLeaveGroupClick(groupId) {
    DialogActionCreators.leaveGroup(groupId);
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
    groupId: React.PropTypes.number
  },

  render() {
    var members = this.props.members;
    var groupId = this.props.groupId;
    var myId = LoginStore.getMyId();


    var membersList = _.map(members, function(member, index) {
      var controls;
      var canKick = member.canKick;

      if (canKick === true && member.peerInfo.peer.id !== myId) {
        controls = <a className="material-icons" onClick={this._onKickMemberClick.bind(this, groupId, member.peerInfo.peer.id)}>clear</a>;
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

  _onClick(id) {
    DialogActionCreators.selectDialogPeerUser(id);
  },

  _onKickMemberClick(groupId, userId) {
    DialogActionCreators.kickMember(groupId, userId);
  }

});

export default GroupProfile;
