import { DeviceEventEmitter, NativeModules } from 'react-native';
import UploaderProgress from './NativeUploaderProgress';

export type UploadEvent = 'progress' | 'error' | 'completed' | 'cancelled';

export type NotificationArgs = {
  enabled: boolean;
};

export type TStartUpload = {
  url: string;
  path: string;
  method?: 'PUT' | 'POST';
  // Optional, because raw is default
  type?: 'raw' | 'multipart';
  // This option is needed for multipart type
  field?: string;
  customUploadId?: string;
  // parameters are supported only in multipart type
  parameters?: { [key: string]: string };
  headers?: Object;
  notification?: NotificationArgs;
};

const NativeModule = NativeModules.UploaderProgress;
const eventPrefix = 'UploaderProgress-';

if (NativeModule) {
  NativeModule.addListener(eventPrefix + 'progress');
  NativeModule.addListener(eventPrefix + 'error');
  NativeModule.addListener(eventPrefix + 'completed');
  NativeModule.addListener(eventPrefix + 'cancelled');
}

export function multiply(a: number, b: number): number {
  return UploaderProgress.multiply(a, b);
}

export function add(a: number, b: number): number {
  return UploaderProgress.add(a, b);
}

export function getFileInfo(path: string): Promise<File> {
  return UploaderProgress.getFileInfo(path);
}

export function startUpload(options: TStartUpload): Promise<string> {
  return UploaderProgress.startUpload(options);
}

export const addListener = (
  eventType: UploadEvent,
  uploadId: string,
  listener: Function
) => {
  return DeviceEventEmitter.addListener(eventPrefix + eventType, (data) => {
    if (!uploadId || !data || !data.id || data.id === uploadId) {
      listener(data);
    }
  });
};
