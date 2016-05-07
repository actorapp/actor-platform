/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { map } from 'lodash';
import React, { PropTypes, Component } from 'react';
import EventListener from 'fbjs/lib/EventListener';
import { findDOMNode } from 'react-dom';
import classnames from 'classnames';
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';

import { KeyCodes } from '../../constants/ActorAppConstants';

import AvatarItem from './AvatarItem.react';

const DROPDOWN_ITEM_HEIGHT = 38;
let scrollIndex = 0;

class MentionDropdown extends Component {
  static propTypes = {
    mentions: PropTypes.array,
    className: PropTypes.string,
    onSelect: PropTypes.func.isRequired,
    onClose: PropTypes.func
  };

  constructor(props) {
    super(props);
    const { mentions } = props;

    this.state = {
      isOpen: mentions && mentions.length > 0,
      selectedIndex: 0
    };

    this.onKeyDown = this.onKeyDown.bind(this);
    this.onDocumentClick = this.onDocumentClick.bind(this);
    this.onDocumentKeyDown = this.onDocumentKeyDown.bind(this);
    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
  }

  componentDidMount() {
    this.listeners = [
      EventListener.listen(document, 'click', this.onDocumentClick),
      EventListener.listen(document, 'keydown', this.onDocumentKeyDown)
    ];
  }

  componentWillUnmount() {
    this.listeners.forEach((listener) => listener.remove());
    this.listeners = null;
  }

  componentWillReceiveProps(props) {
    const { mentions } = props;
    this.setState({
      isOpen: mentions && mentions.length > 0,
      selectedIndex: 0
    });
  }

  onDocumentClick() {
    if (this.state.isOpen) {
      this.closeMentions();
    }
  }

  onDocumentKeyDown(event) {
    if (this.state.isOpen) {
      this.onKeyDown(event);
    }
  }

  closeMentions = () => this.setState({ isOpen: false });

  onSelect = (value) => this.props.onSelect(value);

  handleScroll = (top) => {
    const menuListNode = findDOMNode(this.refs.mentionList);
    menuListNode.scrollTop = top;
  };

  onKeyDown(event) {
    const { mentions, onClose } = this.props;
    const { selectedIndex } = this.state;
    const visibleItems = 6;
    let index = selectedIndex;

    if (index !== null) {
      switch (event.keyCode) {
        case KeyCodes.ENTER:
          event.stopPropagation();
          event.preventDefault();
          this.onSelect(mentions[selectedIndex]);
          break;

        case KeyCodes.ARROW_UP:
          event.stopPropagation();
          event.preventDefault();

          if (index > 0) {
            index -= 1;
          } else if (index === 0) {
            index = mentions.length - 1;
          }

          if (scrollIndex > index) {
            scrollIndex = index;
          } else if (index === mentions.length - 1) {
            scrollIndex = mentions.length - visibleItems;
          }

          this.handleScroll(scrollIndex * DROPDOWN_ITEM_HEIGHT);
          this.setState({ selectedIndex: index });
          break;
        case KeyCodes.ARROW_DOWN:
        case KeyCodes.TAB:
          event.stopPropagation();
          event.preventDefault();

          if (index < mentions.length - 1) {
            index += 1;
          } else if (index === mentions.length - 1) {
            index = 0;
          }

          if (index + 1 > scrollIndex + visibleItems) {
            scrollIndex = index + 1 - visibleItems;
          } else if (index === 0) {
            scrollIndex = 0;
          }

          this.handleScroll(scrollIndex * DROPDOWN_ITEM_HEIGHT);
          this.setState({ selectedIndex: index });
          break;
        default:
      }
    }

    if (event.keyCode === KeyCodes.ESC) {
      this.closeMentions();
      if (onClose) onClose();
    }
  }

  render() {
    const { className, mentions } = this.props;
    const { isOpen, selectedIndex } = this.state;

    if (!isOpen) {
      return <div className="mention" />;
    }

    const mentionsElements = map(mentions, (mention, index) => {
      const itemClassName = classnames('mention__list__item', {
        'mention__list__item--active': selectedIndex === index
      });

      return (
        <li className={itemClassName}
            key={index}
            onClick={() => this.onSelect(mention)}
            onMouseOver={() => this.setState({ selectedIndex: index })}>
          <AvatarItem image={mention.peer.avatar}
                      placeholder={mention.peer.placeholder}
                      size="tiny"
                      title={mention.peer.title}/>
          <div className="title">
            {mention.isNick && <span className="nickname">{mention.mentionText}</span>}
            <span className="name">{mention.mentionText}</span>
          </div>
        </li>
      );
    });

    const mentionClassName = classnames('mention mention--opened', className);

    return (
      <div className={mentionClassName}>
        <div className="mention__wrapper">
          <header className="mention__header">
            <div className="pull-left"><strong>tab</strong>&nbsp; or &nbsp;<strong>↑</strong><strong>↓</strong>&nbsp; to navigate</div>
            <div className="pull-left"><strong>↵</strong>&nbsp; to select</div>
            <div className="pull-right"><strong>esc</strong>&nbsp; to close</div>
          </header>
          <ul className="mention__list" ref="mentionList">
            {mentionsElements}
          </ul>
        </div>
      </div>
    );
  }
}

export default MentionDropdown;
