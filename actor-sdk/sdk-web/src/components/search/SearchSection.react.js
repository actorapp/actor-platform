/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';

import SearchMessagesActionCreators from '../../actions/SearchMessagesActionCreators';

import SearchMessagesStore from '../../stores/SearchMessagesStore';

import SearchResults from './SearchResults.react.js';

class SearchSection extends Component {
  static getStores = () => [SearchMessagesStore];

  static calculateState() {
    return SearchMessagesStore.getState();
  }

  constructor(props) {
    super(props);

    this.onToggleExpanded = this.onToggleExpanded.bind(this);
  }

  onToggleExpanded() {
    SearchMessagesActionCreators.toggleExpand();
  }

  render() {
    const { query, results, isOpen, isExpanded, isSearching } = this.state;
    const searchClassName = classnames('search', {
      'search--opened': isOpen,
      'search--expanded': isExpanded
    });

    return (
      <section className={searchClassName}>
        <SearchResults
          query={query}
          results={results}
          isExpanded={isExpanded}
          isSearching={isSearching}
          onToggleExpanded={this.onToggleExpanded}
        />
      </section>
    )
  }
}

export default Container.create(SearchSection);
