package br.dexter.ifconfiguration.TheConfig;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rey.material.app.Dialog;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;

import br.dexter.ifconfiguration.MainActivity;
import br.dexter.ifconfiguration.R;
import in.myinnos.customfontlibrary.TypefaceUtil;

import static br.dexter.ifconfiguration.MainActivity.ip;
import static br.dexter.ifconfiguration.MainActivity.pass;
import static br.dexter.ifconfiguration.MainActivity.url;
import static br.dexter.ifconfiguration.MainActivity.user;

public class Assistente extends AppCompatActivity
{
    private RecyclerView recyclerView;

    private ArrayList<String> salas = new ArrayList<>();
    private ArrayList<String> professor = new ArrayList<>();

    private boolean Alert;

    private Runnable runnable;

    private Dialog dialog;

    @SuppressLint("SetTextI18n") @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/minhafonte.ttf");
        setContentView(R.layout.activity_assistente);

        setTitle("IFMA Campus Caxias");

        TextView connectTXT = findViewById(R.id.ConnectTXT);

        connectTXT.setText("Bem-vindo(a)\nAssistente de aluno: " + GetCodigo());

        recyclerView = findViewById(R.id.recyclerview);

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog);
        dialog.setCancelable(false);
        dialog.show();

        final Handler handler = new Handler();

        runnable = new Runnable()
        {
            @Override
            public void run()
            {
                if(getWifiApIpAddress().contains(ip)) {
                    new MySQLGet(Assistente.this).execute();
                } else if(Alert) {
                    new AlertDialog.Builder(Assistente.this)
                            .setTitle("Conexão")
                            .setMessage("Você precisa está conectado a rede do Instituto")

                            .setPositiveButton("Fechar aplicativo", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })

                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                    Alert = false;
                }

                handler.postDelayed(runnable, 4000);
            }
        };

        handler.post(runnable);
    }

    private static class MySQLGet extends AsyncTask<String, Void, String>
    {
        private WeakReference<Assistente> activity;

        MySQLGet(Assistente context)
        {
            activity = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            activity.get().salas.clear();
            activity.get().professor.clear();
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
                    if(rs.getInt(7) == 1)
                    {
                        activity.get().salas.add(rs.getString(2));
                        activity.get().professor.add(rs.getString(4));
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();

                activity.get().dialog.dismiss();
            }
            return "task finished";
        }

        @Override
        protected void onPostExecute(String result)
        {
            activity.get().dialog.dismiss();

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity.get());
            activity.get().recyclerView.setLayoutManager(mLayoutManager);
            MyAdapterAssis adapter = new MyAdapterAssis(activity.get(), activity.get().salas, activity.get().professor);
            activity.get().recyclerView.setAdapter(adapter);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_sair)
        {
            deleteFile(MainActivity.nameCod);
            deleteFile(MainActivity.profissionCod);

            Intent intent = new Intent(Assistente.this, MainActivity.class);
            startActivity(intent);
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public String GetCodigo()
    {
        StringBuilder datax = new StringBuilder();
        try
        {
            FileInputStream fIn = openFileInput(MainActivity.nameCod);
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