/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

const MAP_SIZE = '300x100';

export default class Geolocation extends Component {
  constructor(props) {
    super(props);
  }

  static propTypes = {
    content: PropTypes.object.isRequired,
    className: PropTypes.string
  };

  render() {
    const { content, className } = this.props;

    const imageSrc = `https://maps.googleapis.com/maps/api/staticmap?center=${content.latitude},${content.longitude}&zoom=15&size=${MAP_SIZE}&scale=2&maptype=roadmap&markers=color:red%7C${content.latitude},${content.longitude}`;
    const linkToMap = `https://maps.google.com/maps?q=loc:${content.latitude},${content.longitude}`;

    return (
      <div className={className}>
        <div className="location">
          <a href={linkToMap} target="_blank">
            <img src={imageSrc} alt="Location"/>
          </a>
        </div>
      </div>
    );
  }
}
