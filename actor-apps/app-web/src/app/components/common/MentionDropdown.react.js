import React from 'react';
import classnames from 'classnames';

import { KeyCodes } from 'constants/ActorAppConstants';

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
      isShown: mentions && mentions.length > 0,
      selectedIndex: 0
    };
  }

  componentDidMount() {
    const { isShown } = this.state;

    if (isShown) {
      this.setListeners();
    }
  }

  componentWillUnmount() {
    this.cleanListeners();
  }

  componentWillUpdate(nextProps, nextState) {
    if (nextState.isShown && !this.state.isShown) {
      this.setListeners();
    } else if (!nextState.isShown && this.state.isShown) {
      this.cleanListeners();
    }
  }

  componentWillReceiveProps(props) {
    const { mentions } = props;
    this.setState({
      isShown: mentions && mentions.length > 0,
      selectedIndex: 0
    });
  }

  setListeners() {
    document.addEventListener('keydown', this.onKeyDown, false);
    document.addEventListener('click', this.closeMentions, false);
  }

  cleanListeners() {
    document.removeEventListener('keydown', this.onKeyDown, false);
    document.removeEventListener('click', this.closeMentions, false);
  }

  closeMentions = () => {
    this.setState({isShown: false});
  };


  render() {
    const { className, mentions } = this.props;
    const { isShown } = this.state;

    const mentionClassName = classnames('mention', {
      'mention--opened': isShown
    }, className);

    if (isShown) {
      return (
        <div className={mentionClassName}>
          <div className="mention__wrapper">
            <header className="mention__header">mention__header</header>
            <ul className="mention__list" ref="mentionList">
              <li className="mention__list__item">mention__list__item</li>
              <li className="mention__list__item">mention__list__item</li>
              <li className="mention__list__item">mention__list__item</li>
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
