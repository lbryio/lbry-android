import { StyleSheet } from 'react-native';
import Colors from './colors';

const storageStatsStyle = StyleSheet.create({
  container: {
    flex: 1
  },
  row: {
    flexDirection: 'row'
  },
  card: {
    backgroundColor: Colors.White,
    marginTop: 16,
    marginLeft: 16,
    marginRight: 16,
    padding: 16
  },
  totalSize: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 36
  },
  annotation: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 14,
    marginTop: -4
  },
  statsText: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 14
  },
  distributionBar: {
    flexDirection: 'row',
    width: '100%',
    height: 8,
    marginTop: 16,
    marginBottom: 16
  },
  audioDistribution: {
    backgroundColor: Colors.StatsAudio
  },
  imageDistribution: {
    backgroundColor: Colors.StatsImage
  },
  videoDistribution: {
    backgroundColor: Colors.LbryGreen
  },
  otherDistribution: {
    backgroundColor: Colors.StatsOther
  },
  legendItem: {
    alignItems: 'center',
    marginBottom: 8,
    justifyContent: 'space-between'
  },
  legendBox: {
    width: 16,
    height: 16,
  },
  legendText: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 14,
    flex: 0.3
  },
  legendSize: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 14,
    flex: 0.6,
    textAlign: 'right'
  },
  statsToggle: {
    marginLeft: 8,
  },
  summary: {
    flex: 0.5,
    alignSelf: 'flex-start'
  },
  toggleStatsContainer: {
    flex: 0.5,
    alignItems: 'center',
    justifyContent: 'flex-end'
  }
});

export default storageStatsStyle;
