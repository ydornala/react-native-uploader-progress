import { Text, View, StyleSheet } from 'react-native';
import { add, multiply } from 'react-native-uploader-progress';

const result = multiply(3, 7);
const result2 = add(3, 7);

export default function App() {
  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
      <Text>Result Add: {result2}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
