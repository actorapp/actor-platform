/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { FormattedHTMLMessage } from 'react-intl';
import ContactItem from '../common/ContactItem.react';
import SelectListItem from '../common/SelectListItem.react';

class ToolbarSearchResults extends Component {
  static propTypes = {
    query: PropTypes.string.isRequired,
    results: PropTypes.array.isRequired
  };

  renderResults() {
    const { query, results } = this.props;

    if (!results.length) {
      return (
        <div className="not-found">
          <FormattedHTMLMessage id="search.notFound" values={{ query }} />
        </div>
      );
    }

    return results.map((item, index) => {
      return (
        <SelectListItem index={index} key={item.peerInfo.peer.key}>
          <ContactItem
            uid={item.peerInfo.peer.id}
            name={item.peerInfo.title}
            avatar={item.peerInfo.avatar}
            placeholder={item.peerInfo.placeholder}
          />
        </SelectListItem>
      )
    });
  }

  render() {
    return (
      <div className="toolbar__search__results">
        {this.renderResults()}
      </div>
    );
  }
}

export default ToolbarSearchResults;
