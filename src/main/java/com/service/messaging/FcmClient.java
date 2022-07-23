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

    public void sendJoke(Map<String, String> data)
            throws InterruptedException, ExecutionException {

        // this setting is for android
        AndroidConfig androidConfig = AndroidConfig.builder()
                .setTtl(Duration.ofMinutes(2).toMillis()).setCollapseKey("chuck")
                .setPriority(AndroidConfig.Priority.HIGH)

                .setDirectBootOk(true)
                .setNotification(AndroidNotification.builder().setTag("chuck").build()).build();

        // this setting is for IOS
        ApnsConfig apnsConfig = ApnsConfig.builder()
                .setAps(Aps.builder().setCategory("chuck").setThreadId("chuck").build()).build();

        Message message = Message.builder().putAllData(data).setTopic("chuck")
                .setApnsConfig(apnsConfig).setAndroidConfig(androidConfig)
                .setNotification(Notification.builder().setTitle("Greeting").setBody("Good morning from Baba Basket").setImage("https://www.iwmbuzz.com/wp-content/uploads/2022/04/rooh-baba-kartik-aaryan-dons-baba-look-like-akshay-kumar-fans-super-excited.jpg").build())
                .build();


        String response = FirebaseMessaging.getInstance().sendAsync(message).get();
        System.out.println("Sent message: " + response);
    }


}
