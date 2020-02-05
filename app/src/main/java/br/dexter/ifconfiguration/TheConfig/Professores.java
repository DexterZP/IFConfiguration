package br.dexter.ifconfiguration.TheConfig;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
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
import java.util.List;

import br.dexter.ifconfiguration.MainActivity;
import br.dexter.ifconfiguration.R;
import in.myinnos.customfontlibrary.TypefaceUtil;

import static br.dexter.ifconfiguration.MainActivity.ip;
import static br.dexter.ifconfiguration.MainActivity.pass;
import static br.dexter.ifconfiguration.MainActivity.user;
import static br.dexter.ifconfiguration.MainActivity.url;

public class Professores extends AppCompatActivity
{
    private RecyclerView recyclerView;

    private Dialog dialog;

    @SuppressLint("SetTextI18n") @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/minhafonte.ttf");
        setContentView(R.layout.activity_professor);

        setTitle("IFMA Campus Caxias");

        recyclerView = findViewById(R.id.recyclerview);

        TextView connectTXT = findViewById(R.id.ConnectTXT);

        if(getWifiApIpAddress().contains(ip)) {
            new ConnectMySql(this).execute();
        } else {
            new AlertDialog.Builder(this)
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

        connectTXT.setText("Bem-vindo(a)\nProfessor(a): " + GetCodigo());
    }

    private static class ConnectMySql extends AsyncTask<String, Void, String>
    {
        private WeakReference<Professores> activity;

        private List<String> salas = new ArrayList<>();
        private List<String> options1 = new ArrayList<>();
        private List<String> options2 = new ArrayList<>();
        private List<String> missing1 = new ArrayList<>();
        private List<String> missing2 = new ArrayList<>();
        private List<String> rec1 = new ArrayList<>();
        private List<String> rec2 = new ArrayList<>();

        ConnectMySql(Professores context)
        {
            activity = new WeakReference<>(context);
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

                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("select * from sala");

                while(rs.next())
                {
                    salas.add(rs.getString(2));
                    options1.add(rs.getString(8));
                    options2.add(rs.getString(10));

                    if(rs.getString(9) != null) {
                        missing1.add(rs.getString(2));
                        rec1.add(rs.getString(9));
                    }

                    if(rs.getString(11) != null) {
                        missing2.add(rs.getString(2));
                        rec2.add(rs.getString(11));
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();

                activity.get().dialog.dismiss();

                Toast.makeText(activity.get(), "Houve um erro, tente novamente", Toast.LENGTH_SHORT).show();
            }
            return "task finished";
        }

        @Override
        protected void onPostExecute(String result)
        {
            activity.get().dialog.dismiss();

            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(activity.get(), 2);
            activity.get().recyclerView.setLayoutManager(mLayoutManager);
            MyAdapterProf adapter = new MyAdapterProf(activity.get().recyclerView, activity.get(), salas, options1, options2, missing1, missing2, rec1, rec2);
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

            Intent intent = new Intent(Professores.this, MainActivity.class);
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