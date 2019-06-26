import React from 'react';
import {
  ActivityIndicator,
  Clipboard,
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
import {
  isNameValid,
  buildURI,
  regexInvalidURI,
  CLAIM_VALUES,
  LICENSES,
  THUMBNAIL_STATUSES
} from 'lbry-redux';
import { DocumentPicker, DocumentPickerUtil } from 'react-native-document-picker';
import { RNCamera } from 'react-native-camera';
import Button from 'component/button';
import ChannelSelector from 'component/channelSelector';
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
    advancedMode: false,

    // publish
    anonymous: true,
    channelName: CLAIM_VALUES.CHANNEL_ANONYMOUS,
    priceSet: false,

    // input data
    bid: 0.1,
    description: null,
    title: null,
    name: null,
    price: 0,
    uri: null
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

  getNewUri(name, channel) {
    const { resolveUri } = this.props;
    // If they are midway through a channel creation, treat it as anonymous until it completes
    const channelName = channel === CLAIM_VALUES.CHANNEL_ANONYMOUS || channel === CLAIM_VALUES.CHANNEL_NEW ? '' : channel;

    // We are only going to store the full uri, but we need to resolve the uri with and without the channel name
    let uri;
    try {
      uri = buildURI({ contentName: name, channelName });
    } catch (e) {
      // something wrong with channel or name
    }

    if (uri) {
      if (channelName) {
        // resolve without the channel name so we know the winning bid for it
        const uriLessChannel = buildURI({ contentName: name });
        resolveUri(uriLessChannel);
      }
      resolveUri(uri);
      return uri;
    }

    return '';
  }

  handleModePressed = () => {
    this.setState({ advancedMode: !this.state.advancedMode });
  }

  handlePublishPressed = () => {
    const { notify, publish } = this.props;
    const {
      bid,
      channelName,
      currentMedia,
      description,
      name,
      price,
      priceSet,
      title,
      uri
    } = this.state;
    const thumbnail = null;

    if (!name) {
      notify({ message: 'Please specify an address where people can find your content.' });
      return;
    }

    const publishParams = {
      filePath: currentMedia.filePath,
      bid: bid || 0.1,
      title: title || '',
      thumbnail: thumbnail,
      description: description || '',
      language: 'en',
      nsfw: false,
      license: '',
      licenseUrl: '',
      otherLicenseDescription: '',
      name: name || undefined,
      contentIsFree: !priceSet,
      fee: { currency: 'LBC', price },
      uri: uri || undefined,
      channel: (CLAIM_VALUES.CHANNEL_ANONYMOUS === channelName) ? undefined : channelName,
      isStillEditing: false,
      claim: null,
    };

    this.setState({ currentPhase: Constants.PHASE_PUBLISH }, () => publish(publishParams));
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
    this.setState({
      currentMedia: media,
      title: media.name,
      name: this.formatNameForTitle(media.name),
      currentPhase: Constants.PHASE_DETAILS
    });
  }

  formatNameForTitle = (title) => {
    return title.replace(regexInvalidURI, '-').toLowerCase();
  }

  showSelector() {
    this.setState({
      currentMedia: null,
      currentPhase: Constants.PHASE_SELECTOR,

      // publish
      advancedMode: false,
      anonymous: true,
      channelName: CLAIM_VALUES.CHANNEL_ANONYMOUS,
      priceSet: false,

      // input data
      bid: 0.1,
      description: null,
      title: null,
      name: null,
      price: 0,
      uri: null
    });
  }

  handleUploadPressed = () => {
    DocumentPicker.show({
      filetype: [DocumentPickerUtil.allFiles()]
    }, (error, res) => {
      console.log(error);
      console.log('***')
      console.log(res);
      if (!error) {
        console.log(res);
      }
    });
  }

  handlePublishAgainPressed = () => {
    this.showSelector();
  }

  handleBidChange = (bid) => {
    this.setState({ bid });
  }

  handlePriceChange = (price) => {
    this.setState({ price });
  }

  handleNameChange = (name) => {
    const { notify } = this.props;
    this.setState({ name });
    if (!isNameValid(name, false)) {
      notify({ message: 'LBRY names must contain only letters, numbers and dashes.' });
      return;
    }

    const uri = this.getNewUri(name, this.state.channelName);
    this.setState({ uri });
  }

  handleChannelChanged = (channel) => {
    this.setState({ channelName: channel });
  }

  handleTitleChange = (title) => {
    this.setState({
      title,
      name: this.formatNameForTitle(title)
    }, () => {
      this.handleNameChange(this.state.name);
    });
  }

  render() {
    const { navigation, notify } = this.props;
    const { thumbnailPath } = this.state;

    let content;
    if (Constants.PHASE_SELECTOR === this.state.currentPhase) {
      content = (
        <View style={publishStyle.gallerySelector}>
          <View style={publishStyle.actionsView}>
            <RNCamera
              style={publishStyle.cameraPreview}
              ref={ref => {
                this.camera = ref;
              }}
              type={RNCamera.Constants.Type.back}
              flashMode={RNCamera.Constants.FlashMode.on}
              androidCameraPermissionOptions={{
                title: 'Camera',
                message: 'Please grant access to make use of your camera',
                buttonPositive: 'OK',
                buttonNegative: 'Cancel',
              }}
              androidRecordAudioPermissionOptions={{
                title: 'Audio',
                message: 'Please grant access to record audio',
                buttonPositive: 'OK',
                buttonNegative: 'Cancel',
              }}
            />
            <View style={publishStyle.actionsSubView}>
              <View style={publishStyle.record}>
                <Icon name="video" size={48} color={Colors.White} />
                <Text style={publishStyle.actionText}>Record</Text>
              </View>
              <View style={publishStyle.subActions}>
                <View style={publishStyle.photo}>
                  <Icon name="camera" size={48} color={Colors.White} />
                  <Text style={publishStyle.actionText}>Take a photo</Text>
                </View>
                <TouchableOpacity style={publishStyle.upload} onPress={this.handleUploadPressed}>
                  <Icon name="file-upload" size={48} color={Colors.White} />
                  <Text style={publishStyle.actionText}>Upload a file</Text>
                </TouchableOpacity>
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
            <Text style={publishStyle.cardTitle}>Title</Text>
            <TextInput
              placeholder={"Title"}
              style={publishStyle.inputText}
              value={this.state.title}
              numberOfLines={1}
              underlineColorAndroid={Colors.NextLbryGreen}
              onChangeText={this.state.handleTitleChange}
              />
          </View>

          <View style={publishStyle.card}>
            <Text style={publishStyle.cardTitle}>Description</Text>
            <TextInput
              placeholder={"Description"}
              style={publishStyle.inputText}
              value={this.state.description}
              underlineColorAndroid={Colors.NextLbryGreen}
              onChangeText={this.state.handleDescriptionChange}
              />
          </View>

          <View style={publishStyle.card}>
            <View style={publishStyle.titleRow}>
              <Text style={publishStyle.cardTitle}>Channel</Text>
            </View>

            <ChannelSelector onChannelChange={this.handleChannelChange}  />
          </View>

          {this.state.advancedMode &&
          <View style={publishStyle.card}>
            <View style={publishStyle.titleRow}>
              <Text style={publishStyle.cardTitle}>Price</Text>
              <View style={publishStyle.switchTitleRow}>
                <Switch value={this.state.priceSet} onValueChange={value => this.setState({ priceSet: value }) } />
              </View>
            </View>

            {!this.state.priceSet &&
            <Text style={publishStyle.cardText}>Your content will be free. Press the toggle to set a price.</Text>}

            {this.state.priceSet &&
            <View style={[publishStyle.inputRow, publishStyle.priceInputRow]}>
              <TextInput
                placeholder={"0.00"}
                keyboardType={'number-pad'}
                style={publishStyle.priceInput}
                underlineColorAndroid={Colors.NextLbryGreen}
                numberOfLines={1}
                value={String(this.state.price)}
                onChangeText={this.handlePriceChange}
              />
              <Text style={publishStyle.currency}>LBC</Text>
            </View>}
          </View>}

          {this.state.advancedMode &&
          <View style={publishStyle.card}>
            <Text style={publishStyle.cardTitle}>Content Address</Text>
            <Text style={publishStyle.helpText}>The address where people can find your content (ex. lbry://myvideo)</Text>

            <TextInput
              placeholder={"lbry://"}
              style={publishStyle.inputText}
              underlineColorAndroid={Colors.NextLbryGreen}
              numberOfLines={1}
              value={this.state.name}
              onChangeText={this.handleNameChange}
              />
            <View style={publishStyle.inputRow}>
              <TextInput
                placeholder={"0.00"}
                style={publishStyle.priceInput}
                underlineColorAndroid={Colors.NextLbryGreen}
                numberOfLines={1}
                keyboardType={'numeric'}
                value={String(this.state.bid)}
                onChangeText={this.handleBidChange} />
              <Text style={publishStyle.currency}>LBC</Text>
            </View>
            <Text style={publishStyle.helpText}>This LBC remains yours and the deposit can be undone at any time.</Text>
          </View>}

          <View style={publishStyle.actionButtons}>
            <Link style={publishStyle.cancelLink} text="Cancel" onPress={() => this.setState({ currentPhase: Constants.PHASE_SELECTOR })} />

            <View style={publishStyle.rightActionButtons}>
              <Button style={publishStyle.modeButton}
                text={this.state.advancedMode ? 'Simple' : 'Advanced'}
                onPress={this.handleModePressed} />
              <Button style={publishStyle.publishButton} text="Publish" onPress={this.handlePublishPressed} />
            </View>
          </View>
        </ScrollView>
      );
    } else if (Constants.PHASE_PUBLISH === this.state.currentPhase) {
      content = (
        <ScrollView style={publishStyle.publishDetails}>
          <View style={publishStyle.successContainer}>
            <Text style={publishStyle.successTitle}>Success!</Text>
            <Text style={publishStyle.successText}>Congratulations! Your content was successfully uploaded.</Text>
            <View style={publishStyle.successRow}>
              <Link style={publishStyle.successUrl} text={this.state.uri} href={this.state.uri} />
              <TouchableOpacity onPress={() => { Clipboard.setString(this.state.uri); notify({ message: 'Copied.' }); }}>
                <Icon name="clipboard" size={24} color={Colors.LbryGreen} />
              </TouchableOpacity>
            </View>
            <Text style={publishStyle.successText}>Your content will be live in a few minutes. In the mean time, feel free to publish more content or explore the app.</Text>
          </View>
          <View style={publishStyle.actionButtons}>
            <Button style={publishStyle.publishButton} text="Publish again" onPress={this.handlePublishAgainPressed} />
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
