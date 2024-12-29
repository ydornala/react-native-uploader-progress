/* eslint-disable prettier/prettier */
/* eslint-disable react-native/no-inline-styles */
import { Text, View, StyleSheet, Pressable, ScrollView } from 'react-native';
import {
  getFileInfo,
  startUpload,
  addListener,
} from 'react-native-uploader-progress';
import * as ImagePicker from 'react-native-image-picker';
import uuid from 'react-native-uuid';
import { useState } from 'react';
import Card from './Card';

export default function App() {
  const [media, setMedia] = useState<Array<any> | undefined>([]);
  const [progressMap, setProgressMap] = useState([]);

  const handleUpload = () => {
    media?.forEach((asset: any) => {
      getFileInfo(asset.originalPath || '').then(() => {
        startUpload({
          url: 'https://335b-103-211-43-38.ngrok-free.app/upload',
          path: asset.originalPath || '',
          method: 'POST',
          type: 'multipart',
          field: 'file',
          customUploadId: asset.customUploadId,
        }).then((uploadId) => {
          addListener('progress', uploadId, (progress: number) => {
            setProgressMap((prev) => ({
              ...prev,
              [uploadId]: progress,
            }));
          });

          addListener('completed', uploadId, () => {
            // Remove uploaded asset from media
            setMedia((prevMedia) =>
              prevMedia?.filter((m) => m.customUploadId !== uploadId)
            );

            // Cleanup progress bar
            // setProgressMap((prev) => {
            //   const updatedProgress = { ...prev };
            //   delete updatedProgress[uploadId];
            //   return updatedProgress;
            // });
          });
        });
      });
    });
  };

  const handleLoadImages = async () => {
    const imageLibrary = await ImagePicker.launchImageLibrary({
      mediaType: 'photo',
      selectionLimit: 30,
    });

    setMedia(
      imageLibrary.assets?.map((a) => ({ ...a, customUploadId: uuid.v4() }))
    );
  };

  return (
    <View style={styles.container}>
      <ScrollView
        horizontal
        style={{
          backgroundColor: '#eee',
          marginVertical: 25,
        }}
      >
        <View style={{ flexDirection: 'row', gap: 25 }}>
          {media?.map((asset: any, i: number) => {
            const pm = progressMap[asset.customUploadId] || { progress: 0 };
            return <Card path={asset.uri} progress={(pm.progress / 100)} key={i} />;
          })}
        </View>
      </ScrollView>
      <View style={{ flexDirection: 'row', gap: 10 }}>
        <Pressable onPress={handleLoadImages} style={styles.loadImagesButton}>
          <Text>Load Images</Text>
        </Pressable>
        <Pressable onPress={handleUpload} style={styles.loadImagesButton}>
          <Text>Upload Images</Text>
        </Pressable>
        <Pressable onPress={() => setMedia([])} style={styles.loadImagesButton}>
          <Text>Clear Images</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    // flex: 1,
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
