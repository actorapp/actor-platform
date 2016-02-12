/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { map, debounce } from 'lodash';

import React, { Component, PropTypes } from 'react';
import { findDOMNode } from 'react-dom';
import { Container } from 'flux/utils';

import { KeyCodes } from '../../../constants/ActorAppConstants';

import ContactActionCreators from '../../../actions/ContactActionCreators';
import DialogActionCreators from '../../../actions/DialogActionCreators';

import PeopleStore from '../../../stores/PeopleStore';

import People from './PeopleItem.react';

class PeopleList extends Component {
  constructor(props) {
    super(props);
  }

  static contextTypes = {
    intl: PropTypes.object
  };

  static getStores = () => [PeopleStore];

  static calculateState() {
    return {
      list: PeopleStore.getList(),
      results: PeopleStore.getResults(),
      selectedIndex: 0
    };
  }

  componentDidMount() {
    this.setFocus();
    document.addEventListener('keydown', this.handleKeyDown, false);
  }

  componentWillUnmount() {
    document.removeEventListener('keydown', this.handleKeyDown, false);
  }

  setFocus = () => findDOMNode(this.refs.search).focus();

  handleClose = () => ContactActionCreators.close();

  handleSearchChange = (event) => {
    const query = event.target.value;
    this.setState({query});
    this.searchPeople(query)
  };

  searchPeople = debounce((query) => ContactActionCreators.search(query), 300, {trailing: true});

  handleContactSelect = (contact) => {
    DialogActionCreators.selectDialogPeerUser(contact.uid);
    this.handleClose()
  };

  handleKeyDown = (event) => {
    const { results, selectedIndex } = this.state;
    let index = selectedIndex;

    const selectNext = () => {
      if (index < results.length - 1) {
        index += 1;
      } else if (index === results.length - 1) {
        index = 0;
      }

      this.setState({selectedIndex: index});

      const scrollContainerNode = findDOMNode(this.refs.results);
      const selectedNode = findDOMNode(this.refs.selected);
      const scrollContainerNodeRect = scrollContainerNode.getBoundingClientRect();
      const selectedNodeRect = selectedNode.getBoundingClientRect();

      if ((scrollContainerNodeRect.top + scrollContainerNodeRect.height) < (selectedNodeRect.top + selectedNodeRect.height)) {
        this.handleScroll(scrollContainerNode.scrollTop + (selectedNodeRect.top + selectedNodeRect.height) - (scrollContainerNodeRect.top + scrollContainerNodeRect.height));
      } else if (scrollContainerNodeRect.top > selectedNodeRect.top) {
        this.handleScroll(0);
      }
    };
    const selectPrev = () => {
      if (index > 0) {
        index -= 1;
      } else if (index === 0) {
        index = results.length - 1;
      }

      this.setState({selectedIndex: index});

      const scrollContainerNode = findDOMNode(this.refs.results);
      const selectedNode = findDOMNode(this.refs.selected);
      const scrollContainerNodeRect = scrollContainerNode.getBoundingClientRect();
      const selectedNodeRect = selectedNode.getBoundingClientRect();

      if (scrollContainerNodeRect.top > selectedNodeRect.top) {
        this.handleScroll(scrollContainerNode.scrollTop + selectedNodeRect.top - scrollContainerNodeRect.top);
      } else if (selectedNodeRect.top > (scrollContainerNodeRect.top + scrollContainerNodeRect.height)) {
        this.handleScroll(scrollContainerNode.scrollHeight);
      }
    };

    switch (event.keyCode) {
      case KeyCodes.ENTER:
        event.stopPropagation();
        event.preventDefault();
        this.handleContactSelect(results[selectedIndex]);
        break;

      case KeyCodes.ARROW_UP:
        event.stopPropagation();
        event.preventDefault();
        selectPrev();
        break;
      case KeyCodes.ARROW_DOWN:
        event.stopPropagation();
        event.preventDefault();
        selectNext();
        break;
      case KeyCodes.TAB:
        event.stopPropagation();
        event.preventDefault();
        if (event.shiftKey) {
          selectPrev();
        } else {
          selectNext();
        }
        break;
      default:
    }
  };

  handleScroll = (top) => {
    const resultsNode = findDOMNode(this.refs.results);
    resultsNode.scrollTop = top;
  };

  render() {
    const { query, results, selectedIndex, list } = this.state;
    const { intl } = this.context;

    const peopleList = map(results, (result, index) => <People contact={result} key={index}
                                                               onClick={this.handleContactSelect}
                                                               isSelected={selectedIndex === index}
                                                               ref={selectedIndex === index ? 'selected' : null}
                                                               onMouseOver={() => this.setState({selectedIndex: index})}/>);

    return (
      <div className="newmodal newmodal__contacts">
        <header className="newmodal__header">
          <h2>{intl.messages['modal.contacts.title']}</h2>
        </header>

        <section className="newmodal__search">
          <input className="newmodal__search__input"
                 onChange={this.handleSearchChange}
                 placeholder={intl.messages['modal.contacts.search']}
                 type="search"
                 ref="search"
                 value={query}/>
        </section>

        <ul className="newmodal__result contacts__list" ref="results">
          {
            list.length === 0
              ? <div>{intl.messages['modal.contacts.loading']}</div>
              : results.length === 0
              ? <li className="contacts__list__item contacts__list__item--empty text-center">
                  {intl.messages['modal.contacts.notFound']}
                </li>
              : peopleList
          }
        </ul>
      </div>
    )
  }
}

export default Container.create(PeopleList, {pure: false});

