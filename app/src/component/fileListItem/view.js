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
import { navigateToUri, formatBytes } from '../../utils/helper';
import Colors from '../../styles/colors';
import FileItemMedia from '../fileItemMedia';
import Link from '../../component/link';
import NsfwOverlay from '../../component/nsfwOverlay';
import fileListStyle from '../../styles/fileList';

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
      isResolvingUri,
      isDownloaded,
      style,
      onPress,
      navigation
    } = this.props;

    const uri = normalizeURI(this.props.uri);
    const obscureNsfw = this.props.obscureNsfw && metadata && metadata.nsfw;
    const isResolving = !fileInfo && isResolvingUri;
    const title = fileInfo ? fileInfo.metadata.title : metadata && metadata.title ? metadata.title : parseURI(uri).contentName;

    let name;
    let channel;
    if (claim) {
      name = claim.name;
      channel = claim.channel_name;
    }

    return (
      <View style={style}>
        <TouchableOpacity style={style} onPress={onPress}>
          <FileItemMedia style={fileListStyle.thumbnail}
                         blurRadius={obscureNsfw ? 15 : 0}
                         resizeMode="cover"
                         title={title}
                         thumbnail={metadata ? metadata.thumbnail : null} />
          <View style={fileListStyle.detailsContainer}>
            {!title && !name && !channel && isResolving && (
            <View>
              {(!title && !name) && <Text style={fileListStyle.uri}>{uri}</Text>}
              {(!title && !name) && <View style={fileListStyle.row}>
                <ActivityIndicator size={"small"} color={Colors.LbryGreen} />
              </View>}
            </View>)}

            {title && <Text style={fileListStyle.title}>{this.formatTitle(title) || this.formatTitle(name)}</Text>}
            {channel &&
              <Link style={fileListStyle.publisher} text={channel} onPress={() => {
                const channelUri = normalizeURI(channel);
                navigateToUri(navigation, channelUri);
              }} />}

            {fileInfo &&
              <View style={fileListStyle.downloadInfo}>
                <Text style={fileListStyle.downloadStorage}>{this.getStorageForFileInfo(fileInfo)}</Text>
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
