import React from 'react';

import FaviconStore from 'stores/FaviconStore';

export default class Fav extends React.Component {
  constructor(props) {
    super(props);

    FaviconStore.addChangeListener(this.update)
  }

  componentWillUnmount() {
    FaviconStore.removeChangeListener(this.update);
  }

  update() {
    setTimeout(() => {
      // Clone created element and create href attribute
      let updatedFavicon = document.getElementById('favicon').cloneNode(true);

      // Set new href attribute
      updatedFavicon.setAttribute('href', FaviconStore.getFaviconPath());

      // Remove old and add new favicon
      document.getElementById('favicon').remove();
      document.head.appendChild(updatedFavicon);
    }, 0);
  }

  render() {
    return null;
  }
}
