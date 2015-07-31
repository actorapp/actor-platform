import _ from 'lodash';

import React from 'react';
import { PureRenderMixin } from 'react/addons';

import DialogActionCreators from 'actions/DialogActionCreators';

import LoginStore from 'stores/LoginStore';

import AvatarItem from 'components/common/AvatarItem.react';

const GroupProfileMembers = React.createClass({
  propTypes: {
    groupId: React.PropTypes.number,
    members: React.PropTypes.array.isRequired
  },

  mixins: [PureRenderMixin],

  onClick(id) {
    DialogActionCreators.selectDialogPeerUser(id);
  },

  onKickMemberClick(groupId, userId) {
    DialogActionCreators.kickMember(groupId, userId);
  },

  render() {
    let groupId = this.props.groupId;
    let members = this.props.members;
    let myId = LoginStore.getMyId();


    let membersList = _.map(members, (member, index) => {
      let controls;
      let canKick = member.canKick;

      if (canKick === true && member.peerInfo.peer.id !== myId) {
        controls = (
          <div className="controls pull-right">
            <a onClick={this.onKickMemberClick.bind(this, groupId, member.peerInfo.peer.id)}>Kick</a>
          </div>
        );
      }

      return (
        <li className="profile__list__item row" key={index}>
          <a onClick={this.onClick.bind(this, member.peerInfo.peer.id)}>
            <AvatarItem image={member.peerInfo.avatar}
                        placeholder={member.peerInfo.placeholder}
                        title={member.peerInfo.title}/>
          </a>

          <div className="col-xs">
            <a onClick={this.onClick.bind(this, member.peerInfo.peer.id)}>
              <span className="title">
                {member.peerInfo.title}
              </span>
            </a>
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
  }
});

export default GroupProfileMembers;
