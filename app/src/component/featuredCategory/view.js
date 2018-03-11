import React from 'react';
import { Text, View } from 'react-native';
import { normalizeURI } from 'lbry-redux';
import FileItem from '../fileItem';
import discoverStyle from '../../styles/discover';

class FeaturedCategory extends React.PureComponent {
  render() {
    const { category, names, categoryLink } = this.props;

    return (
      <View>
        <Text style={discoverStyle.categoryName}>{category}</Text>
          {names &&
            names.map(name => (
              <FileItem style={discoverStyle.fileItem} key={name} uri={normalizeURI(name)} />
            ))}
      </View>
    );
  }
}

export default FeaturedCategory;