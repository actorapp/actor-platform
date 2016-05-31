/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import { FormattedMessage, FormattedHTMLMessage } from 'react-intl';

import SearchStore from '../../stores/SearchStore';
import SearchActionCreators from '../../actions/SearchActionCreators';

import SearchInput from './SearchInput.react';
import SelectList from '../common/SelectList.react';
import SelectListItem from '../common/SelectListItem.react';
import ToolbarSearchResults from './ToolbarSearchResults.react';

class ToolbarSearch extends Component {
  static getStores() {
    return [SearchStore];
  }

  static calculateState() {
    return SearchStore.getState();
  }

  constructor(props) {
    super(props);

    this.handleSearchChange = this.handleSearchChange.bind(this);
    this.handlerSearchClear = this.handlerSearchClear.bind(this);
    this.handleSearchBlur = this.handleSearchBlur.bind(this);
    this.handleSearchFocus = this.handleSearchFocus.bind(this);
    this.onResultSelect = this.onResultSelect.bind(this);
  }

  handlerSearchClear() {
    SearchActionCreators.clear();
  }

  handleSearchBlur() {
    SearchActionCreators.blur();
  }

  handleSearchFocus() {
    SearchActionCreators.focus();
  }

  handleSearchChange(query) {
    SearchActionCreators.handleSearch(query);
  }

  onResultSelect(index) {
    const { results } = this.state;
    if (index === results.length) {
      SearchActionCreators.goToMessagesSearch(this.state.query);
    } else {
      const contact = results[index];
      SearchActionCreators.goToContact(contact);
    }
  }

  renderSearchResultsDropdown() {
    const { isFocused, query, results } = this.state;

    if (!isFocused) {
      return null
    }

    if (!query.length) {
      return (
        <div className="toolbar__search__dropdown">
          <div className="hint">
            <FormattedHTMLMessage id="search.hint"/>
          </div>
        </div>
      );
    }

    return (
      <SelectList className="toolbar__search__dropdown" max={results.length} onSelect={this.onResultSelect}>
        <ToolbarSearchResults query={query}results={results} />
        <SelectListItem index={results.length}>
          <footer className="toolbar__search__footer">
            <FormattedMessage id="search.inDialog"/> <i className="material-icons">arrow_forward</i>
          </footer>
        </SelectListItem>
      </SelectList>
    );
  }

  render() {
    const { query, isFocused } = this.state;
    const toolbarSearchClassName = classnames('toolbar__search row', {
      'toolbar__search--focused': isFocused
    });

    return (
      <div className={toolbarSearchClassName} onClick={this.handleToolbarSearchClick}>
        <div className="row">
          <i className="search-icon material-icons">search</i>
          <SearchInput
            value={query}
            isFocused={isFocused}
            onBlur={this.handleSearchBlur}
            onFocus={this.handleSearchFocus}
            onClear={this.handlerSearchClear}
            onChange={this.handleSearchChange}
          />
        </div>
        {this.renderSearchResultsDropdown()}
      </div>
    );
  }
}

export default Container.create(ToolbarSearch);
