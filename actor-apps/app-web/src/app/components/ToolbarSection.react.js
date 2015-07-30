import React from 'react';
import classnames from 'classnames';

import DialogStore from 'stores/DialogStore';

import ActivityActionCreators from 'actions/ActivityActionCreators';
import ActivityStore from 'stores/ActivityStore';

//import AvatarItem from 'components/common/AvatarItem.react';

const getStateFromStores = () => {
  return {
    dialogInfo: DialogStore.getSelectedDialogInfo(),
    isActivityOpen: ActivityStore.isOpen()
  };
};

class ToolbarSection extends React.Component {
  state = {
    dialogInfo: null,
    isActivityOpen: false
  };

  constructor(props) {
    super(props);

    DialogStore.addSelectedChangeListener(this.onChange);
    ActivityStore.addChangeListener(this.onChange);
  }

  componentWillUnmount() {
    DialogStore.removeSelectedChangeListener(this.onChange);
    ActivityStore.removeChangeListener(this.onChange);
  }

  onClick = () => {
    if (!this.state.isActivityOpen) {
      ActivityActionCreators.show();
    } else {
      ActivityActionCreators.hide();
    }
  };

  onChange = () => {
    this.setState(getStateFromStores());
  };

  render() {
    const info = this.state.dialogInfo;
    const isActivityOpen = this.state.isActivityOpen;
    let dialogElement;

    let infoButtonClassName = classnames('button button--icon', {
      'button--active': isActivityOpen
    });

    if (info != null) {
      dialogElement = (
        <div className="toolbar__peer row">
          {/*
          <a onClick={this.onClick}>
            <AvatarItem image={info.avatar}
                        placeholder={info.placeholder}
                        size="small"
                        title={info.name}/>
          </a>
          */}
          <div className="toolbar__peer__body col-xs">
            <span className="toolbar__peer__title">{info.name}</span>
            <span className="toolbar__peer__presence">{info.presence}</span>
          </div>
        </div>
      );
    }

    return (
      <header className="toolbar">
        <div className="pull-left">
          {dialogElement}
        </div>

        <div className="toolbar__controls pull-right">
          <div className="toolbar__controls__search pull-left">
            <i className="material-icons">search</i>
            <input className="input input--search" placeholder="Search" type="search"/>
          </div>
          <div className="toolbar__controls__buttons pull-right">
            <button className={infoButtonClassName} onClick={this.onClick}>
              <i className="material-icons">info</i>
            </button>
            <button className="button button--icon">
              <i className="material-icons">more_vert</i>
            </button>
          </div>
        </div>
      </header>
    );
  }
}

export default ToolbarSection;
