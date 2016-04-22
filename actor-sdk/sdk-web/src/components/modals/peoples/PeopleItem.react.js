/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';
import classnames from 'classnames';

import AvatarItem from '../../common/AvatarItem.react';

import { escapeWithEmoji } from '../../../utils/EmojiUtils';

class PeopleItem extends Component {
  static propTypes = {
    contact: PropTypes.object.isRequired,
    isSelected: PropTypes.bool.isRequired,
    onClick: PropTypes.func.isRequired,
    onMouseOver: PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
  }

  handleClick = () => {
    const { contact, onClick } = this.props;
    onClick(contact);
  };

  handleMouseOver = () => {
    const { onMouseOver } = this.props;
    onMouseOver();
  };


  render() {
    const { contact, isSelected } = this.props;
    const resultClassName = classnames('result-list__item row', {
      'result-list__item--active': isSelected
    });

    return (
      <li className={resultClassName}
          onClick={this.handleClick}
          onMouseOver={this.handleMouseOver}>
        <AvatarItem image={contact.avatar}
                    placeholder={contact.placeholder}
                    size="medium"
                    title={contact.name}/>

        <div className="col-xs">
          <span className="title" dangerouslySetInnerHTML={{ __html: escapeWithEmoji(contact.name) }}/>
        </div>
      </li>
    );
  }
}

export default PeopleItem;
