/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';
import { FormattedMessage, FormattedHTMLMessage } from 'react-intl';
import Scroller from '../common/Scroller.react';
import SearchResultItem from './SearchResultItem.react';

class SearchResults extends Component {
  static propTypes = {
    query: PropTypes.string.isRequired,
    results: PropTypes.array.isRequired,
    isSearching: PropTypes.bool.isRequired
  };

  constructor(props) {
    super(props);

    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
  }

  renderResults() {
    const { query, results, isSearching } = this.props;

    if (!query) {
      return (
        <li className="search__results__item search__results__item--empty">
          <FormattedMessage id="search.emptyQuery" />
        </li>
      );
    }

    if (isSearching) {
      return (
        <li className="search__results__item search__results__item--not-found">
          <FormattedMessage id="search.searching" values={{ query }} />
        </li>
      );
    }

    if (!results.length) {
      return (
        <li className="search__results__item search__results__item--not-found">
          <FormattedHTMLMessage id="search.notFound" values={{ query }} />
        </li>
      );
    }

    return results.map((result, index) => {
      return <SearchResultItem {...result} key={index} />
    });
  }

  render() {
    return (
      <Scroller className="search__body">
        {this.renderResults()}
      </Scroller>
    );
  }
}

export default SearchResults;
