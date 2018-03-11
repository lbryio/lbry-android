import React from 'react';
import FeaturedCategory from '../../component/featuredCategory';
import NavigationActions from 'react-navigation';
import { Text, View, ScrollView } from 'react-native';
import discoverStyle from '../../styles/discover';
import Feather from 'react-native-vector-icons/Feather';

class DiscoverPage extends React.PureComponent {
  componentWillMount() {
    this.props.fetchFeaturedUris();
  }

  render() {
    const { featuredUris, fetchingFeaturedUris } = this.props;
    const hasContent = typeof featuredUris === 'object' && Object.keys(featuredUris).length,
      failedToLoad = !fetchingFeaturedUris && !hasContent;

    return (
      <View style={discoverStyle.container}>
        {!hasContent && fetchingFeaturedUris && <Text style={discoverStyle.title}>Fetching content...</Text>}
        {hasContent &&
          <ScrollView style={discoverStyle.scrollContainer}>
          {hasContent &&
            Object.keys(featuredUris).map(
              category =>
                featuredUris[category].length ? (
                  <FeaturedCategory
                      key={category}
                      category={category}
                      names={featuredUris[category]}
                      navigation={this.props.navigation}
                    />
                ) : (
                  ''
                )
            )}
          </ScrollView>
        }
      </View>
    );
  }
}

export default DiscoverPage;
