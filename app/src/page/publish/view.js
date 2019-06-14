import React from 'react';
import {
  ActivityIndicator,
  Image,
  NativeModules,
  Picker,
  ScrollView,
  Switch,
  Text,
  TextInput,
  TouchableOpacity,
  View
} from 'react-native';
import { FlatGrid } from 'react-native-super-grid';
import Button from 'component/button';
import Colors from 'styles/colors';
import Constants from 'constants';
import FastImage from 'react-native-fast-image';
import FloatingWalletBalance from 'component/floatingWalletBalance';
import Icon from 'react-native-vector-icons/FontAwesome5';
import Link from 'component/link';
import UriBar from 'component/uriBar';
import publishStyle from 'styles/publish';

class PublishPage extends React.PureComponent {
  state = {
    thumbnailPath: null,
    videos: null,
    currentMedia: null,
    currentPhase: Constants.PHASE_SELECTOR,

    // publish
    anonymous: true,
    channelName: null,
    priceFree: true,
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
    const { pushDrawerStack, setPlayerVisible } = this.props;

    pushDrawerStack();
    setPlayerVisible();
    NativeModules.Gallery.getThumbnailPath().then(thumbnailPath => {
      if (thumbnailPath != null) {
        this.setState({ thumbnailPath });
      }
    });
    NativeModules.Gallery.getVideos().then(videos => {
      this.setState({ videos });
    });
  };

  componentDidMount() {
    this.onComponentFocused();
  }

  componentWillReceiveProps(nextProps) {
    const { currentRoute } = nextProps;
    const { currentRoute: prevRoute } = this.props;

    if (Constants.DRAWER_ROUTE_PUBLISH === currentRoute && currentRoute !== prevRoute) {
      this.onComponentFocused();
    }
  }

  setCurrentMedia(media) {
    this.setState({ currentMedia: media, currentPhase: Constants.PHASE_DETAILS });
  }

  showSelector() {
    this.setState({
      currentMedia: null,
      currentPhase: Constants.PHASE_SELECTOR,
      // reset publish state
      anonymous: true,
      channelName: null,
      priceFree: true
    });
  }

  render() {
    const { navigation } = this.props;
    const { thumbnailPath } = this.state;

    let content;
    if (Constants.PHASE_SELECTOR === this.state.currentPhase) {
      content = (
        <View style={publishStyle.gallerySelector}>
          <View style={publishStyle.actionsView}>
            <View style={publishStyle.record}>
              <Icon name="video" size={48} color={Colors.White} />
              <Text style={publishStyle.actionText}>Record</Text>
            </View>
            <View style={publishStyle.subActions}>
              <View style={publishStyle.photo}>
                <Icon name="camera" size={48} color={Colors.White} />
                <Text style={publishStyle.actionText}>Take a photo</Text>
              </View>
              <View style={publishStyle.upload}>
                <Icon name="file-upload" size={48} color={Colors.White} />
                <Text style={publishStyle.actionText}>Upload a file</Text>
              </View>
            </View>
          </View>
          {(!this.state.videos || !thumbnailPath) &&
            <View style={publishStyle.loadingView}>
              <ActivityIndicator size='large' color={Colors.LbryGreen} />
            </View>
          }
          {(this.state.videos && thumbnailPath) &&
          <FlatGrid
            style={publishStyle.galleryGrid}
            itemDimension={134}
            spacing={2}
            items={this.state.videos}
            renderItem={({ item, index }) => {
              return (
                <TouchableOpacity key={index} onPress={() => this.setCurrentMedia(item)}>
                  <FastImage
                    style={publishStyle.galleryGridImage}
                    resizeMode={FastImage.resizeMode.cover}
                    source={{ uri: `file://${thumbnailPath}/${item.id}.png` }} />
                </TouchableOpacity>
              );
            }}
          />}
        </View>
      );
    } else if (Constants.PHASE_DETAILS === this.state.currentPhase && this.state.currentMedia) {
      const { currentMedia } = this.state;
      content = (
        <ScrollView style={publishStyle.publishDetails}>
          <View style={publishStyle.mainThumbnailContainer}>
            <FastImage
              style={publishStyle.mainThumbnail}
              resizeMode={FastImage.resizeMode.contain}
              source={{ uri: `file://${thumbnailPath}/${currentMedia.id}.png` }}
              />
          </View>

          <View style={publishStyle.card}>
            <TextInput
              placeholder={"Title"}
              style={publishStyle.inputText}
              value={currentMedia.name}
              numberOfLines={1}
              underlineColorAndroid={Colors.NextLbryGreen} />
            <TextInput
              placeholder={"Description"}
              style={publishStyle.inputText}
              underlineColorAndroid={Colors.NextLbryGreen} />
          </View>

          <View style={publishStyle.card}>
            <Text style={publishStyle.cardTitle}>Price</Text>

            <View style={publishStyle.cardRow}>
              <View style={publishStyle.switchRow}>
                <Switch value={this.state.priceFree} onValueChange={value => this.setState({ priceFree: value }) } />
                <Text style={publishStyle.switchText}>Free</Text>
              </View>

              {!this.state.priceFree &&
              <View style={[publishStyle.inputRow, publishStyle.priceInputRow]}>
                <TextInput placeholder={"0.00"} style={publishStyle.priceInput} underlineColorAndroid={Colors.NextLbryGreen} numberOfLines={1} />
                <Text style={publishStyle.currency}>LBC</Text>
              </View>}
            </View>
          </View>

          <View style={publishStyle.card}>
            <Text style={publishStyle.cardTitle}>Publish anonymously or as a channel?</Text>
            <View style={publishStyle.cardRow}>
              <View style={publishStyle.switchRow}>
                <Switch value={this.state.anonymous} onValueChange={value => this.setState({ anonymous: value }) } />
                <Text style={publishStyle.switchText}>Anonymous</Text>
              </View>

              {!this.state.anonymous &&
              <Picker
                selectedValue={this.state.channelName}
                style={publishStyle.channelPicker}
                onValueChange={(itemValue, itemIndex) =>
                  this.setState({channelName: itemValue})
                }>
                <Picker.Item label="Select..." value={null} />
              </Picker>}
            </View>
          </View>

          <View style={publishStyle.card}>
            <Text style={publishStyle.cardTitle}>Where can people find this content?</Text>
            <Text style={publishStyle.helpText}>The LBRY URL is the exact address where people can find your content (ex. lbry://myvideo)</Text>

            <TextInput placeholder={"lbry://"} style={publishStyle.inputText} underlineColorAndroid={Colors.NextLbryGreen} numberOfLines={1} />
            <View style={publishStyle.inputRow}>
              <TextInput placeholder={"0.00"} style={publishStyle.priceInput} underlineColorAndroid={Colors.NextLbryGreen} numberOfLines={1} />
              <Text style={publishStyle.currency}>LBC</Text>
            </View>
            <Text style={publishStyle.helpText}>This LBC remains yours and the deposit can be undone at any time.</Text>
          </View>

          <View style={publishStyle.actionButtons}>
            <Link style={publishStyle.cancelLink} text="Cancel" onPress={() => this.setState({ currentPhase: Constants.PHASE_SELECTOR })} />
            <Button style={publishStyle.publishButton} text="Publish" />
          </View>
        </ScrollView>
      );
    }

    return (
      <View style={publishStyle.container}>
        <UriBar navigation={navigation} />
        {content}
        {(false && Constants.PHASE_SELECTOR !== this.state.currentPhase) && <FloatingWalletBalance navigation={navigation} />}
      </View>
    );
  }
}

export default PublishPage;
