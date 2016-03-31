/*
* Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
*/

import React, { Component } from 'react';
import { FormattedMessage } from 'react-intl';

import SvgIcon from '../common/SvgIcon.react';

class ContactDetails extends Component {
  static propTypes = {
    peerInfo: React.PropTypes.object.isRequired
  };

  renderNickname() {
    const { peerInfo } = this.props;
    return peerInfo.nick ? (
      <li>
        <SvgIcon className="icon icon--pink" glyph="username"/>
        <span className="title">{peerInfo.nick}</span>
        <span className="description"><FormattedMessage id="profile.nickname"/></span>
      </li>
    ) : null;
  }

  renderPhone() {
    const { peerInfo } = this.props;
    return peerInfo.phones[0] ? (
      <li>
        <i className="material-icons icon icon--green">call</i>
        <span className="title"><a href={'tel:+' + peerInfo.phones[0].number}>{'+' + peerInfo.phones[0].number}</a></span>
        <span className="description"><FormattedMessage id="profile.phone"/></span>
      </li>
    ) : null;
  }

  renderEmail() {
    const { peerInfo } = this.props;
    return peerInfo.emails[0] ? (
      <li>
        <SvgIcon className="icon icon--blue" glyph="envelope"/>
        <span className="title"><a href={'mailto:' + peerInfo.emails[0].email}>{peerInfo.emails[0].email}</a></span>
        <span className="description"><FormattedMessage id="profile.email"/></span>
      </li>
    ) : null;
  }

  render() {

    return (
      <ul className="user_profile__contact_info__list">
        {this.renderNickname()}
        {this.renderPhone()}
        {this.renderEmail()}
      </ul>
    );
  }
}

export default ContactDetails;
