/* eslint-disable */
import React from 'react';
import classnames from 'classnames';

class Dropdown extends React.Component {
  static propTypes = {
    className: React.PropTypes.string,
    children: React.PropTypes.array,
    onSelect: React.PropTypes.func
  };

  constructor(props) {
    super(props);

    this.state = {
      isShown: false
    };
  }

  openDropdown = () => {
    this.setState({isShown: true});
    document.addEventListener('click', this.closeDropdown, false);
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

  render() {
    const { className, children } = this.props;

    let dropdownItems = [];

    React.Children.forEach(children, (child, index) => {
      if (child.type.name === 'DropdownItem') {
        dropdownItems.push(
          <li className="dropdown__menu__item" key={index} onClick={this.onSelect.bind(this, child.props.value)}>
            {child.props.children}
          </li>
        );
      }
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
