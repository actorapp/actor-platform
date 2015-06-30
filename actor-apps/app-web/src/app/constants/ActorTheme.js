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
      primary2Color: Colors.cyan700,
      primary3Color: Colors.cyan100,
      accent1Color: Colors.pinkA200,
      accent2Color: Colors.pinkA400,
      accent3Color: Colors.pinkA100,
      textColor: Colors.darkBlack,
      canvasColor: Colors.white,
      borderColor: Colors.grey300,
      disabledColor: ColorManipulator.fade(Colors.darkBlack, 0.3)
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
      }
    };

    return obj;
  }
};

export default ActorTheme;
