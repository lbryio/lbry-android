import React from 'react';
import { Linking, Text, TouchableOpacity } from 'react-native';

export default class Link extends React.PureComponent {

  constructor(props) {
    super(props)
    this.state = {
      tappedStyle: false,
    }
    this.addTappedStyle = this.addTappedStyle.bind(this)
  }

  handlePress = () => {
    const { error, href, navigation, notify } = this.props;

    if (navigation && href.startsWith('#')) {
      navigation.navigate(href.substring(1));
    } else {
      if (this.props.effectOnTap) this.addTappedStyle();
      Linking.openURL(href)
        .then(() => setTimeout(() => { this.setState({ tappedStyle: false }); }, 2000))
      .catch(err => {
        notify({ message: error, isError: true })
        this.setState({tappedStyle: false})
      }
    );
    }
  }

  addTappedStyle() {
    this.setState({ tappedStyle: true });
    setTimeout(() => { this.setState({ tappedStyle: false }); }, 2000);
  }

  render() {
    const {
      ellipsizeMode,
      numberOfLines,
      onPress,
      style,
      text
    } = this.props;

    let styles = [];
    if (style) {
      if (style.length) {
        styles = styles.concat(style);
      } else {
        styles.push(style);
      }
    }

    if (this.props.effectOnTap && this.state.tappedStyle) {
      styles.push(this.props.effectOnTap);
    }

    return (
      <Text
        style={styles}
        numberOfLines={numberOfLines}
        ellipsizeMode={ellipsizeMode}
        onPress={onPress ? onPress : this.handlePress}>
        {text}
      </Text>
    );
  }
};
