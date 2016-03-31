/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { FormattedHTMLMessage } from 'react-intl'
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';

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

  constructor(props, context) {
    super(props, context);

    this.onInviteClick = this.onInviteClick.bind(this);
    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
  }

  onInviteClick() {
    const { peer } = this.props;
    const group = GroupStore.getGroup(peer.id);
    InviteUserActions.show(group);
  }

  renderUserText(id) {
    const user = UserStore.getUser(id);
    return (
      <FormattedHTMLMessage id="message.welcome.private" values={{name: user.name}}/>
    );
  }

  renderGroupText(id) {
    const { intl } = this.context;
    const group = GroupStore.getGroup(id);
    const myID = UserStore.getMyId();
    const admin = UserStore.getUser(group.adminId);
    const creator = group.adminId === myID ? intl.messages['message.welcome.group.you'] : admin.name;

    return (
      <div>
        <FormattedHTMLMessage id="message.welcome.group.main" values={{name: group.name, creator}}/>
        <p key={2}>
          {intl.messages['message.welcome.group.actions.start']}
          <a onClick={this.onInviteClick}>{intl.messages['message.welcome.group.actions.invite']}</a>
          {intl.messages['message.welcome.group.actions.end']}
        </p>
      </div>
    );
  }

  renderText() {
    const { peer } = this.props;

    switch (peer.type) {
      case PeerTypes.USER:
        return this.renderUserText(peer.id);
      case PeerTypes.GROUP:
        return this.renderGroupText(peer.id);
    }
  }

  render() {
    return(
      <div className="message message--welcome row">
        <div className="message__info">
          <div className="welcome-avatar">
            <SvgIcon className="icon icon--gray" glyph="star" />
          </div>
        </div>
        <div className="message__body col-xs">
          {this.renderText()}
        </div>
      </div>
    )
  }
}

export default Welcome;
