import React from 'react';
import { Lbry, buildURI } from 'lbry-redux';
import {
  ActivityIndicator,
  Button,
  FlatList,
  Text,
  TextInput,
  View,
  ScrollView
} from 'react-native';
import { navigateToUri, uriFromFileInfo } from 'utils/helper';
import Colors from 'styles/colors';
import PageHeader from 'component/pageHeader';
import FileListItem from 'component/fileListItem';
import FloatingWalletBalance from 'component/floatingWalletBalance';
import StorageStatsCard from 'component/storageStatsCard';
import UriBar from 'component/uriBar';
import downloadsStyle from 'styles/downloads';
import fileListStyle from 'styles/fileList';

class DownloadsPage extends React.PureComponent {
  static navigationOptions = {
    title: 'Downloads'
  };

  componentDidMount() {
    const { fileList, pushDrawerStack } = this.props;
    pushDrawerStack();
    fileList();
  }

  render() {
    const { fetching, fileInfos, navigation } = this.props;
    const hasDownloads = fileInfos && Object.values(fileInfos).length > 0;

    return (
      <View style={downloadsStyle.container}>
        <UriBar navigation={navigation} />
        {!fetching && !hasDownloads &&
          <View style={downloadsStyle.busyContainer}>
            <Text style={downloadsStyle.noDownloadsText}>You have not downloaded anything from LBRY yet.</Text>
          </View>}
        {fetching && !hasDownloads &&
          <View style={downloadsStyle.busyContainer}>
            <ActivityIndicator size="large" color={Colors.LbryGreen} style={downloadsStyle.loading} />
          </View>}
        {hasDownloads &&
          <View style={downloadsStyle.subContainer}>
            <StorageStatsCard fileInfos={fileInfos} />
            <FlatList
              style={downloadsStyle.scrollContainer}
              contentContainerStyle={downloadsStyle.scrollPadding}
              renderItem={ ({item}) => (
                  <FileListItem
                    style={fileListStyle.item}
                    uri={uriFromFileInfo(item)}
                    navigation={navigation}
                    onPress={() => navigateToUri(navigation, uriFromFileInfo(item), { autoplay: true })} />
                )
              }
              data={fileInfos.sort((a, b) => {
                // TODO: Implement sort based on user selection
                if (!a.completed && b.completed) return -1;
                if (a.completed && !b.completed) return 1;
                if (a.metadata.title === b.metadata.title) return 0;
                return (a.metadata.title < b.metadata.title) ? -1 : 1;
              })}
              keyExtractor={(item, index) => item.download_path}
            />
          </View>}
        <FloatingWalletBalance navigation={navigation} />
      </View>
    );
  }
}

export default DownloadsPage;
