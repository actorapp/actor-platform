import React from 'react';

import DialogStore from '../stores/DialogStore';

import ActivityActionCreators from '../actions/ActivityActionCreators';

//import AvatarItem from './common/AvatarItem.react';

var getStateFromStores = () => {
  return {dialogInfo: null};
};

class ToolbarSection extends React.Component {
  componentWillMount() {
    DialogStore.addSelectedChangeListener(this.onChange);
  }

  componentWillUnmount() {
    DialogStore.removeSelectedChangeListener(this.onChange);
  }

  constructor() {
    super();

    this.onClick = this.onClick.bind(this);
    this.onChange = this.onChange.bind(this);

    this.state = getStateFromStores();
  }

  onClick() {
    ActivityActionCreators.show();
  }

  onChange() {
    this.setState({dialogInfo: DialogStore.getSelectedDialogInfo()});
  }

  render() {
    let info = this.state.dialogInfo;
    let dialogElement;

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
            <span className="toolbar__peer__title" onClick={this.onClick}>{info.name}</span>
            <span className="toolbar__peer__presence">{info.presence}</span>
          </div>
        </div>
      );
    } else {
      dialogElement = null;
    }

    return (
      <header className="toolbar">
        {dialogElement}
      </header>
    );
  }
}

export default ToolbarSection;
