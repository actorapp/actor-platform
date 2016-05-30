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
import SearchResultGroup from './SearchResultGroup.react';

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
    this.handleSearchFocus = this.handleSearchFocus.bind(this);
    this.handleSearchBlur = this.handleSearchBlur.bind(this);
    this.onResultSelect = this.onResultSelect.bind(this);
  }

  handlerSearchClear() {
    SearchActionCreators.clear();
  }

  handleSearchFocus() {
    SearchActionCreators.focus();
  }

  handleSearchBlur(event) {
    console.debug(event);
    // tricky workaround for click on search result
    setTimeout(() => SearchActionCreators.blur(), 50);
  }

  handleSearchChange(query) {
    SearchActionCreators.handleSearch(query);
  }

  onResultSelect(index) {
    const { results: { contacts, groups } } = this.state;
    if (index === contacts.length + groups.length) {
      SearchActionCreators.goToMessagesSearch(this.state.query);
    } else {
      const contact = [...contacts, ...groups][index];
      SearchActionCreators.goToContact(contact);
    }
  }

  renderSearchResultsDropdown() {
    const { isFocused, query, results: { contacts, groups } } = this.state;

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

    const max = contacts.length + groups.length;

    return (
      <SelectList className="toolbar__search__dropdown" max={max} onSelect={this.onResultSelect}>
        <div className="toolbar__search__results">
          <SearchResultGroup id="contacts" offset={0} items={contacts} />
          <SearchResultGroup id="groups" offset={contacts.length} items={groups} />
        </div>
        <SelectListItem index={max}>
          <footer className="toolbar__search__footer">
            <FormattedMessage id="search.inChat"/> <i className="material-icons">arrow_forward</i>
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
            onFocus={this.handleSearchFocus}
            onBlur={this.handleSearchBlur}
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
