package br.dexter.ifconfiguration.TheConfig;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

public class MyAdapterAssis extends RecyclerView.Adapter<MyAdapterAssis.ViewHolder>
{
    private Context context;
    private ArrayList<String> sala;
    private ArrayList<String> professor;

    private Dialog dialog;

    MyAdapterAssis(Context context, ArrayList<String> sala, ArrayList<String> professor)
    {
        this.context = context;
        this.sala = sala;
        this.professor = professor;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_config_card_assistente, parent, false);

        return new ViewHolder(view);
    }

    @Override @SuppressLint("SetTextI18n")
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i)
    {
        viewHolder.nameOfSala.setText(sala.get(i));
        viewHolder.nameOfProf.setText("Professor(a): " + professor.get(i).toUpperCase());

        Button(viewHolder, i);
    }

    private void Button(@NonNull final ViewHolder viewHolder, final int i)
    {
        viewHolder.ActionSolict.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                new MySQLDelete(MyAdapterAssis.this, sala.get(i), 0).execute();

                sala.remove(i);
                notifyItemRemoved(i);
            }
        });
    }

    private static class MySQLDelete extends AsyncTask<String, Void, String>
    {
        private WeakReference<MyAdapterAssis> activity;
        private String sala;
        private int assistente;

        MySQLDelete(MyAdapterAssis context, String sala, int assistente)
        {
            activity = new WeakReference<>(context);
            this.sala = sala;
            this.assistente = assistente;
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
                try{
                    Class.forName("com.mysql.jdbc.Driver");
                }catch (Exception e) {
                    Toast.makeText(activity.get().context, "Houve um erro, tente novamente", Toast.LENGTH_SHORT).show();
                }

                Connection con = DriverManager.getConnection(url, user, pass);
                Statement st = con.createStatement();

                st.execute("update sala set assistente='" + assistente + "'" + "where sala='" + sala + "'");
            }
            catch (Exception e)
            {
                e.printStackTrace();

                activity.get().dialog.dismiss();

                Toast.makeText(activity.get().context, "Houve um erro, tente novamente", Toast.LENGTH_SHORT).show();
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
        TextView nameOfSala, nameOfProf;
        Button ActionSolict;

        ViewHolder(final View itemView)
        {
            super(itemView);

            nameOfSala = itemView.findViewById(R.id.nameOfSala);
            nameOfProf = itemView.findViewById(R.id.nameOfProf);
            ActionSolict = itemView.findViewById(R.id.soc_ligar);
        }
    }
}