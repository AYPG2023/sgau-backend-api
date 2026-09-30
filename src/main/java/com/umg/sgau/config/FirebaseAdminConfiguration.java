package com.umg.sgau.notificacion.config;
import com.google.auth.oauth2.GoogleCredentials; import com.google.firebase.*; import com.google.firebase.messaging.FirebaseMessaging; import java.io.IOException; import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; import org.springframework.context.annotation.*;
@Configuration @ConditionalOnProperty(name="firebase.project-id") public class FirebaseAdminConfiguration {
 @Bean FirebaseApp sgauFirebaseApp(org.springframework.core.env.Environment env)throws IOException{var options=FirebaseOptions.builder().setCredentials(GoogleCredentials.getApplicationDefault()).setProjectId(env.getProperty("firebase.project-id")).build();return FirebaseApp.getApps().stream().filter(a->a.getName().equals("sgau")).findFirst().orElseGet(()->FirebaseApp.initializeApp(options,"sgau"));}
 @Bean FirebaseMessaging firebaseMessaging(FirebaseApp sgauFirebaseApp){return FirebaseMessaging.getInstance(sgauFirebaseApp);}
 @Bean com.umg.sgau.notificacion.service.FcmSender fcmSender(FirebaseMessaging messaging){return(token,data)->messaging.send(com.google.firebase.messaging.Message.builder().setToken(token).putAllData(data).build());}
}
