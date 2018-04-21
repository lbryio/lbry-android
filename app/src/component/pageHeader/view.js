// Based on https://github.com/react-navigation/react-navigation/blob/master/src/views/Header/Header.js
import React from 'react';
import {
  Animated,
  Platform,
  StyleSheet,
  Text,
  TouchableOpacity,
  View
} from 'react-native';
import Feather from 'react-native-vector-icons/Feather';
import pageHeaderStyle from '../../styles/pageHeader';

const APPBAR_HEIGHT = Platform.OS === 'ios' ? 44 : 56;
const AnimatedText = Animated.Text;

class PageHeader extends React.PureComponent {
  render() {
    const { title, onBackPressed } = this.props;
    const containerStyles = [
      pageHeaderStyle.container,
      { height: APPBAR_HEIGHT }
    ];
    
    return (
      <View style={containerStyles}>
        <View style={pageHeaderStyle.flexOne}>
          <View style={pageHeaderStyle.header}>
            <View style={pageHeaderStyle.title}>
              <AnimatedText
                numberOfLines={1}
                style={pageHeaderStyle.titleText}
                accessibilityTraits="header">
                {title}
              </AnimatedText>
            </View>
            <TouchableOpacity style={pageHeaderStyle.left}>
              <Feather name="arrow-left" size={24} onPress={onBackPressed} style={pageHeaderStyle.backIcon} />
            </TouchableOpacity>
          </View>
        </View>
      </View>
    );
  }
}

export default PageHeader;
