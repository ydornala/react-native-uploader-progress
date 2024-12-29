/* eslint-disable react-native/no-inline-styles */
import { Image, StyleSheet, View } from 'react-native';
import { ProgressBar } from '@react-native-community/progress-bar-android';

type ICard = {
  path: string;
  progress: number;
};

const Card: React.FC<ICard> = (props) => {
  const { progress } = props;
  return (
    <View style={styles.card}>
      <ProgressBar
        style={{
          height: 25,
          width: '90%',
          position: 'absolute',
          top: 10,
          left: 10,
          zIndex: 999,
          // width: '100%',
        }}
        progress={progress}
        indeterminate={false}
        color="orange"
        styleAttr="Horizontal"
      />
      <Image source={{ uri: props.path }} style={{ width: 200, height: 200 }} />
    </View>
  );
};

export default Card;

const styles = StyleSheet.create({
  card: {
    width: 200,
    height: 200,
    // elevation: 5,
    borderRadius: 10,
  },
});
