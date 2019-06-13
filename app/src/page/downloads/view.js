import React from 'react';
import { Lbry, buildURI } from 'lbry-redux';
import { ActivityIndicator, Button, FlatList, Text, TextInput, View, ScrollView } from 'react-native';
import { navigateToUri, uriFromFileInfo } from 'utils/helper';
import Colors from 'styles/colors';
import Constants from 'constants';
import PageHeader from 'component/pageHeader';
import FileListItem from 'component/fileListItem';
import FloatingWalletBalance from 'component/floatingWalletBalance';
import StorageStatsCard from 'component/storageStatsCard';
import UriBar from 'component/uriBar';
import downloadsStyle from 'styles/downloads';
import fileListStyle from 'styles/fileList';

class DownloadsPage extends React.PureComponent {
  static navigationOptions = {
    title: 'Downloads',
  };

  didFocusListener;

  componentWillMount() {
    const { navigation } = this.props;
    this.didFocusListener = navigation.addListener('didFocus', this.onComponentFocused);
  }

  componentWillUnmount() {
    if (this.didFocusListener) {
      this.didFocusListener.remove();
    }
  }

  onComponentFocused = () => {
    const { fileList, pushDrawerStack, setPlayerVisible } = this.props;
    pushDrawerStack();
    setPlayerVisible();
    fileList();
  };

  componentDidMount() {
    this.onComponentFocused();
  }

  componentWillReceiveProps(nextProps) {
    const { currentRoute } = nextProps;
    const { currentRoute: prevRoute } = this.props;
    if (Constants.FULL_ROUTE_NAME_MY_LBRY === currentRoute && currentRoute !== prevRoute) {
      this.onComponentFocused();
    }
  }

  render() {
    const { fetching, fileInfos, navigation } = this.props;
    const hasDownloads = fileInfos && Object.values(fileInfos).length > 0;

    return (
      <View style={downloadsStyle.container}>
        <UriBar navigation={navigation} />
        {!fetching && !hasDownloads && (
          <View style={downloadsStyle.busyContainer}>
            <Text style={downloadsStyle.noDownloadsText}>
              You have not watched or downloaded any content from LBRY yet.
            </Text>
          </View>
        )}
        {fetching && !hasDownloads && (
          <View style={downloadsStyle.busyContainer}>
            <ActivityIndicator size="large" color={Colors.LbryGreen} style={downloadsStyle.loading} />
          </View>
        )}
        {hasDownloads && (
          <View style={downloadsStyle.subContainer}>
            <StorageStatsCard fileInfos={fileInfos} />
            <FlatList
              style={downloadsStyle.scrollContainer}
              contentContainerStyle={downloadsStyle.scrollPadding}
              renderItem={({ item }) => (
                <FileListItem
                  style={fileListStyle.item}
                  uri={uriFromFileInfo(item)}
                  navigation={navigation}
                  onPress={() => navigateToUri(navigation, uriFromFileInfo(item), { autoplay: true })}
                />
              )}
              data={fileInfos}
              keyExtractor={(item, index) => item.outpoint}
            />
          </View>
        )}
        <FloatingWalletBalance navigation={navigation} />
      </View>
    );
  }
}

export default DownloadsPage;
