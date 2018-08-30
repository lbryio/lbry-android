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
import Icon from 'react-native-vector-icons/FontAwesome5';
import NavigationButton from '../navigationButton';
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
            <NavigationButton
              name="arrow-left"
              style={pageHeaderStyle.left}
              size={24}
              iconStyle={pageHeaderStyle.backIcon}
              onPress={onBackPressed}
            />
          </View>
        </View>
      </View>
    );
  }
}

export default PageHeader;
