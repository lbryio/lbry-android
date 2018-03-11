import React from 'react';
import {AppRegistry, StyleSheet, Text, View} from 'react-native';

class InfoComponent extends React.Component {
  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.title}>LBRY UI</Text>
      </View>
    );
  }
}
var styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
  },
  title: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
});

AppRegistry.registerComponent('LBRYApp', () => InfoComponent);
