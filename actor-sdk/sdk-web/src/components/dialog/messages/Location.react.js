/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import ActorClient from '../../../utils/ActorClient';

const MAP_SIZE = '300x100';

/**
 * Class that represent a component for display location messages content
 */
class Location extends Component {
  static propTypes = {
    latitude: PropTypes.number.isRequired,
    longitude: PropTypes.number.isRequired,
    className: PropTypes.string
  };
  constructor(props) {
    super(props);

    this.handleMapClick = this.handleMapClick.bind(this);
  }

  handleMapClick(event) {
    const { latitude, longitude } = this.props;
    const linkToMap = `https://maps.google.com/maps?q=loc:${latitude},${longitude}`;

    if (ActorClient.isElectron()) {
      ActorClient.handleLinkClick(event);
    } else {
      window.open(linkToMap);
    }
  }

  render() {
    const { latitude, longitude, className } = this.props;

    return (
      <div className={className}>
        <div className="location" onClick={this.handleMapClick}>
          <img
            src={`https://maps.googleapis.com/maps/api/staticmap?center=${latitude},${longitude}&zoom=15&size=${MAP_SIZE}&scale=2&maptype=roadmap&markers=color:red%7C${latitude},${longitude}`}
            alt="Location"
          />
        </div>
      </div>
    );
  }
}

export default Location;
