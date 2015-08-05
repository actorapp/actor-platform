import React from 'react';

//import { Experiment, Variant } from 'react-ab';
//import mixpanel from 'utils/Mixpanel';
import ReactMixin from 'react-mixin';

import { IntlMixin, FormattedMessage } from 'react-intl';

import DialogActionCreators from 'actions/DialogActionCreators';
import GroupProfileActionCreators from 'actions/GroupProfileActionCreators';

import LoginStore from 'stores/LoginStore';
import PeerStore from 'stores/PeerStore';
import DialogStore from 'stores/DialogStore';
import GroupStore from 'stores/GroupStore';
import InviteUserActions from 'actions/InviteUserActions';

import AvatarItem from 'components/common/AvatarItem.react';
import InviteUser from 'components/modals/InviteUser.react';
import InviteByLink from 'components/modals/invite-user/InviteByLink.react';
import GroupProfileMembers from 'components/activity/GroupProfileMembers.react';
import Fold from 'components/common/Fold.React';

const getStateFromStores = (groupId) => {
  const thisPeer = PeerStore.getGroupPeer(groupId);
  return {
    thisPeer: thisPeer,
    isNotificationsEnabled: DialogStore.isNotificationsEnabled(thisPeer),
    integrationToken: GroupStore.getIntegrationToken()
  };
};

@ReactMixin.decorate(IntlMixin)
class GroupProfile extends React.Component {
  static propTypes = {
    group: React.PropTypes.object.isRequired
  };

  constructor(props) {
    super(props);

    this.state = getStateFromStores(props.group.id);

    DialogStore.addNotificationsListener(this.onChange);
    GroupStore.addChangeListener(this.onChange);
  }

  componentWillUnmount() {
    DialogStore.removeNotificationsListener(this.onChange);
    GroupStore.addChangeListener(this.onChange);
  }

  componentWillReceiveProps(newProps) {
    this.setState(getStateFromStores(newProps.group.id));
  }

  onAddMemberClick = group => {
    InviteUserActions.show(group);
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

  selectToken = (event) => {
    event.target.select();
  };

  render() {
    const group = this.props.group;
    const myId = LoginStore.getMyId();
    const isNotificationsEnabled = this.state.isNotificationsEnabled;
    const integrationToken = this.state.integrationToken;
    const admin = GroupProfileActionCreators.getUser(group.adminId);

    let adminControls;
    if (group.adminId === myId) {
      adminControls = [
        <li className="dropdown__menu__item hide">
          <i className="material-icons">photo_camera</i>
          <FormattedMessage message={this.getIntlMessage('setGroupPhoto')}/>
        </li>
      ,
        <li className="dropdown__menu__item hide">
          <svg className="icon icon--dropdown"
               dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/sprite/icons.svg#integration"/>'}}/>
          <FormattedMessage message={this.getIntlMessage('addIntegration')}/>
        </li>
      ,
        <li className="dropdown__menu__item">
          <i className="material-icons">mode_edit</i>
          <FormattedMessage message={this.getIntlMessage('editGroup')}/>
        </li>
      ,
        <li className="dropdown__menu__item">
          <FormattedMessage message={this.getIntlMessage('deleteGroup')}/>
        </li>
      ];
    }

    let members = <FormattedMessage message={this.getIntlMessage('members')} numMembers={group.members.length}/>;

    return (
      <div className="activity__body group_profile">
        <ul className="profile__list">
          <li className="profile__list__item group_profile__meta">
            <header>
              <AvatarItem image={group.bigAvatar}
                          placeholder={group.placeholder}
                          size="big"
                          title={group.name}/>

              <h3 className="group_profile__meta__title">{group.name}</h3>
              <div className="group_profile__meta__created">
                <FormattedMessage admin={admin.name} message={this.getIntlMessage('createdBy')}/>
              </div>
            </header>

            <div className="group_profile__meta__description hide">
              Description here
            </div>

            <footer>
              <button className="button button--blue pull-left" onClick={this.onAddMemberClick.bind(this, group)}>
                <i className="material-icons">person_add</i>
                <FormattedMessage message={this.getIntlMessage('addPeople')}/>
              </button>
              <div className="dropdown pull-right">
                <button className="dropdown__button button button--blue">
                  <i className="material-icons">more_horiz</i>
                  <FormattedMessage message={this.getIntlMessage('more')}/>
                </button>
                <ul className="dropdown__menu dropdown__menu--right">
                  {adminControls}
                  <li className="dropdown__menu__item dropdown__menu__item--light"
                      onClick={this.onLeaveGroupClick.bind(this, group.id)}>
                    <FormattedMessage message={this.getIntlMessage('leaveGroup')}/>
                  </li>
                </ul>
              </div>
            </footer>
          </li>

          <li className="profile__list__item group_profile__media no-p hide">
            <Fold icon="attach_file" iconClassName="icon--gray" title={this.getIntlMessage('sharedMedia')}>
              <ul>
                <li><a>230 Shared Photos and Videos</a></li>
                <li><a>49 Shared Links</a></li>
                <li><a>49 Shared Files</a></li>
              </ul>
            </Fold>
          </li>

          <li className="profile__list__item group_profile__notifications no-p">
            <label htmlFor="notifications">
              <i className="material-icons icon icon--squash">notifications_none</i>
              <FormattedMessage message={this.getIntlMessage('notifications')}/>
              <div className="switch pull-right">
                <input checked={isNotificationsEnabled}
                       id="notifications"
                       onChange={this.onNotificationChange}
                       type="checkbox"/>
                <label htmlFor="notifications"></label>
              </div>
            </label>
          </li>

          <li className="profile__list__item group_profile__members no-p">
            <Fold icon="person_outline" iconClassName="icon--green" title={members}>
              <GroupProfileMembers groupId={group.id} members={group.members}/>
            </Fold>
          </li>

          <li className="profile__list__item group_profile__integration no-p">
            <Fold icon="power" iconClassName="icon--pink" title="Integration Token">
              <div className="info info--light">
                If you have programming chops, or know someone who does,
                this integration token allow the most flexibility and communication
                with your own systems.
                <a href="https://actor.readme.io/docs/simple-integration" target="_blank">Learn how to integrate</a>
              </div>
              <textarea className="token" onClick={this.selectToken} readOnly row="3" value={integrationToken}/>
            </Fold>
          </li>
        </ul>

        <InviteUser/>
        <InviteByLink/>
      </div>
    );
  }
}

export default GroupProfile;
