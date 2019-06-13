import React from 'react';
import { NativeModules, Text, View } from 'react-native';
import UriBar from 'component/uriBar';
import publishStyle from 'styles/reward';

class PublishPage extends React.PureComponent {
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
    const { pushDrawerStack, setPlayerVisible } = this.props;

    pushDrawerStack();
    setPlayerVisible();
    NativeModules.Gallery.getVideos().then(videos => {
      console.log(videos);
    });
  };

  componentDidMount() {
    this.onComponentFocused();
  }

  componentWillReceiveProps(nextProps) {
    const { currentRoute } = nextProps;
    const { currentRoute: prevRoute } = this.props;

    if (Constants.DRAWER_ROUTE_REWARDS === currentRoute && currentRoute !== prevRoute) {
      this.onComponentFocused();
    }
  }

  render() {
    const { navigation } = this.props;

    return (
      <View style={publishStyle.container}>
        <UriBar navigation={navigation} />
      </View>
    );
  }
}

export default PublishPage;
