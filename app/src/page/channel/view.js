// @flow
import React from 'react';
import { ScrollView, Text, View } from 'react-native';
import UriBar from '../../component/uriBar';
import channelPageStyle from '../../styles/channelPage';

class ChannelPage extends React.PureComponent {
  render() {
    const { claim, navigation, uri } = this.props;
    const { name } = claim;

    return (
      <View style={channelPageStyle.container}>
        <Text style={channelPageStyle.title}>{name}</Text>
        <ScrollView style={channelPageStyle.content}>
          
        </ScrollView>
        <UriBar value={uri} navigation={navigation} />
      </View>
    )
  } 
}

export default ChannelPage;
