/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import history from '../../utils/history';
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
    this.onResultSelect = this.onResultSelect.bind(this);
  }

  handlerSearchClear() {
    SearchActionCreators.clear();
  }

  handleSearchFocus() {
    SearchActionCreators.focus();
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
      history.push(`/im/${contact.peerInfo.peer.id}`);
    }
  }

  renderSearchResultsDropdown() {
    const { results: { contacts, groups } } = this.state;

    return (
      <SelectList className="toolbar__search__dropdown" max={contacts.length + groups.length} onSelect={this.onResultSelect}>
        <div className="toolbar__search__results">
          <SearchResultGroup id="contacts" offset={0} items={contacts} />
          <SearchResultGroup id="groups" offset={contacts.length} items={groups} />
        </div>
        <SelectListItem index={contacts.length + groups.length}>
          <footer className="toolbar__search__footer">
            Search messages in current dialog <i className="material-icons">arrow_forward</i>
          </footer>
        </SelectListItem>
      </SelectList>
    );
  }

  render() {
    const { query, isFocused } = this.state;
    const toolbarSearchClassName = classnames('toolbar__search row toolbar__button', {
      'toolbar__search--focused': isFocused
    });

    return (
      <div className={toolbarSearchClassName} onClick={this.handleToolbarSearchClick}>
        <div className="row">
          <i className="search-icon material-icons">search</i>
          <SearchInput
            value={query}
            onFocus={this.handleSearchFocus}
            onClear={this.handlerSearchClear}
            onChange={this.handleSearchChange}
          />
        </div>
        {isFocused && this.renderSearchResultsDropdown()}
      </div>
    );
  }
}

export default Container.create(ToolbarSearch);
