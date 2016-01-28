/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedHTMLMessage } from 'react-intl'
import PureRenderMixin from 'react-addons-pure-render-mixin';

import { PeerTypes } from '../../../constants/ActorAppConstants';

import InviteUserActions from '../../../actions/InviteUserActions';

import UserStore from '../../../stores/UserStore';
import GroupStore from '../../../stores/GroupStore';

class Welcome extends Component {
  constructor(props) {
    super(props);
  }

  static propTypes = {
    peer: PropTypes.object.isRequired
  };

  render() {
    const { peer } = this.props;

    let welcomeText;
    switch (peer.type) {
      case PeerTypes.USER:
        const user = UserStore.getUser(peer.id);
        welcomeText = <FormattedHTMLMessage message={this.getIntlMessage('message.welcome.private')} name={user.name}/>;
        break;
      case PeerTypes.GROUP:
        const group = GroupStore.getGroup(peer.id);
        const myID = UserStore.getMyId();
        const admin = UserStore.getUser(group.adminId);
        const creator = group.adminId === myID ? this.getIntlMessage('message.welcome.group.you') : admin.name;
        welcomeText = [
          <FormattedHTMLMessage message={this.getIntlMessage('message.welcome.group.main')}
                                name={group.name}
                                creator={creator}/>
        ,
          <p>
            {this.getIntlMessage('message.welcome.group.actions.start')}
            <a onClick={() => InviteUserActions.show(group)}>{this.getIntlMessage('message.welcome.group.actions.invite')}</a>
            {this.getIntlMessage('message.welcome.group.actions.end')}
          </p>
        ];
        break;
    }

    return(
      <li className="message message--welcome row">
        <div className="message__info">
          <div className="welcome-avatar">
            <svg className="icon icon--gray"
                 dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/images/icons.svg#star"/>'}}/>
          </div>
        </div>
        <div className="message__body col-xs">
          {welcomeText}
        </div>
      </li>
    )
  }
}

ReactMixin.onClass(Welcome, IntlMixin);
ReactMixin.onClass(Welcome, PureRenderMixin);

export default Welcome;
