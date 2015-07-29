import React from 'react';

import DialogStore from 'stores/DialogStore';

import ActivityActionCreators from 'actions/ActivityActionCreators';

//import AvatarItem from 'components/common/AvatarItem.react';

const getStateFromStores = () => {
  return {
    dialogInfo: DialogStore.getSelectedDialogInfo()
  };
};

class ToolbarSection extends React.Component {
  state = {
    dialogInfo: null
  };

  constructor(props) {
    super(props);

    DialogStore.addSelectedChangeListener(this.onChange);
  }

  componentWillUnmount() {
    DialogStore.removeSelectedChangeListener(this.onChange);
  }

  onClick = () => {
    ActivityActionCreators.show();
  };

  onChange = () => {
    this.setState(getStateFromStores());
  };

  render() {
    const info = this.state.dialogInfo;
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
    }

    return (
      <header className="toolbar">
        {dialogElement}
      </header>
    );
  }
}

export default ToolbarSection;
