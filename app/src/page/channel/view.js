// @flow
import React from 'react';
import { ActivityIndicator, Text, View } from 'react-native';
import Colors from '../../styles/colors';
import FileList from '../../component/fileList';
import PageHeader from '../../component/pageHeader';
import UriBar from '../../component/uriBar';
import channelPageStyle from '../../styles/channelPage';

class ChannelPage extends React.PureComponent {
  componentDidMount() {
    const { uri, page, claimsInChannel, fetchClaims, fetchClaimCount } = this.props;

    if (!claimsInChannel || !claimsInChannel.length) {
      fetchClaims(uri, page || 1);
      fetchClaimCount(uri);
    }
  }

  render() {
    const { fetching, claimsInChannel, claim, navigation, uri } = this.props;
    const { name, permanent_url: permanentUrl } = claim;

    let contentList;
    if (fetching) {
      contentList = (
        <View style={channelPageStyle.busyContainer}>
          <ActivityIndicator size="large" color={Colors.LbryGreen} />
          <Text style={channelPageStyle.infoText}>Fetching content...</Text>
        </View>
      );
    } else {
      contentList =
        claimsInChannel && claimsInChannel.length ? (
          <FileList sortByHeight
                    hideFilter
                    fileInfos={claimsInChannel}
                    navigation={navigation}
                    style={channelPageStyle.fileList} />
        ) : (
          <View style={channelPageStyle.busyContainer}>
            <Text style={channelPageStyle.infoText}>No content found.</Text>
          </View>
        );
    }


    return (
      <View style={channelPageStyle.container}>
        <PageHeader title={name} onBackPressed={() => { this.props.navigation.goBack(); }} />
        {contentList}
        <UriBar value={uri} navigation={navigation} />
      </View>
    )
  } 
}

export default ChannelPage;
