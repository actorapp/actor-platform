import React from 'react';

export default class Fav extends React.Component {
  static propTypes = {
    path: React.PropTypes.string
  }

  constructor(props) {
    super(props);

    //// Create link element and it's attributes
    //let favicon = document.createElement('link');
    //let rel = document.createAttribute('rel');
    //let type = document.createAttribute('type');
    //let href = document.createAttribute('href');
    //let id = document.createAttribute('id');
    //
    //// Set attributes values
    //rel.value = 'icon';
    //type.value = 'image/png';
    //href.value = props.path;
    //id.value = 'favicon';
    //
    //// Set attributes to favicon element
    //favicon.setAttributeNode(rel);
    //favicon.setAttributeNode(type);
    //favicon.setAttributeNode(href);
    //favicon.setAttributeNode(id);
    //
    //// Append favicon to head
    //document.head.appendChild(favicon);
  }

  componentDidUpdate() {
    // Clone created element and create href attribute
    let updatedFavicon = document.getElementById('favicon').cloneNode(true);
    let href = document.createAttribute('href');

    // Set new href attribute
    href.value = this.props.path;
    updatedFavicon.setAttributeNode(href);

    // Remove old and add new favicon
    document.getElementById('favicon').remove();
    document.head.appendChild(updatedFavicon);
  }

  render() {
    return null;
  }
}
