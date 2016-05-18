/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import { FormattedMessage } from 'react-intl';

import SearchStore from '../../stores/SearchStore';

import SearchActionCreators from '../../actions/SearchActionCreators';
import ComposeActionCreators from '../../actions/ComposeActionCreators';

import SearchInput from './SearchInput.react';
import ContactItem from '../common/ContactItem.react';

class ToolbarSearch extends Component {
  static propTypes = {
    className: PropTypes.string
  }

  static getStores() {
    return [SearchStore];
  }

  static calculateState(prevState) {
    const searchState = SearchStore.getState();

    return {
      ...prevState,
      query: searchState.query,
      results: searchState.results,
      isSearchExpanded: prevState ? prevState.isSearchExpanded : false,
      isSearchFocused: prevState ? prevState.isSearchFocused : false
    }
  }

  constructor(props) {
    super(props);

    this.handleToolbarSearchClick = this.handleToolbarSearchClick.bind(this);
    this.handleSearchChange = this.handleSearchChange.bind(this);
    // this.handleSearchToggleOpen = this.handleSearchToggleOpen.bind(this);
    this.handleSearchToggleFocus = this.handleSearchToggleFocus.bind(this);
  }

  handleSearchChange(query) {
    SearchActionCreators.handleSearch(query);
  }

  handleSearchToggleFocus(isFocused) {
    ComposeActionCreators.toggleAutoFocus(!isFocused);
    this.setState({ isSearchFocused: isFocused });
  }

  handleToolbarSearchClick() {
    this.setState({ isSearchExpanded: true });
  }

  renderSearchInput() {
    const { query, isSearchExpanded } = this.state;

    if (!isSearchExpanded) {
      return null;
    }

    return (
      <SearchInput
        className="toolbar__search__input col-xs"
        value={query}
        onChange={this.handleSearchChange}
        onToggleFocus={this.handleSearchToggleFocus}
      />
    );
  }

  renderSearchGroupResults() {
    const { results: { groups } } = this.state;

    if (!groups.length) {
      return null;
    }

    return groups.map((group) => {
      return (
        <ContactItem
          uid={group.peerInfo.peer.id}
          name={group.peerInfo.title}
          placeholder={group.peerInfo.placeholder}
          avatar={group.peerInfo.avatar}
        />
      );
    });
  }

  renderSearchContactResults() {
    const { results: { contacts } } = this.state;

    if (!contacts.length) {
      return null;
    }

    return contacts.map((contact) => {
      return (
        <ContactItem
          uid={contact.peerInfo.peer.id}
          name={contact.peerInfo.title}
          placeholder={contact.peerInfo.placeholder}
          avatar={contact.peerInfo.avatar}
        />
      );
    });
  }

  renderSearchResultsDropdown() {
    const { query, isSearchFocused } = this.state;

    if (!query || query === '' || !isSearchFocused) {
      return null;
    }

    return (
      <div className="toolbar__search__dropdown">
        <div className="toolbar__search__results">
          {this.renderSearchContactResults()}
          {this.renderSearchGroupResults()}
          <a href="#">Search messages in current dialog</a>
        </div>
      </div>
    );
  }

  render() {
    const { className } = this.props;
    const { isSearchExpanded, isSearchFocused } = this.state;

    const toolbarSearchClassName = classnames('toolbar__search row', className, {
      'toolbar__search--expanded': isSearchExpanded,
      'toolbar__search--focused': isSearchFocused
    });

    return (
      <div className={toolbarSearchClassName} onClick={this.handleToolbarSearchClick}>
        <div className="row">
          <i className="search-icon material-icons">search</i>
          {this.renderSearchInput()}
        </div>
        {this.renderSearchResultsDropdown()}
      </div>
    );
  }
}

export default Container.create(ToolbarSearch);
