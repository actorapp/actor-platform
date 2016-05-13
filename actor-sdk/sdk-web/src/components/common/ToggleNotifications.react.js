/*
* Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
*/

import React, { Component, PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';

class ToggleNotifications extends Component {
  static propTypes = {
    isNotificationsEnabled: PropTypes.bool.isRequired,
    onNotificationChange: PropTypes.func.isRequired
  };

  render() {
    const { isNotificationsEnabled, onNotificationChange } = this.props;

    return (
      <label htmlFor="notifications">
        <i className="material-icons icon icon--squash">notifications_none</i>
        <FormattedMessage id="notifications"/>
        <div className="switch pull-right">
          <input
            checked={isNotificationsEnabled}
            id="notifications"
            onChange={onNotificationChange}
            type="checkbox"
          />
          <label htmlFor="notifications"/>
        </div>
      </label>
    );
  }
}

export default ToggleNotifications;
