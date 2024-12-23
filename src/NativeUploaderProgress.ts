import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  multiply(a: number, b: number): number;
  add(a: number, b: number): number;
  getFileInfo(path: string): Promise<File>;
  startUpload(options: {
    url: string;
    path: string;
    file?: string;
    type?: string;
    method?: string;
  }): Promise<string>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('UploaderProgress');
