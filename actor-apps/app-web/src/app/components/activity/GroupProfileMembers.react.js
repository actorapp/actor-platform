import _ from 'lodash';

import React from 'react';
import ReactMixin from 'react-mixin';
import addons from 'react/addons';

import DialogActionCreators from 'actions/DialogActionCreators';

import LoginStore from 'stores/LoginStore';

import AvatarItem from 'components/common/AvatarItem.react';

const {addons: { PureRenderMixin }} = addons;

@ReactMixin.decorate(PureRenderMixin)
class GroupProfileMembers extends React.Component {
  static propTypes = {
    groupId: React.PropTypes.number,
    members: React.PropTypes.array.isRequired
  };

  constructor(props) {
    super(props);
  }

  onClick(id) {
    DialogActionCreators.selectDialogPeerUser(id);
  }

  onKickMemberClick(groupId, userId) {
    DialogActionCreators.kickMember(groupId, userId);
  }

  render() {
    const groupId = this.props.groupId;
    const members = this.props.members;
    const myId = LoginStore.getMyId();

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
        <li className="group_profile__members__list__item" key={index}>
          <a onClick={this.onClick.bind(this, member.peerInfo.peer.id)}>
            <AvatarItem image={member.peerInfo.avatar}
                        placeholder={member.peerInfo.placeholder}
                        title={member.peerInfo.title}/>
          </a>

          <a onClick={this.onClick.bind(this, member.peerInfo.peer.id)}>
            {member.peerInfo.title}
          </a>

          {controls}
        </li>
      );
    }, this);

    return (
        <ul className="group_profile__members__list">
          {membersList}
        </ul>
    );
  }
}

export default GroupProfileMembers;
