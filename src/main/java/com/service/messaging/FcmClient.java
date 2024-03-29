package com.service.messaging;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
@Component
public class FcmClient {
    public FcmClient(FcmSettings settings) {
        Path p = Paths.get(settings.getServiceAccountFile());
        try (InputStream serviceAccount = Files.newInputStream(p)) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();

            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendNotification(Map<String, String> data)
            throws InterruptedException, ExecutionException {

        AndroidNotification androidNofi = AndroidNotification.builder()
                .setSound("order.wav")
                .setTitle(data.get("title"))
                .setBody(data.get("text"))
                .setPriority(AndroidNotification.Priority.HIGH)
                .setImage(data.get("image"))
                .build();

        // this setting is for android
        AndroidConfig androidConfig = AndroidConfig.builder()
                .setTtl(Duration.ofMinutes(2).toMillis())
                .setPriority(AndroidConfig.Priority.HIGH)
                .setDirectBootOk(true)
                .setNotification(androidNofi).build();


        Message message = Message.builder().putAllData(data)
                .setToken(data.get("token"))
                .setAndroidConfig(androidConfig)
                .build();

        String response = FirebaseMessaging.getInstance().sendAsync(message).get();
    }


    public void multiCaseMessage(Map<String, String> data, List<String> tokens) throws ExecutionException, InterruptedException {
        // this setting is for android

        long[] val = new long[1];
        val[0]=10000l;
        AndroidNotification androidNofi = AndroidNotification.builder()
                .setTitle(data.get("title"))
                .setBody(data.get("body"))
                .setVibrateTimingsInMillis(val)
                .setPriority(AndroidNotification.Priority.MAX)
                .setImage(data.get("image"))
                .build();

        AndroidConfig androidConfig = AndroidConfig.builder()
                .setTtl(Duration.ofMinutes(2).toMillis())
                .setPriority(AndroidConfig.Priority.HIGH)
                .setDirectBootOk(true)
                .setNotification(androidNofi).build();

        Aps aps = Aps.builder()
//                .setSound("https://firebasestorage.googleapis.com/v0/b/baba-basket-645b9.appspot.com/o/sound.wav?alt=media&token=3ab56c5c-d517-4bc4-90fc-61d23e3498e5")
                .setSound("assets/sound.wav")

                .build();

        ApnsConfig apnsConfig = ApnsConfig.builder()
                .setAps(aps).build();
        MulticastMessage message = MulticastMessage.builder()
                .setAndroidConfig(androidConfig)
                .setApnsConfig(apnsConfig)
                .addAllTokens(tokens)
                .build();



        // this setting is for android

        String response = FirebaseMessaging.getInstance().sendMulticastAsync(message).toString();
    }



}
