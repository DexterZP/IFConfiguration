package br.dexter.ifconfiguration.TheConfig;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rey.material.app.Dialog;
import com.rey.material.widget.Button;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;

import br.dexter.ifconfiguration.R;

import static br.dexter.ifconfiguration.MainActivity.pass;
import static br.dexter.ifconfiguration.MainActivity.url;
import static br.dexter.ifconfiguration.MainActivity.user;

public class MyAdapterServ extends RecyclerView.Adapter<MyAdapterServ.ViewHolder>
{
    private Context context;
    private ArrayList<String> sala;
    private ArrayList<String> professor;
    private ArrayList<Integer> arCondionado;
    private ArrayList<Integer> recursos;
    private ArrayList<String> recursosTXT;

    private Dialog dialog;

    MyAdapterServ(Context context, ArrayList<String> sala, ArrayList<String> professor, ArrayList<Integer> arCondicionado, ArrayList<Integer> recursos, ArrayList<String> recursosTXT)
    {
        this.context = context;
        this.sala = sala;
        this.professor = professor;
        this.arCondionado = arCondicionado;
        this.recursos = recursos;
        this.recursosTXT = recursosTXT;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_config_card_servidor, parent, false);

        return new ViewHolder(view);
    }

    @Override @SuppressLint("SetTextI18n")
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i)
    {
        viewHolder.nameOfSala.setText(sala.get(i));

        viewHolder.nameOfProf.setText("Professor(a): " + professor.get(i).toUpperCase());

        switch (arCondionado.get(i)) {
            case 1:
                viewHolder.nameOfStatus.setText("Ar-condicionado: solicitado");
                break;
            case 0:
                viewHolder.nameOfStatus.setText("Ar-condicionado: não solicitado");
                break;
        }

        if (recursos.get(i) == 0) {
            viewHolder.nameOfRecursos.setText("Recursos não solicitado");
        } else if (recursos.get(i) == 1) {
            viewHolder.nameOfRecursos.setText("Recursos solicitado(s): " + recursosTXT.get(i).toUpperCase());
        }

        Button(viewHolder, i);
    }

    private void Button(@NonNull final ViewHolder viewHolder, final int i)
    {
        viewHolder.ActionSolict.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                new MySQLDelete(MyAdapterServ.this, sala.get(i), 0, 0, "").execute();

                sala.remove(i);
                notifyItemRemoved(i);
            }
        });
    }

    private static class MySQLDelete extends AsyncTask<String, Void, String>
    {
        private WeakReference<MyAdapterServ> activity;
        private String sala;
        private int arCondicionado;
        private int recursos;
        private String recursosTXT;

        MySQLDelete(MyAdapterServ context, String sala, int arCondicionado, int recursos, String recursosTXT)
        {
            activity = new WeakReference<>(context);
            this.sala = sala;
            this.arCondicionado = arCondicionado;
            this.recursos = recursos;
            this.recursosTXT = recursosTXT;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            activity.get().dialog = new Dialog(activity.get().context);
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

                st.execute("update sala set arCondicionado='" + arCondicionado + "'" + "where sala='" + sala + "'");
                st.execute("update sala set recursos='" + recursos + "'" + "where sala='" + sala + "'");
                st.execute("update sala set recursosTXT='" + recursosTXT + "'" + "where sala='" + sala + "'");
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
        }
    }

    @Override
    public int getItemCount()
    {
        return sala.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView nameOfSala, nameOfStatus, nameOfProf, nameOfRecursos;
        Button ActionSolict;

        ViewHolder(final View itemView)
        {
            super(itemView);

            nameOfSala = itemView.findViewById(R.id.nameOfSala);
            nameOfStatus = itemView.findViewById(R.id.nameOfStatus);
            nameOfProf = itemView.findViewById(R.id.nameOfProf);
            nameOfRecursos = itemView.findViewById(R.id.nameOfRecursos);
            ActionSolict = itemView.findViewById(R.id.soc_ligar);
        }
    }
}