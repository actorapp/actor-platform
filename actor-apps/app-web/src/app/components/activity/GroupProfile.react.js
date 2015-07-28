import React from 'react';

import { Experiment, Variant } from 'react-ab';
import mixpanel from 'utils/Mixpanel';

import DialogActionCreators from 'actions/DialogActionCreators';
import GroupProfileActionCreators from 'actions/GroupProfileActionCreators';

import LoginStore from 'stores/LoginStore';
import PeerStore from 'stores/PeerStore';
import DialogStore from 'stores/DialogStore';
import GroupStore from 'stores/GroupStore';
import InviteUserActions from 'actions/InviteUserActions';

import AvatarItem from 'components/common/AvatarItem.react';
import InviteUser from 'components/modals/InviteUser.react';
import GroupProfileMembers from 'components/activity/GroupProfileMembers.react';

const getStateFromStores = (groupId) => {
  const thisPeer = PeerStore.getGroupPeer(groupId);
  return {
    thisPeer: thisPeer,
    isNotificationsEnabled: DialogStore.isNotificationsEnabled(thisPeer),
    integrationToken: GroupStore.getIntegrationToken()
  };
};

class GroupProfile extends React.Component {
  static propTypes = {
    group: React.PropTypes.object.isRequired
  };

  constructor(props) {
    super(props);

    DialogStore.addNotificationsListener(this.onChange);
    GroupStore.addChangeListener(this.onChange);

    this.state = getStateFromStores(props.group.id);
  }

  componentWillUnmount() {
    DialogStore.removeNotificationsListener(this.onChange);
    GroupStore.addChangeListener(this.onChange);
  }

  componentWillReceiveProps(newProps) {
    this.setState(getStateFromStores(newProps.group.id));
  }

  onAddMemberClick = group => {
    InviteUserActions.modalOpen(group);
  };

  onLeaveGroupClick = groupId => {
    DialogActionCreators.leaveGroup(groupId);
  };

  onNotificationChange = event => {
    DialogActionCreators.changeNotificationsEnabled(this.state.thisPeer, event.target.checked);
  };

  onChange = () => {
    this.setState(getStateFromStores(this.props.group.id));
  };

  onChoice = (experiment, variant) => {
    mixpanel.register({'tagline': variant});
  };

  render() {
    const group = this.props.group;
    const myId = LoginStore.getMyId();
    const isNotificationsEnabled = this.state.isNotificationsEnabled;
    const integrationToken = this.state.integrationToken;
    const admin = GroupProfileActionCreators.getUser(group.adminId);

    let memberArea,
        adminControls;

    if (group.adminId === myId) {
      adminControls = (
        <li className="profile__list__item">
          <a className="red">Delete group</a>
        </li>
      );
    }

    if (DialogStore.isGroupMember(group)) {
      memberArea = (
        <div>
          <GroupProfileMembers groupId={group.id} members={group.members}/>

          <ul className="profile__list profile__list--controls">
            <li className="profile__list__item">
              <a className="link__blue" onClick={this.onAddMemberClick.bind(this, group)}>Add member</a>
            </li>
            <li className="profile__list__item">
              <a className="link__red" onClick={this.onLeaveGroupClick.bind(this, group.id)}>Leave group</a>
            </li>
            {adminControls}
          </ul>

          <InviteUser/>
        </div>
      );
    }

    return (
      <Experiment name="groupInfo" onChoice={this.onChoice}>
        <Variant name="old">
          <div className="activity__body profile">
            <div className="profile__name">
              <AvatarItem image={group.bigAvatar}
                          placeholder={group.placeholder}
                          size="medium"
                          title={group.name}/>
              <h3>{group.name}</h3>
            </div>
            <div className="notifications">
              <label htmlFor="notifications">Enable Notifications</label>

              <div className="switch pull-right">
                <input checked={isNotificationsEnabled} id="notifications" onChange={this.onNotificationChange} type="checkbox"/>
                <label htmlFor="notifications"></label>
              </div>
            </div>

            {memberArea}
            {integrationToken}
          </div>
        </Variant>
        <Variant name="new">
          <div className="activity__body group_profile">
            <ul className="group_profile__list">
              <li className="group_profile__list__item group_profile__meta">
                <AvatarItem image={group.bigAvatar}
                            placeholder={group.placeholder}
                            title={group.name}/>

                <h3 className="group_profile__meta__title">{group.name}</h3>

                <div className="info">
                  <p>some description heere</p>
                </div>
                <div className="info--light">
                  Created by {admin.name} {/*on Month Day, Year*/}
                </div>

              </li>
              <li className="group_profile__list__item">
                <label htmlFor="notifications">Notifications</label>
                <div className="switch pull-right">
                  <input checked={isNotificationsEnabled}
                         id="notifications"
                         onChange={this.onNotificationChange}
                         type="checkbox"/>
                  <label htmlFor="notifications"></label>
                </div>
              </li>
              <li className="group_profile__list__item group_profile__media">
                Media
                <i className="material-icons pull-right">keyboard_arrow_down</i>
              </li>
              <li className="group_profile__list__item group_profile__members">
                Members
                <i className="material-icons pull-right">keyboard_arrow_down</i>

                <div className="fold">
                  {memberArea}
                </div>
              </li>
              <li className="group_profile__list__item group_profile__integration">
                Token
                <i className="material-icons pull-right">keyboard_arrow_down</i>

                <div className="fold">
                  <div className="info--light">
                    If you have programming chops, or know someone who does,
                    this integration token allow the most flexibility and communication
                    with your own systems.
                  </div>
                  {integrationToken}
                </div>
              </li>
            </ul>
          </div>
        </Variant>
      </Experiment>
    );
  }
}

export default GroupProfile;
