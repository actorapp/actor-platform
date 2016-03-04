/*
* Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
*/

import React, { Component, PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';

class ContactDetails extends Component {
  static propTypes = {
    user: React.PropTypes.object.isRequired
  };

  renderNickname() {
    const { user } = this.props;
    return user.nick ? (
      <li>
        <svg className="icon icon--pink"
             dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/images/icons.svg#username"/>'}}/>
        <span className="title">{user.nick}</span>
        <span className="description"><FormattedMessage id="profile.nickname"/></span>
      </li>
    ) : null;
  }

  renderPhone() {
    const { user } = this.props;
    return user.phones[0] ? (
      <li>
        <i className="material-icons icon icon--green">call</i>
        <span className="title"><a href={'tel:+' + user.phones[0].number}>{'+' + user.phones[0].number}</a></span>
        <span className="description"><FormattedMessage id="profile.phone"/></span>
      </li>
    ) : null;
  }

  renderEmail() {
    const { user } = this.props;
    return user.emails[0] ? (
      <li>
        <svg className="icon icon--blue"
             dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/images/icons.svg#envelope"/>'}}/>
        <span className="title"><a href={'mailto:' + user.emails[0].email}>{user.emails[0].email}</a></span>
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
