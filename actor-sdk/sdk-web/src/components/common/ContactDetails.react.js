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
    const { nick } = this.props.peerInfo;
    if (!nick) return null;

    return (
      <li>
        <SvgIcon className="icon icon--pink" glyph="username"/>
        <span className="title">{nick}</span>
        <span className="description"><FormattedMessage id="profile.nickname"/></span>
      </li>
    );
  }

  renderPhone() {
    const { phones } = this.props.peerInfo;
    if (phones.length === 0) return null;

    return phones.map((phone, index) => {
      return (
        <li key={`p${index}`}>
          <i className="material-icons icon icon--green">call</i>
          <span className="title"><a href={'tel:+' + phone.number}>{'+' + phone.number}</a></span>
          <span className="description"><FormattedMessage id="profile.phone"/></span>
        </li>
      );
    });
  }

  renderEmail() {
    const { emails } = this.props.peerInfo;
    if (emails.length === 0) return null;

    return emails.map((email, index) => {
      return (
        <li key={`e${index}`}>
          <SvgIcon className="icon icon--blue" glyph="envelope"/>
          <span className="title"><a href={'mailto:' + email.email}>{email.email}</a></span>
          <span className="description"><FormattedMessage id="profile.email"/></span>
        </li>
      );
    });
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
