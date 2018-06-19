package app.laiki.service.fcm;

import android.support.annotation.Nullable;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static app.laiki.App.appService;
import static app.laiki.diagnostics.Logger.logFcm;

public class FcmRegistrationService extends FirebaseInstanceIdService {

    @Nullable
    public static String getFcmToken() {
        String token = FirebaseInstanceId.getInstance().getToken();
        logFcm("FCM token called: " + token);
        return token;
    }

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        appService().syncFcm();
    }

//    public static String getAccessToken() throws IOException {
//        InputStream stream = app().getResources().openRawResource(R.raw.firebase_server_key);
//        GoogleCredential googleCredential = GoogleCredential
//                .fromStream(stream)
//                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
//        googleCredential.refreshToken();
//        return googleCredential.getAccessToken();
//    }
}
