import React from 'react';
import { normalizeURI, parseURI } from 'lbry-redux';
import {
  ActivityIndicator,
  Platform,
  ProgressBarAndroid,
  Text,
  TouchableOpacity,
  View
} from 'react-native';
import { navigateToUri, formatBytes } from 'utils/helper';
import Colors from 'styles/colors';
import DateTime from 'component/dateTime';
import FileItemMedia from 'component/fileItemMedia';
import Icon from 'react-native-vector-icons/FontAwesome5';
import Link from 'component/link';
import NsfwOverlay from 'component/nsfwOverlay';
import fileListStyle from 'styles/fileList';

class FileListItem extends React.PureComponent {
  getStorageForFileInfo = (fileInfo) => {
    if (!fileInfo.completed) {
      const written = formatBytes(fileInfo.written_bytes);
      const total = formatBytes(fileInfo.total_bytes);
      return `(${written} / ${total})`;
    }

    return formatBytes(fileInfo.written_bytes);
  }

  formatTitle = (title) => {
    if (!title) {
      return title;
    }

    return (title.length > 80) ? title.substring(0, 77).trim() + '...' : title;
  }

  getDownloadProgress = (fileInfo) => {
    return Math.ceil((fileInfo.written_bytes / fileInfo.total_bytes) * 100);
  }

  componentDidMount() {
    const { claim, resolveUri, uri } = this.props;
    if (!claim) {
      resolveUri(uri);
    }
  }

  render() {
    const {
      claim,
      fileInfo,
      metadata,
      featuredResult,
      isResolvingUri,
      isDownloaded,
      style,
      onPress,
      navigation,
      thumbnail,
      title
    } = this.props;

    const uri = normalizeURI(this.props.uri);
    const obscureNsfw = this.props.obscureNsfw && metadata && metadata.nsfw;
    const isResolving = !fileInfo && isResolvingUri;

    let name, channel, height, channelClaimId, fullChannelUri;
    if (claim) {
      name = claim.name;
      channel = claim.channel_name;
      height = claim.height;
      channelClaimId = claim.value && claim.value.publisherSignature && claim.value.publisherSignature.certificateId;
      fullChannelUri = channelClaimId ? `${channel}#${channelClaimId}` : channel;
    }

    if (featuredResult && !isResolvingUri && !claim && !title && !name) {
      return null;
    }

    return (
      <View style={style}>
        <TouchableOpacity style={style} onPress={onPress}>
          <FileItemMedia style={fileListStyle.thumbnail}
                         blurRadius={obscureNsfw ? 15 : 0}
                         resizeMode="cover"
                         title={(title || name)}
                         thumbnail={thumbnail} />
          {fileInfo && fileInfo.completed && <Icon style={fileListStyle.downloadedIcon} solid={true} color={Colors.BrightGreen} name={"folder"} size={16} />}
          <View style={fileListStyle.detailsContainer}>
            {featuredResult && <Text style={fileListStyle.featuredUri} numberOfLines={1}>{uri}</Text>}

            {!title && !name && !channel && isResolving && (
            <View>
              {(!title && !name) && <Text style={fileListStyle.uri}>{uri}</Text>}
              {(!title && !name) && <View style={fileListStyle.row}>
                <ActivityIndicator size={"small"} color={featuredResult ? Colors.White : Colors.LbryGreen} />
              </View>}
            </View>)}

            {(title || name) && <Text style={featuredResult ? fileListStyle.featuredTitle : fileListStyle.title}>{this.formatTitle(title) || this.formatTitle(name)}</Text>}
            {channel &&
              <Link style={fileListStyle.publisher} text={channel} onPress={() => {
                navigateToUri(navigation, normalizeURI(fullChannelUri));
              }} />}

            <View style={fileListStyle.info}>
              {fileInfo && <Text style={fileListStyle.infoText}>{this.getStorageForFileInfo(fileInfo)}</Text>}
              <DateTime style={fileListStyle.publishInfo} textStyle={fileListStyle.infoText} timeAgo uri={uri} />
            </View>

            {fileInfo &&
              <View style={fileListStyle.downloadInfo}>
                {!fileInfo.completed &&
                  <View style={fileListStyle.progress}>
                    <View style={[fileListStyle.progressCompleted, { flex: this.getDownloadProgress(fileInfo) } ]} />
                    <View style={[fileListStyle.progressRemaining, { flex: (100 - this.getDownloadProgress(fileInfo)) } ]} />
                  </View>}
              </View>
            }
          </View>
        </TouchableOpacity>
        {obscureNsfw && <NsfwOverlay onPress={() => navigation.navigate({ routeName: 'Settings', key: 'settingsPage' })} />}
      </View>
    );
  }
}

export default FileListItem;
