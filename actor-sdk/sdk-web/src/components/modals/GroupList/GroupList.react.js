/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { map, debounce } from 'lodash';

import React, { Component, PropTypes } from 'react';
import { findDOMNode } from 'react-dom';
import { Container } from 'flux/utils';
import { FormattedHTMLMessage } from 'react-intl';

import { KeyCodes } from '../../../constants/ActorAppConstants';

import GroupListActionCreators from '../../../actions/GroupListActionCreators'
import DialogActionCreators from '../../../actions/DialogActionCreators'

import GroupListStore from '../../../stores/GroupListStore';

import Group from './Group.react';

class GroupList extends Component {
  constructor(props) {
    super(props);
  }

  static contextTypes = {
    intl: PropTypes.object
  };

  static getStores = () => [GroupListStore];

  static calculateState() {
    return {
      list: GroupListStore.getList(),
      results: GroupListStore.getResults(),
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
  handleClose = () => GroupListActionCreators.close();

  handleSearchChange = (event) => {
    const query = event.target.value;
    this.setState({query});
    this.searchGroups(query)
  };

  searchGroups = debounce((query) => GroupListActionCreators.search(query), 300, {trailing: true});

  handleGroupSelect = (peer) => {
    DialogActionCreators.selectDialogPeer(peer);
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
        this.handleGroupSelect(results[selectedIndex].peerInfo.peer);
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

    let groupList = map(results, (result, index) => <Group group={result} key={index}
                                                           isSelected={selectedIndex === index}
                                                           ref={selectedIndex === index ? 'selected' : null}
                                                           onClick={this.handleGroupSelect}
                                                           onMouseOver={() => this.setState({selectedIndex: index})}/>);

    return (
      <div className="newmodal newmodal__groups">
        <header className="newmodal__header">
          <h2>{intl.messages['modal.groups.title']}</h2>
        </header>

        <section className="newmodal__search">
          <input className="newmodal__search__input"
                 onChange={this.handleSearchChange}
                 placeholder={intl.messages['modal.groups.search']}
                 type="search"
                 ref="search"
                 value={query}/>
        </section>

        <ul className="newmodal__result group__list" ref="results">
          {
            list.length === 0
              ? <div>{intl.messages['modal.groups.loading']}</div>
              : results.length === 0
                  ? <li className="group__list__item group__list__item--empty text-center">
                      <FormattedHTMLMessage id="modal.groups.notFound"
                                            values={{query}} />
                    </li>
                  : groupList
          }
        </ul>
      </div>
    )
  }
}

export default Container.create(GroupList, {pure: false});
