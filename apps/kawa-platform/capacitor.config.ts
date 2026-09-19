import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.djesystems.kawa',
  appName: 'KAWA',
  webDir: 'dist',
  plugins: {

    FirebaseAuthentication: {
      providers: ["google.com"]
    },

    FirebaseMessaging: {
      presentationOptions: [
        "alert",
        "badge",
        "sound"
      ]
    }
  }
  
};

export default config;
