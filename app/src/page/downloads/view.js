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
import { navigateToUri } from '../../utils/helper';
import Colors from '../../styles/colors';
import PageHeader from '../../component/pageHeader';
import FileListItem from '../../component/fileListItem';
import FloatingWalletBalance from '../../component/floatingWalletBalance';
import UriBar from '../../component/uriBar';
import downloadsStyle from '../../styles/downloads';
import fileListStyle from '../../styles/fileList';

class DownloadsPage extends React.PureComponent {
  static navigationOptions = {
    title: 'My LBRY'
  };

  componentDidMount() {
    this.props.fileList();
  }

  uriFromFileInfo(fileInfo) {
    const { name: claimName, claim_name: claimNameDownloaded, claim_id: claimId } = fileInfo;
    const uriParams = {};
    uriParams.contentName = claimName || claimNameDownloaded;
    uriParams.claimId = claimId;
    return buildURI(uriParams);
  }

  render() {
    const { fetching, fileInfos, navigation } = this.props;
    const hasDownloads = fileInfos && Object.values(fileInfos).length > 0;

    return (
      <View style={downloadsStyle.container}>
        {!fetching && !hasDownloads && <Text style={downloadsStyle.noDownloadsText}>You have not downloaded anything from LBRY yet.</Text>}
        {fetching && !hasDownloads && <ActivityIndicator size="large" color={Colors.LbryGreen} style={downloadsStyle.loading} /> }
        {hasDownloads &&
          <FlatList
            style={downloadsStyle.scrollContainer}
            contentContainerStyle={downloadsStyle.scrollPadding}
            renderItem={ ({item}) => (
                <FileListItem
                  style={fileListStyle.item}
                  uri={this.uriFromFileInfo(item)}
                  navigation={navigation}
                  onPress={() => navigateToUri(navigation, this.uriFromFileInfo(item), { autoplay: true })} />
              )
            }
            data={fileInfos.sort((a, b) => {
              // TODO: Implement sort based on user selection
              if (!a.completed && b.completed) return -1;
              if (a.completed && !b.completed) return 1;
              if (a.metadata.title === b.metadata.title) return 0;
              return (a.metadata.title < b.metadata.title) ? -1 : 1;
            })}
            keyExtractor={(item, index) => item.outpoint}
          />}
        <FloatingWalletBalance navigation={navigation} />
        <UriBar navigation={navigation} />
      </View>
    );
  }
}

export default DownloadsPage;
