import { Text, View, StyleSheet, Pressable } from 'react-native';
import {
  add,
  getFileInfo,
  multiply,
  startUpload,
  addListener,
} from 'react-native-uploader-progress';
import * as ImagePicker from 'react-native-image-picker';

const result = multiply(3, 7);
const result2 = add(3, 17);

export default function App() {
  const handleLoadImages = async () => {
    const media = await ImagePicker.launchImageLibrary({
      mediaType: 'photo',
      selectionLimit: 30,
    });

    media?.assets?.forEach((asset) => {
      getFileInfo(asset.originalPath || '').then(() => {
        startUpload({
          url: 'https://335b-103-211-43-38.ngrok-free.app/upload',
          path: asset.originalPath || '',
          method: 'POST',
          type: 'multipart',
          field: 'file',
          notification: {
            enabled: false,
            autoClear: true,
          },
        }).then((uploadId) => {
          console.log('res upload: ', uploadId);
          addListener('progress', uploadId, (progress: number) => {
            console.log('progress: ', uploadId, progress);
          });

          addListener('completed', uploadId, (res: any) => {
            console.log('completed: ', uploadId, res);
          });
        });
      });
    });
  };

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
      <Text>Result Add: {result2}</Text>
      <Pressable onPress={handleLoadImages} style={styles.loadImagesButton}>
        <Text>Load Images</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  loadImagesButton: {
    paddingVertical: 10,
    paddingHorizontal: 20,
    elevation: 4,
    backgroundColor: '#eee',
  },
});
