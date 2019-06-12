import React from 'react';
import { ActivityIndicator, NativeModules, FlatList, Text, View } from 'react-native';
import { normalizeURI } from 'lbry-redux';
import AsyncStorage from '@react-native-community/async-storage';
import moment from 'moment';
import FileItem from 'component/fileItem';
import discoverStyle from 'styles/discover';
import fileListStyle from 'styles/fileList';
import Colors from 'styles/colors';
import Constants from 'constants';
import FloatingWalletBalance from 'component/floatingWalletBalance';
import UriBar from 'component/uriBar';

class TrendingPage extends React.PureComponent {
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
    const { fetchTrendingUris, pushDrawerStack, setPlayerVisible } = this.props;
    pushDrawerStack();
    setPlayerVisible();
    fetchTrendingUris();
  };

  componentDidMount() {
    this.onComponentFocused();
  }

  componentWillReceiveProps(nextProps) {
    const { currentRoute } = nextProps;
    const { currentRoute: prevRoute } = this.props;
    if (Constants.FULL_ROUTE_NAME_TRENDING === currentRoute && currentRoute !== prevRoute) {
      this.onComponentFocused();
    }
  }

  render() {
    const { trendingUris, fetchingTrendingUris, navigation } = this.props;
    const hasContent = typeof trendingUris === 'object' && trendingUris.length,
      failedToLoad = !fetchingTrendingUris && !hasContent;

    return (
      <View style={discoverStyle.container}>
        <UriBar navigation={navigation} />
        {!hasContent && fetchingTrendingUris && (
          <View style={discoverStyle.busyContainer}>
            <ActivityIndicator size="large" color={Colors.LbryGreen} />
            <Text style={discoverStyle.title}>Fetching content...</Text>
          </View>
        )}
        {hasContent && (
          <FlatList
            style={discoverStyle.trendingContainer}
            renderItem={({ item }) => (
              <FileItem
                style={fileListStyle.fileItem}
                mediaStyle={fileListStyle.fileItemMedia}
                key={item}
                uri={normalizeURI(item)}
                navigation={navigation}
                showDetails={true}
                compactView={false}
              />
            )}
            data={trendingUris.map(uri => uri.url)}
            keyExtractor={(item, index) => item}
          />
        )}
        <FloatingWalletBalance navigation={navigation} />
      </View>
    );
  }
}

export default TrendingPage;
