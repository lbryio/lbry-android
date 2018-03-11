import React from 'react';
import FeaturedCategory from '../../component/featuredCategory';
import { Text, View, ScrollView } from 'react-native';
import discoverStyle from '../../styles/discover';

class DiscoverPage extends React.PureComponent {
  static navigationOptions = {
    title: 'Discover'
  };
  
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
