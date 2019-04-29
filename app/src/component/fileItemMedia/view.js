import React from 'react';
import { ActivityIndicator, Image, Text, View } from 'react-native';
import Colors from 'styles/colors';
import FastImage from 'react-native-fast-image'
import fileItemMediaStyle from 'styles/fileItemMedia';

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

  state: {
    imageLoadFailed: false
  };

  componentWillMount() {
    this.setState({
      autoThumbStyle:
        FileItemMedia.AUTO_THUMB_STYLES[
          Math.floor(Math.random() * FileItemMedia.AUTO_THUMB_STYLES.length)
        ],
    });
  }

  getFastImageResizeMode(resizeMode) {
    switch (resizeMode) {
      case "contain":
        return FastImage.resizeMode.contain;
      case "stretch":
        return FastImage.resizeMode.stretch;
      case "center":
        return FastImage.resizeMode.center;
      default:
        return FastImage.resizeMode.cover;
    }
  }

  isThumbnailValid = (thumbnail) => {
    if (!thumbnail || ((typeof thumbnail) !== 'string')) {
      return false;
    }

    if (thumbnail.substring(0, 7) != 'http://' && thumbnail.substring(0, 8) != 'https://') {
      return false;
    }

    return true;
  }

  render() {
    let style = this.props.style;
    const { blurRadius, isResolvingUri, thumbnail, title, resizeMode } = this.props;
    const atStyle = this.state.autoThumbStyle;
    if (this.isThumbnailValid(thumbnail) && !this.state.imageLoadFailed) {
      if (style == null) {
        style = fileItemMediaStyle.thumbnail;
      }

      if (blurRadius > 0) {
        // No blur radius support in FastImage yet
        return (
          <Image
            source={{uri: thumbnail}}
            blurRadius={blurRadius}
            resizeMode={resizeMode ? resizeMode : "cover"}
            style={style}
          />);
      }

      return (
        <FastImage
          source={{uri: thumbnail}}
          onError={() => this.setState({ imageLoadFailed: true })}
          resizeMode={this.getFastImageResizeMode(resizeMode)}
          style={style}
        />
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
