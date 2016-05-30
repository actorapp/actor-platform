/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';
import ContactItem from '../common/ContactItem.react';
import SelectListItem from '../common/SelectListItem.react';

class SearchResultGroup extends Component {
  static propTypes = {
    id: PropTypes.string.isRequired,
    items: PropTypes.array.isRequired,
    offset: PropTypes.number.isRequired
  };

  renderResults() {
    const { id, items, offset } = this.props;

    if (!items.length) {
      return (
        <div className="not-found">
          <FormattedMessage id={`toolbar.search.${id}.notFound`} />
        </div>
      );
    }

    return items.map((item, index) => {
      return (
        <SelectListItem index={index + offset} key={item.peerInfo.peer.key}>
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
    const { id } = this.props;

    return (
      <div>
        <header>
          <FormattedMessage id={`toolbar.search.${id}.title`}/>
        </header>
        {this.renderResults()}
      </div>
    );
  }
}

export default SearchResultGroup;
