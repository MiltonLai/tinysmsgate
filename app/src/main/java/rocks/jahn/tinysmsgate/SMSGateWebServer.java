package rocks.jahn.tinysmsgate;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;


public class SMSGateWebServer extends NanoHTTPD {
    private static final String TAG = "SMSGateWebServer";
    private static final String SENT = "SMS_SENT";
    private static final String DELIVERED = "SMS_DELIVERED";
    private static final String KEY_PHONE = "PHONE";
    
    private SharedPreferences preferences;
    private SmsManager smsManager;
    private Context context;
    private BroadcastReceiver sentReceiver, deliveredReceiver;
    private Gson gson = new Gson();

    public SMSGateWebServer(int port) {
        super(port);
    }
    
    public void setPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }
    
    public void setSmsManager(SmsManager smsManager) {
        this.smsManager = smsManager;
    }
    
    public void setContext(Context context) {
        this.context = context;
    }
    
    @Override
    public void start() throws IOException {
        sentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "Sent OK: " + intent.getStringExtra(KEY_PHONE));
                    break;
                    default:
                        Log.e(TAG, "Sent Error:" + intent.getStringExtra(KEY_PHONE) + ", "  + getResultCode());
                }
            }
        };
        context.registerReceiver(sentReceiver, new IntentFilter(SENT));

        deliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "Delivered OK: " + intent.getStringExtra(KEY_PHONE));
                        break;
                    default:
                        Log.e(TAG, "Delivered Error:" + intent.getStringExtra(KEY_PHONE) + ", "  + getResultCode());
                }
            }
        };
        context.registerReceiver(deliveredReceiver, new IntentFilter(DELIVERED));
        super.start();
    }

    @Override
    public void stop() {
        context.unregisterReceiver(sentReceiver);
        context.unregisterReceiver(deliveredReceiver);
        super.stop();
    }

    public void sendSms(String to, String message) {
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(SENT).putExtra(KEY_PHONE, to), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED).putExtra(KEY_PHONE, to), 0);
        smsManager.sendTextMessage(to, null, message, sentPI, deliveredPI);
    }

    private String jsonEncode(String code, String message) {
        Result result = new Result(code, message);
        return gson.toJson(result);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
        Map<String, String> files = new HashMap<>();

        if(Method.POST.equals(method)) {
            try {
                session.parseBody(files);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR,"application/json",
                    jsonEncode("Internal Server Error", e.getMessage())
                );
            } catch (ResponseException e) {
                Log.e(TAG, e.getMessage(), e);
                return newFixedLengthResponse(e.getStatus(),"application/json",
                    jsonEncode("Internal Server Error", e.getMessage())
                );
            }
        }

        Map<String, String> data = session.getParms();
        data.put("NanoHttpd.QUERY_STRING", session.getQueryParameterString());
        boolean usePassword = preferences.getBoolean("chkUsePassword", false);
        String password = preferences.getString("txtPassword", "");
        
        if(uri.equals(preferences.getString("txtPage", "/send"))) {
            String desiredMethod = preferences.getString("lstReceiveMethod", "POST");
            if((desiredMethod.equals("POST") && Method.POST.equals(method)) ||
               (desiredMethod.equals("GET") && Method.GET.equals(method))) {
                if(usePassword) {
                    if(data.containsKey("password")) {
                        String sentPassword = data.get("password");
                        
                        if(sentPassword.equals(password)) {
                            String phone = data.get("phone");
                            String message = data.get("message");
                            sendSms(phone, message);

                            return newFixedLengthResponse(
                                    Response.Status.OK,
                                    "application/json",
                                    jsonEncode("SMSgate", "Sent!"));
                        } else {
                            return newFixedLengthResponse(
                                Response.Status.FORBIDDEN,
                                "application/json",
                                jsonEncode("Forbidden", "Bad password."));
                        }
                    } else {
                        return newFixedLengthResponse(
                            Response.Status.FORBIDDEN,
                            "application/json",
                            jsonEncode("Forbidden", "Bad password."));
                    }
                } else {
                    return newFixedLengthResponse(
                            Response.Status.OK,
                            "application/json",
                            jsonEncode("SMSgate", "Sent!"));
                }
            } else {
                return newFixedLengthResponse(
                    Response.Status.NOT_FOUND,
                    "application/json",
                    jsonEncode("404", "Aw, man. :("));
            }
        } else if(uri.equals("/")) {
            return newFixedLengthResponse(
                    Response.Status.OK,
                    "application/json",
                    jsonEncode("SMSGate", "Welcome to SMSGate!"));
        } else {
            return newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                "application/json",
                jsonEncode("404", "Aw, man. :("));
        }
    }

    public static class Result {
        private String code;
        private String message;

        public Result(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
