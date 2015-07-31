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
      // TODO: remove unused a/bvariant when new will complite
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
                <header>
                  <AvatarItem image={group.bigAvatar}
                              placeholder={group.placeholder}
                              size="big"
                              title={group.name}/>

                  <h3 className="group_profile__meta__title">{group.name}</h3>
                  <div className="group_profile__meta__created">
                    сreated by {admin.name}
                  </div>
                  <footer>
                    <button className="button button--blue pull-left">
                      <i className="material-icons">person_add</i>
                      Add people
                    </button>
                    <div className="dropdown  pull-right">
                      <button className="dropdown__button button button--blue">
                        <i className="material-icons">more_horiz</i>
                        More
                      </button>
                      <ul className="dropdown__menu dropdown__menu--right">
                        <li className="dropdown__menu__item">
                          <i className="material-icons">photo_camera</i>
                          Set Group Photo
                        </li>
                        <li className="dropdown__menu__item">
                          <i className="material-icons">power</i>
                          Add a Service Integration
                        </li>
                        <li className="dropdown__menu__item">
                          <i className="material-icons">mode_edit</i>
                          Edit Group
                        </li>
                        <li className="dropdown__menu__item dropdown__menu__item--light">
                          Leave Group
                        </li>
                      </ul>
                    </div>
                  </footer>
                </header>

                <div className="group_profile__meta__description hide">
                  some description here
                </div>
              </li>

              <li className="group_profile__list__item">
                <svg className="icon"
                     dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/sprite/icons.svg#notifications"/>'}}/>

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
                <i className="material-icons icon icon--gray">attach_file</i>
                Media
                <i className="material-icons pull-right">arrow_drop_down</i>
              </li>
              <li className="group_profile__list__item group_profile__members">
                <i className="material-icons icon icon--green">person_outline</i>

                {group.members.length}&nbsp;Members

                <i className="material-icons pull-right">arrow_drop_down</i>

                <div className="fold">
                  <GroupProfileMembers groupId={group.id} members={group.members}/>
                </div>
              </li>
              <li className="group_profile__list__item group_profile__integration hide">
                Token
                <i className="material-icons pull-right">arrow_drop_down</i>

                <div className="fold">
                  <div className="info info--light">
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
