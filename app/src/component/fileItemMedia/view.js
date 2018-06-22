import React from 'react';
import { ActivityIndicator, Image, Text, View } from 'react-native';
import Colors from '../../styles/colors';
import fileItemMediaStyle from '../../styles/fileItemMedia';

class FileItemMedia extends React.PureComponent {
  static AUTO_THUMB_STYLES = [
    fileItemMediaStyle.autothumbPurple,
    fileItemMediaStyle.autothumbRed,
    fileItemMediaStyle.autothumbPink,
    fileItemMediaStyle.autothumbIndigo,
    fileItemMediaStyle.autothumbBlue,
    fileItemMediaStyle.autothumbLightBlue,
    fileItemMediaStyle.autothumbCyan,
    fileItemMediaStyle.autothumbTeal,
    fileItemMediaStyle.autothumbGreen,
    fileItemMediaStyle.autothumbYellow,
    fileItemMediaStyle.autothumbOrange,
  ];

  componentWillMount() {
    this.setState({
      autoThumbStyle:
        FileItemMedia.AUTO_THUMB_STYLES[
          Math.floor(Math.random() * FileItemMedia.AUTO_THUMB_STYLES.length)
        ],
    });
  }

  render() {
    let style = this.props.style;
    const { blurRadius, isResolvingUri, thumbnail, title, resizeMode } = this.props;
    const atStyle = this.state.autoThumbStyle;

    if (thumbnail && ((typeof thumbnail) === 'string')) {
      if (style == null) {
        style = fileItemMediaStyle.thumbnail;
      }

      return (
        <Image source={{uri: thumbnail}}
               blurRadius={blurRadius}
               resizeMode={resizeMode ? resizeMode : "cover"}
               style={style} />
      );
    }

    return (
      <View style={[style ? style : fileItemMediaStyle.autothumb, atStyle]}>
        {isResolvingUri && (
          <View style={fileItemMediaStyle.resolving}>
            <ActivityIndicator color={Colors.White} size={"large"} />
            <Text style={fileItemMediaStyle.text}>Resolving...</Text>
          </View>
        )}
        {!isResolvingUri && <Text style={fileItemMediaStyle.autothumbText}>{title &&
            title
              .replace(/\s+/g, '')
              .substring(0, Math.min(title.replace(' ', '').length, 5))
              .toUpperCase()}</Text>}
      </View>
    );
  }
}

export default FileItemMedia;
