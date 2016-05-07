/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { PropTypes, Component } from 'react';
import EventListener from 'fbjs/lib/EventListener';
import { findDOMNode } from 'react-dom';
import classnames from 'classnames';
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';

import { KeyCodes } from '../../../constants/ActorAppConstants';

const DROPDOWN_ITEM_HEIGHT = 33;
let scrollIndex = 0;

class BotCommandsHint extends Component {
  static propTypes = {
    commands: PropTypes.array.isRequired,
    onSelect: PropTypes.func.isRequired,
    onClose: PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.state = {
      selectedIndex: 0
    };

    this.scrollTo = this.scrollTo.bind(this);
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

  onDocumentClick() {
    this.props.onClose();
  }

  onDocumentKeyDown(event) {
    this.onKeyDown(event);
  }

  scrollTo(top) {
    const menuListNode = findDOMNode(this.refs.list);
    menuListNode.scrollTop = top;
  }

  onKeyDown(event) {
    const { commands } = this.props;
    const { selectedIndex } = this.state;
    const visibleItems = 3;
    let index = selectedIndex;

    if (index !== null) {
      switch (event.keyCode) {
        case KeyCodes.ENTER:
          event.stopPropagation();
          event.preventDefault();
          this.props.onSelect(commands[selectedIndex].command);
          break;

        case KeyCodes.ARROW_UP:
          event.stopPropagation();
          event.preventDefault();

          if (index > 0) {
            index -= 1;
          } else if (index === 0) {
            index = commands.length - 1;
          }

          if (scrollIndex > index) {
            scrollIndex = index;
          } else if (index === commands.length - 1) {
            scrollIndex = commands.length - visibleItems;
          }

          this.scrollTo(scrollIndex * DROPDOWN_ITEM_HEIGHT);
          this.setState({ selectedIndex: index });
          break;
        case KeyCodes.ARROW_DOWN:
        case KeyCodes.TAB:
          event.stopPropagation();
          event.preventDefault();

          if (index < commands.length - 1) {
            index += 1;
          } else if (index === commands.length - 1) {
            index = 0;
          }

          if (index + 1 > scrollIndex + visibleItems) {
            scrollIndex = index + 1 - visibleItems;
          } else if (index === 0) {
            scrollIndex = 0;
          }

          this.scrollTo(scrollIndex * DROPDOWN_ITEM_HEIGHT);
          this.setState({ selectedIndex: index });
          break;
        default:
      }
    }

    if (event.keyCode === KeyCodes.ESC) {
      this.props.onClose();
    }
  }

  renderCommands() {
    const { selectedIndex } = this.state;

    return this.props.commands.map(({ command, description }, index) => {
      const className = classnames('mention__list__item', {
        'mention__list__item--active': selectedIndex === index
      });

      return (
        <li
          key={command}
          className={className}
          onClick={() => this.props.onSelect(command)}
          onMouseOver={() => this.setState({ selectedIndex: index })}
        >
          <div className="title">
            <span className="nickname">{`/${command}`}</span>
            <span className="name">{description}</span>
          </div>
        </li>
      );
    });
  }

  render() {
    return (
      <div className="bot-commands bot-commands">
        <div className="bot-commands__wrapper">
          <header className="bot-commands__header">
            <div className="pull-left"><strong>tab</strong>&nbsp; or &nbsp;<strong>↑</strong><strong>↓</strong>&nbsp; to navigate</div>
            <div className="pull-left"><strong>↵</strong>&nbsp; to select</div>
            <div className="pull-right"><strong>esc</strong>&nbsp; to close</div>
          </header>
          <ul className="bot-commands__list" ref="list">
            {this.renderCommands()}
          </ul>
        </div>
      </div>
    );
  }
}

export default BotCommandsHint;
