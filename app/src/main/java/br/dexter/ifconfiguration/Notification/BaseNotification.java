package br.dexter.ifconfiguration.Notification;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;

import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import br.dexter.ifconfiguration.MainActivity;
import br.dexter.ifconfiguration.R;

import static br.dexter.ifconfiguration.MainActivity.pass;
import static br.dexter.ifconfiguration.MainActivity.url;
import static br.dexter.ifconfiguration.MainActivity.user;

public class BaseNotification extends BroadcastReceiver
{
    public static final String BaseAction = "br.dexter.ifconfiguration";

    private String sala = "";

    private boolean isRunningAR = false;
    private boolean isRunningREC = false;
    private boolean isRunningASS = false;

    private Handler handler = new Handler();
    private Runnable runnable;

    private int DelayPostMinute = 20000; // ms

    public BaseNotification() { }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
        {
            startBaseAlarmManager(context);
        }
        else if(BaseAction.equals(intent.getAction()))
        {
            StartYourService(context);
        }
    }

    private void StartYourService(final Context context)
    {
        runnable = new Runnable()
        {
            @Override
            public void run()
            {
                new MySQLGet(BaseNotification.this).execute();

                if(GetCodigo(context).equals("Técnico Administrativo"))
                {
                    if(isRunningREC || isRunningAR)
                    {
                        Notification(context, sala);
                    }
                }
                else if(GetCodigo(context).equals("Assistente de Aluno") && isRunningASS)
                {
                    Notification(context, sala);
                }
                handler.postDelayed(runnable, DelayPostMinute);
            }
        };

        handler.post(runnable);
    }

    public static void startBaseAlarmManager(final Context context)
    {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, BaseNotification.class);
        intent.setAction(BaseAction);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if(alarmMgr != null) {
            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000, 5000, alarmIntent);
        }
    }

    public static void enableBootReceiver(Context context)
    {
        ComponentName receiver = new ComponentName(context, BaseNotification.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void Notification(Context context, String sala)
    {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(BaseAction, "Notification", NotificationManager.IMPORTANCE_DEFAULT);

            if (notificationManager != null) {
                notificationChannel.setDescription("EDMT Channel");
                notificationChannel.enableLights(true);
                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                notificationChannel.enableLights(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, BaseAction);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_notification)
                .setFullScreenIntent(pendingIntent, true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentTitle(sala)
                .setContentText("Necessitamos de você nesta sala")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (notificationManager != null)
            notificationManager.notify(1, notificationBuilder.build());
    }

    private static class MySQLGet extends AsyncTask<String, Void, String>
    {
        private WeakReference<BaseNotification> activity;

        MySQLGet(BaseNotification context)
        {
            activity = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(url, user, pass);

                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("select * from sala");

                while (rs.next())
                {
                    if(rs.getInt(3) == 1) {
                        activity.get().sala = rs.getString(2);
                        activity.get().isRunningAR = true;
                    }

                    if(rs.getInt(5) == 1) {
                        activity.get().sala = rs.getString(2);
                        activity.get().isRunningREC = true;
                    }

                    if(rs.getInt(7) == 1) {
                        activity.get().sala = rs.getString(2);
                        activity.get().isRunningASS = true;
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return "task finished";
        }

        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    activity.get().isRunningAR = false;
                    activity.get().isRunningREC = false;
                    activity.get().isRunningASS = false;
                }
            }, activity.get().DelayPostMinute);
        }
    }

    private String GetCodigo(Context context)
    {
        StringBuilder datax = new StringBuilder();
        try
        {
            FileInputStream fIn = context.openFileInput(MainActivity.profissionCod);
            InputStreamReader isr = new InputStreamReader(fIn);
            BufferedReader buffreader = new BufferedReader(isr);

            String readString = buffreader.readLine();
            while (readString != null) {
                datax.append(readString);
                readString = buffreader.readLine();
            }

            isr.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return datax.toString();
    }
}