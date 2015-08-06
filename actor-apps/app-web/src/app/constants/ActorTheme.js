import { Styles, Utils } from 'material-ui';
const Colors = Styles.Colors;
const Spacing = Styles.Spacing;
const ColorManipulator = Utils.ColorManipulator;

const ActorTheme = {
  spacing: Spacing,
  contentFontFamily: 'RobotoDraft, Roboto, sans-serif',

  getPalette() {
    return {
      primary1Color: '#4a90e2',
      primary2Color: '#486181',
      primary3Color: '#5191db',
      accent1Color: Colors.white,
      textColor: '#4d4d4d'
      //accent2Color: Colors.pinkA400,
      //accent3Color: Colors.pinkA100,
      //textColor: Colors.darkBlack,
      //canvasColor: Colors.white,
      //borderColor: Colors.grey300,
      //disabledColor: ColorManipulator.fade(Colors.darkBlack, 0.3)
    };
  },

  getComponentThemes(palette, spacing) {
    spacing = spacing || Spacing;
    let obj = {
      raisedButton: {
        color: palette.primary1Color,
        textColor: Colors.white
      },
      textField: {
        textColor: Colors.white,
        floatingLabelColor: Colors.white,
        disabledTextColor: Colors.white,
        hintColor: ColorManipulator.fade(Colors.white, .3),
        errorColor: '#eb8080',
        focusColor: palette.primary1Color,
        backgroundColor: 'transparent',
        borderColor: ColorManipulator.fade(Colors.white, .12)
      },
      tabs: {
        backgroundColor: palette.primary2Color
      },
      radioButton: {
        borderColor: ColorManipulator.fade(Colors.black, .15),
        checkedColor: palette.primary3Color,
        requiredColor: palette.primary3Color
      },
      dropDownMenu: {
        accentColor: palette.primary3Color
      },
      menuItem: {
        hoverColor: '#f5f6f7',
        selectedTextColor: palette.primary3Color
      }
    };

    return obj;
  },

  getSnackbarStyles() {
    return {
      left: 'auto',
      marginLeft: 0,
      right: 0,
      height: '40px',
      lineHeight: '40px',
      color: 'white',
      marginRight: 24,
      pointerEvents: 'none',
      backgroundColor: '#4a90e2',
      textShadow: '0 1px 1px rgba(0,0,0,.3)'
    };
  }
};

export default ActorTheme;
