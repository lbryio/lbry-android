import React from 'react';
import NavigationActions from 'react-navigation';
import {
  ActivityIndicator,
  AsyncStorage,
  NativeModules,
  FlatList,
  Text,
  View
} from 'react-native';
import { normalizeURI } from 'lbry-redux';
import moment from 'moment';
import FileItem from '../../component/fileItem';
import discoverStyle from '../../styles/discover';
import Colors from '../../styles/colors';
import UriBar from '../../component/uriBar';
import Feather from 'react-native-vector-icons/Feather';

class TrendingPage extends React.PureComponent {
  componentDidMount() {
    this.props.fetchTrendingUris();
  }

  render() {
    const { trendingUris, fetchingTrendingUris, navigation } = this.props;
    const hasContent = typeof trendingUris === 'object' && trendingUris.length,
      failedToLoad = !fetchingTrendingUris && !hasContent;

    return (
      <View style={discoverStyle.container}>
        {!hasContent && fetchingTrendingUris && (
          <View style={discoverStyle.busyContainer}>
            <ActivityIndicator size="large" color={Colors.LbryGreen} />
            <Text style={discoverStyle.title}>Fetching content...</Text>
          </View>
        )}
        {hasContent &&
          <FlatList style={discoverStyle.trendingContainer}
            renderItem={ ({item}) => (
                <FileItem
                  style={discoverStyle.fileItem}
                  key={item}
                  uri={normalizeURI(item)}
                  navigation={navigation} />
              )
            }
            data={trendingUris.map(uri => uri.url)}
            keyExtractor={(item, index) => item}
          />
        }
        <UriBar navigation={navigation} />
      </View>
    );
  }
}

export default TrendingPage;
