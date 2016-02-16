/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import FaviconStore from '../../stores/FaviconStore';

class Favicon extends Component {
  constructor(props) {
    super(props);
  }

  static getStores = () => [FaviconStore];

  static calculateState() {
    return {
      iconPath: FaviconStore.getFaviconPath()
    };
  }

  componentWillUpdate(nextProps, nextState) {
    // Clone created element and create href attribute
    const currentFaviconNode = document.getElementById('favicon');
    let updatedFaviconNode = currentFaviconNode.cloneNode(true);

    // Set new href attribute
    updatedFaviconNode.setAttribute('href', nextState.iconPath);

    // Remove old and add new favicon
    currentFaviconNode.remove();
    document.head.appendChild(updatedFaviconNode);
  }

  render() {
    return null;
  }
}

export default Container.create(Favicon);
