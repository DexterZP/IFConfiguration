package br.dexter.ifconfiguration;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.rey.material.app.Dialog;
import com.rey.material.widget.Button;
import com.rey.material.widget.Spinner;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;

import br.dexter.ifconfiguration.Notification.BaseNotification;
import br.dexter.ifconfiguration.TheConfig.Config;
import in.myinnos.customfontlibrary.TypefaceUtil;

public class MainActivity extends AppCompatActivity
{
    private Button entrar;
    private TextInputLayout nameLayout;
    private TextInputEditText name;
    private Spinner spinner;

    private String cName = "";

    private Dialog dialog;

    public static String nameCod = "codNameIF.txt";
    public static String profissionCod = "profissionIF.txt";

    public static final String ip = "192.168";
    public static final String url = "jdbc:mysql://remotemysql.com:3306/hz1yYUcrvf";
    public static final String user = "hz1yYUcrvf";
    public static final String pass = "ePgzafZdrQ";

    //public static final String ip = "10.9";
    //public static final String url = "jdbc:mysql://10.9.12.10:3306/ifsystem";
    //public static final String user = "root";
    //public static final String pass = "nti1fm@cx09";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/minhafonte.ttf");
        setContentView(R.layout.activity_main);

        setTitle("IFMA Campus Caxias");

        entrar = findViewById(R.id.Entrar);

        nameLayout = findViewById(R.id.textInputLayout);
        name = findViewById(R.id.Nome);

        spinner = findViewById(R.id.spinner_Select);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getBaseContext(), R.array.spinnerSelect, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        BaseNotification.startBaseAlarmManager(this);
        BaseNotification.enableBootReceiver(this);

        Button();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if(fileExists(this, nameCod) && fileExists(this, profissionCod)) {
            Intent intent = new Intent(this, Config.class);
            startActivity(intent);
            finish();
        }
    }

    public void Button()
    {
        entrar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(name.getText() != null)
                {
                    if(!name.getText().toString().isEmpty())
                    {
                        if(getWifiApIpAddress().contains(ip)) {
                            new ConnectMySql(MainActivity.this, name.getText().toString()).execute();
                        } else {
                            new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Conexão")
                            .setMessage("Você precisa está conectado a rede do Instituto")

                            .setPositiveButton("Fechar aplicativo", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })

                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                        }
                    }
                    else {
                        nameLayout.setError("Não deixe em branco");
                    }
                }
            }
        });
    }

    private static class ConnectMySql extends AsyncTask<String, Void, String>
    {
        private WeakReference<MainActivity> activity;
        private String siapeTemp;

        ConnectMySql(MainActivity context, String siape)
        {
            activity = new WeakReference<>(context);
            siapeTemp = siape;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            activity.get().dialog = new Dialog(activity.get());
            activity.get().dialog.setContentView(R.layout.dialog);
            activity.get().dialog.setCancelable(false);
            activity.get().dialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(url, user, pass);

                StringBuilder result = new StringBuilder();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("select * from usuario");

                while (rs.next()) {
                    if(siapeTemp.toUpperCase().equals(rs.getString(2)))
                    {
                        result.append(rs.getString(3));
                    }
                }
                activity.get().cName = result.toString();
            }
            catch (Exception e)
            {
                e.printStackTrace();

                activity.get().dialog.dismiss();

                Toast.makeText(activity.get(), "Houve um erro, tente novamente", Toast.LENGTH_SHORT).show();
            }
            return activity.get().cName;
        }

        @Override
        protected void onPostExecute(String result)
        {
            activity.get().dialog.dismiss();

            if(activity.get().cName.isEmpty()) {
                Toast.makeText(activity.get(), "SIAPE não encontrado", Toast.LENGTH_LONG).show();
            }
            else
            {
                activity.get().SavedCodigo();

                Intent intent = new Intent(activity.get(), Config.class);
                activity.get().startActivity(intent);
                activity.get().finish();
            }
        }
    }

    public String getWifiApIpAddress()
    {
        try
        {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
            {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().contains("wlan"))
                {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                    {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && (inetAddress.getAddress().length == 4)) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        }
        catch (SocketException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean fileExists(Context context, String filename)
    {
        File file = context.getFileStreamPath(filename);
        return file.exists();
    }

    public void SavedCodigo()
    {
        FileOutputStream namber, profission;
        try
        {
            namber = openFileOutput(nameCod, Context.MODE_PRIVATE);
            namber.write(cName.getBytes());
            namber.close();

            profission = openFileOutput(profissionCod, Context.MODE_PRIVATE);
            profission.write(spinner.getSelectedItem().toString().getBytes());
            profission.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}