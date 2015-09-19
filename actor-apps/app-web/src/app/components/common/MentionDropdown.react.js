import React from 'react';
import classnames from 'classnames';
import ReactMixin from 'react-mixin';
import addons from 'react/addons';

import { KeyCodes } from 'constants/ActorAppConstants';

import AvatarItem from 'components/common/AvatarItem.react';

const {addons: { PureRenderMixin }} = addons;

const DROPDOWN_ITEM_HEIGHT = 38; // is this right?
let scrollIndex = 0;

@ReactMixin.decorate(PureRenderMixin)
class MentionDropdown extends React.Component {
  static propTypes = {
    mentions: React.PropTypes.array,
    className: React.PropTypes.string,
    onSelect: React.PropTypes.func.isRequired,
    onClose: React.PropTypes.func
  };

  constructor(props) {
    super(props);
    const { mentions } = props;

    this.state = {
      isOpen: mentions && mentions.length > 0,
      selectedIndex: 0
    };
  }

  componentWillUnmount() {
    this.cleanListeners();
  }

  componentWillUpdate(nextProps, nextState) {
    if (nextState.isOpen && !this.state.isOpen) {
      this.setListeners();
    } else if (!nextState.isOpen && this.state.isOpen) {
      this.cleanListeners();
    }
  }

  componentWillReceiveProps(props) {
    const { mentions } = props;
    this.setState({
      isOpen: mentions && mentions.length > 0,
      selectedIndex: 0
    });
  }

  setListeners() {
    document.addEventListener('keydown', this.onKeyDown, false);
    document.addEventListener('click', this.closeMentions, false);
  }

  cleanListeners() {
    //console.info('cleanListeners');
    document.removeEventListener('keydown', this.onKeyDown, false);
    document.removeEventListener('click', this.closeMentions, false);
  }

  closeMentions = () => this.setState({isOpen: false});

  onSelect = (value) => this.props.onSelect(value);

  handleScroll = (top) => {
    const menuListNode = React.findDOMNode(this.refs.mentionList);
    menuListNode.scrollTop = top;
  };

  onKeyDown = (event) => {
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
          this.setState({selectedIndex: index});
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
          this.setState({selectedIndex: index});
          break;
        default:
      }
    }

    if (event.keyCode === KeyCodes.ESC) {
      this.closeMentions();
      if (onClose) onClose();
    }
  };

  render() {
    const { className, mentions } = this.props;
    const { isOpen, selectedIndex } = this.state;

    const mentionClassName = classnames('mention', {
      'mention--opened': isOpen
    }, className);
    const mentionsElements = _.map(mentions, (mention, index) => {
      const itemClassName = classnames('mention__list__item', {
        'mention__list__item--active': selectedIndex === index
      });

      const title = mention.isNick ? [
        <span className="nickname">{mention.mentionText}</span>,
        <span className="name">{mention.secondText}</span>
      ] : (
        <span className="name">{mention.mentionText}</span>
      );

      return (
        <li className={itemClassName}
            key={index}
            onClick={() => this.onSelect(mention)}
            onMouseOver={() => this.setState({selectedIndex: index})}>
          <AvatarItem image={mention.peer.avatar}
                      placeholder={mention.peer.placeholder}
                      size="tiny"
                      title={mention.peer.title}/>
          <div className="title">{title}</div>
        </li>
      );
    });

    if (isOpen) {
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
    } else {
      return null;
    }
  }
}

export default MentionDropdown;
