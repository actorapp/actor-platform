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
    content: PropTypes.object.isRequired,
    className: PropTypes.string
  };

  handleMapClick = (event) => {
    const { content } = this.props;
    const linkToMap = `https://maps.google.com/maps?q=loc:${content.latitude},${content.longitude}`;

    if (ActorClient.isElectron()) {
      ActorClient.handleLinkClick(event);
    } else {
      window.open(linkToMap);
    }
  };

  render() {
    const { content, className } = this.props;
    const imageSrc = `https://maps.googleapis.com/maps/api/staticmap?center=${content.latitude},${content.longitude}&zoom=15&size=${MAP_SIZE}&scale=2&maptype=roadmap&markers=color:red%7C${content.latitude},${content.longitude}`;

    return (
      <div className={className}>
        <div className="location" onClick={this.handleMapClick}>
          <img src={imageSrc} alt="Location"/>
        </div>
      </div>
    );
  }
}

export default Location;
