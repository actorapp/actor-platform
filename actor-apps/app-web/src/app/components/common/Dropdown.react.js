import React from 'react';
import classnames from 'classnames';

import { KeyCodes } from 'constants/ActorAppConstants';

class Dropdown extends React.Component {
  static propTypes = {
    isShown: React.PropTypes.bool,
    className: React.PropTypes.string,
    children: React.PropTypes.array,
    onSelect: React.PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.state = {
      isShown: props.isShown,
      selectedIndex: this.getDefaultIndex(props.children)
    };
  }

  componentWillMount() {
    document.addEventListener('keydown', this.onKeyDown, false);
  }

  componentWillUnmount() {
    document.removeEventListener('click', this.closeDropdown);
    document.removeEventListener('keydown', this.onKeyDown, false);
  }

  componentWillReceiveProps(props) {
    this.setState({
      isShown: props.isShown,
      selectedIndex: this.getDefaultIndex(props.children)
    });
  }

  getDefaultIndex = (children) => {
    let index = null;

    if (children && children.length > 0) {
      index = children.length - 1;
    }

    return index;
  };

  getValue = (index) => {
    if (this.props.children.length > 0) {
      return this.props.children[index].props.value;
    } else {
      return null;
    }
  };

  closeDropdown = () => {
    this.setState({isShown: false});
    document.removeEventListener('click', this.closeDropdown, false);
  };

  onSelect = (value) => {
    if (this.props.onSelect) {
      this.props.onSelect(value);
    }
  };


  onKeyDown = (e) => {
    let index = this.state.selectedIndex;

    if (index !== null) {
      switch (e.keyCode) {
        case KeyCodes.ENTER:
          e.stopPropagation();
          e.preventDefault();

          this.onSelect(this.getValue(this.state.selectedIndex));
          break;
        case KeyCodes.ARROW_UP:
          e.stopPropagation();
          e.preventDefault();

          if (index !== 0) {
            index -= 1;
          }

          this.setState({selectedIndex: index});
          break;
        case KeyCodes.ARROW_DOWN:
          e.stopPropagation();
          e.preventDefault();

          if (index !== this.props.children.length - 1) {
            index += 1;
          }

          this.setState({selectedIndex: index});
          break;
        default:
      }
    }

    if (e.keyCode === KeyCodes.ESC) {
      this.closeDropdown();
    }
  };

  render() {
    const { className, children } = this.props;

    let dropdownItems = [];

    React.Children.forEach(children, (child, index) => {
      let elClassName = 'dropdown__menu__item';

      if (this.state.selectedIndex === index) {
        elClassName += ' dropdown__menu__item--active';
      }

      dropdownItems.push(
        <li className={elClassName}
            key={index}
            onClick={this.onSelect.bind(this, child.props.value)}
            onMouseOver={() => this.setState({selectedIndex: index})}>
          {child.props.children}
        </li>
      );
    });

    const dropdownClassName = classnames({
      'dropdown': true,
      'dropdown--opened': this.state.isShown
    }, className);

    return (
      <div className={dropdownClassName}>
        <ul className="dropdown__menu">
          {dropdownItems}
        </ul>
      </div>
    );
  }
}

class DropdownItem extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    return this.props.children;
  }
}

export default { Dropdown, DropdownItem };
