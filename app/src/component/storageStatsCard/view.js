import React from 'react';
import { normalizeURI, parseURI } from 'lbry-redux';
import {
  ActivityIndicator,
  Platform,
  Switch,
  Text,
  TouchableOpacity,
  View
} from 'react-native';
import { formatBytes } from '../../utils/helper';
import Colors from '../../styles/colors';
import storageStatsStyle from '../../styles/storageStats';

class StorageStatsCard extends React.PureComponent {
  state = {
    totalBytes: 0,
    totalAudioBytes: 0,
    totalAudioPercent: 0,
    totalImageBytes: 0,
    totalImagePercent: 0,
    totalVideoBytes: 0,
    totalVideoPercent: 0,
    totalOtherBytes: 0,
    totalOtherPercent: 0,
    showStats: false
  };

  componentDidMount() {
    // calculate total bytes
    const { fileInfos } = this.props;

    let totalBytes = 0, totalAudioBytes = 0, totalImageBytes = 0, totalVideoBytes = 0;
    let totalAudioPercent = 0, totalImagePercent = 0, totalVideoPercent = 0;

    fileInfos.forEach(fileInfo => {
      if (fileInfo.completed) {
        const bytes = fileInfo.written_bytes;
        const type = fileInfo.mime_type;
        totalBytes += bytes;
        if (type) {
          if (type.startsWith('audio/')) totalAudioBytes += bytes;
          if (type.startsWith('image/')) totalImageBytes += bytes;
          if (type.startsWith('video/')) totalVideoBytes += bytes;
        }
      }
    });

    totalAudioPercent = ((totalAudioBytes / totalBytes) * 100).toFixed(2);
    totalImagePercent = ((totalImageBytes / totalBytes) * 100).toFixed(2);
    totalVideoPercent = ((totalVideoBytes / totalBytes) * 100).toFixed(2);

    this.setState({
      totalBytes,
      totalAudioBytes,
      totalAudioPercent,
      totalImageBytes,
      totalImagePercent,
      totalVideoBytes,
      totalVideoPercent,
      totalOtherBytes: totalBytes - (totalAudioBytes + totalImageBytes + totalVideoBytes),
      totalOtherPercent: (100 - (parseFloat(totalAudioPercent) +
                                 parseFloat(totalImagePercent) +
                                 parseFloat(totalVideoPercent))).toFixed(2)
    });
  }

  render() {
    return (
      <View style={storageStatsStyle.card}>
        <View style={[storageStatsStyle.row, storageStatsStyle.totalSizeContainer]}>
          <View style={storageStatsStyle.summary}>
            <Text style={storageStatsStyle.totalSize}>{formatBytes(this.state.totalBytes, 2)}</Text>
            <Text style={storageStatsStyle.annotation}>used</Text>
          </View>
          <View style={[storageStatsStyle.row, storageStatsStyle.toggleStatsContainer]}>
            <Text style={storageStatsStyle.statsText}>Stats</Text>
            <Switch
              style={storageStatsStyle.statsToggle}
              value={this.state.showStats}
              onValueChange={(value) => this.setState({ showStats: value })} />
          </View>
        </View>
        {this.state.showStats &&
        <View>
          <View style={storageStatsStyle.distributionBar}>
            <View style={[storageStatsStyle.audioDistribution, { flex: parseFloat(this.state.totalAudioPercent) }]} />
            <View style={[storageStatsStyle.imageDistribution, { flex: parseFloat(this.state.totalImagePercent) }]} />
            <View style={[storageStatsStyle.videoDistribution, { flex: parseFloat(this.state.totalVideoPercent) }]} />
            <View style={[storageStatsStyle.otherDistribution, { flex: parseFloat(this.state.totalOtherPercent) }]} />
          </View>
          <View style={storageStatsStyle.legend}>
            {this.state.totalAudioBytes > 0 &&
              <View style={[storageStatsStyle.row, storageStatsStyle.legendItem]}>
                <View style={[storageStatsStyle.legendBox, storageStatsStyle.audioDistribution]} />
                <Text style={storageStatsStyle.legendText}>Audio</Text>
                <Text style={storageStatsStyle.legendSize}>{formatBytes(this.state.totalAudioBytes, 2)}</Text>
              </View>
            }
            {this.state.totalImageBytes > 0 &&
              <View style={[storageStatsStyle.row, storageStatsStyle.legendItem]}>
                <View style={[storageStatsStyle.legendBox, storageStatsStyle.imageDistribution]} />
                <Text style={storageStatsStyle.legendText}>Images</Text>
                <Text style={storageStatsStyle.legendSize}>{formatBytes(this.state.totalImageBytes, 2)}</Text>
              </View>
            }
            {this.state.totalVideoBytes > 0 &&
              <View style={[storageStatsStyle.row, storageStatsStyle.legendItem]}>
                <View style={[storageStatsStyle.legendBox, storageStatsStyle.videoDistribution]} />
                <Text style={storageStatsStyle.legendText}>Videos</Text>
                <Text style={storageStatsStyle.legendSize}>{formatBytes(this.state.totalVideoBytes, 2)}</Text>
              </View>
            }
            {this.state.totalOtherBytes > 0 &&
              <View style={[storageStatsStyle.row, storageStatsStyle.legendItem]}>
                <View style={[storageStatsStyle.legendBox, storageStatsStyle.otherDistribution]} />
                <Text style={storageStatsStyle.legendText}>Other</Text>
                <Text style={storageStatsStyle.legendSize}>{formatBytes(this.state.totalOtherBytes, 2)}</Text>
              </View>
            }
          </View>
        </View>}
      </View>
    )
  }
}

export default StorageStatsCard;
