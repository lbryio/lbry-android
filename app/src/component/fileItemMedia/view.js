import React from 'react';
import { Text, Image, View } from 'react-native';
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
    const { title, thumbnail } = this.props;
    const atStyle = this.state.autoThumbStyle;

    if (thumbnail && ((typeof thumbnail) === 'string')) {
      return (
        <Image source={{uri: thumbnail }} resizeMode="cover" style={fileItemMediaStyle.thumbnail} />
      )
    }
    return (
      <View style={[fileItemMediaStyle.autothumb, atStyle]}>
        <Text style={fileItemMediaStyle.autothumbText}>{title &&
            title
              .replace(/\s+/g, '')
              .substring(0, Math.min(title.replace(' ', '').length, 5))
              .toUpperCase()}</Text>
      </View>
    );
  }
}

export default FileItemMedia;
