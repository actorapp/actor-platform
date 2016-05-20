/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';
import { FormattedMessage, FormattedHTMLMessage } from 'react-intl';

import SearchResultItem from './SearchResultItem.react';

class SearchResults extends Component {
  static propTypes = {
    query: PropTypes.string.isRequired,
    results: PropTypes.array.isRequired,
    isExpanded: PropTypes.bool.isRequired,
    isSearching: PropTypes.bool.isRequired,
    onToggleExpanded: PropTypes.func.isRequired
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
    const { isExpanded, onToggleExpanded } = this.props;

    return (
      <div>
        <div className="search__expand" onClick={onToggleExpanded}>
          <i className="material-icons">{isExpanded ? 'chevron_right' : 'chevron_left'}</i>
          <i className="material-icons">{isExpanded ? 'chevron_right' : 'chevron_left'}</i>
        </div>
        <header className="search__header">
          <ul className="search__filter">
            <li className="search__filter__item search__filter__item--active">Text</li>
          </ul>
        </header>
        <div className="search__body">
          <ul className="search__results">
            {this.renderResults()}
          </ul>
        </div>
      </div>
    );
  }
}

export default SearchResults;
