/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import ReactMixin from 'react-mixin';
import { FormattedHTMLMessage } from 'react-intl'
import PureRenderMixin from 'react-addons-pure-render-mixin';

import SvgIcon from '../../common/SvgIcon.react';

import { PeerTypes } from '../../../constants/ActorAppConstants';

import InviteUserActions from '../../../actions/InviteUserActions';

import UserStore from '../../../stores/UserStore';
import GroupStore from '../../../stores/GroupStore';

class Welcome extends Component {
  static propTypes = {
    peer: PropTypes.object.isRequired
  };

  static contextTypes = {
    intl: PropTypes.object
  };

  render() {
    const { peer } = this.props;
    const { intl } = this.context;

    let welcomeText;
    switch (peer.type) {
      case PeerTypes.USER:
        const user = UserStore.getUser(peer.id);
        welcomeText = <FormattedHTMLMessage id="message.welcome.private" values={{name: user.name}}/>;
        break;
      case PeerTypes.GROUP:
        const group = GroupStore.getGroup(peer.id);
        const myID = UserStore.getMyId();
        const admin = UserStore.getUser(group.adminId);
        const creator = group.adminId === myID ? intl.messages['message.welcome.group.you'] : admin.name;
        welcomeText = [
          <FormattedHTMLMessage id="message.welcome.group.main" key={1}
                                values={{name: group.name, creator}}/>
        ,
          <p key={2}>
            {intl.messages['message.welcome.group.actions.start']}
            <a onClick={() => InviteUserActions.show(group)}>{intl.messages['message.welcome.group.actions.invite']}</a>
            {intl.messages['message.welcome.group.actions.end']}
          </p>
        ];
        break;
    }

    return(
      <div className="message message--welcome row">
        <div className="message__info">
          <div className="welcome-avatar">
            <SvgIcon className="icon icon--gray" glyph="star" />
          </div>
        </div>
        <div className="message__body col-xs">
          {welcomeText}
        </div>
      </div>
    )
  }
}

ReactMixin.onClass(Welcome, PureRenderMixin);

export default Welcome;
