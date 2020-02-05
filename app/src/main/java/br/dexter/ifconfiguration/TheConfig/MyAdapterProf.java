package br.dexter.ifconfiguration.TheConfig;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.rey.material.app.Dialog;
import com.rey.material.widget.Button;
import com.rey.material.widget.Spinner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import br.dexter.ifconfiguration.MainActivity;
import br.dexter.ifconfiguration.R;

import static br.dexter.ifconfiguration.MainActivity.pass;
import static br.dexter.ifconfiguration.MainActivity.url;
import static br.dexter.ifconfiguration.MainActivity.user;

public class MyAdapterProf extends RecyclerView.Adapter<MyAdapterProf.ViewHolder>
{
    private RecyclerView recyclerView;
    private Context context;
    private List<String> sala;
    private List<String> options1;
    private List<String> options2;
    private List<String> missing1;
    private List<String> missing2;
    private List<String> rec1;
    private List<String> rec2;

    private Dialog dialogAlert;

    private int arCondicionado = 0;
    private int recursos = 0;
    private int assistente = 0;

    MyAdapterProf(RecyclerView recyclerView, Context context, List<String> sala, List<String> options1, List<String> options2, List<String> missing1, List<String> missing2, List<String> rec1, List<String> rec2)
    {
        this.recyclerView = recyclerView;
        this.context = context;
        this.sala = sala;
        this.options1 = options1;
        this.options2 = options2;
        this.missing1 = missing1;
        this.missing2 = missing2;
        this.rec1 = rec1;
        this.rec2 = rec2;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_config_card, parent, false);

        return new ViewHolder(view);
    }

    @Override @SuppressLint("SetTextI18n")
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i)
    {
        viewHolder.nameOfSala.setText(sala.get(i));

        if(missing1.get(i).equals(sala.get(i)) && !rec1.get(i).isEmpty()) {
            viewHolder.nameOfOptions1.setText(options1.get(i) + "\n" + rec1.get(i));
            viewHolder.nameOfOptions1.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_alert, 0);
        }
        else {
            viewHolder.nameOfOptions1.setText(options1.get(i));
            viewHolder.nameOfOptions1.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        if(missing2.get(i).equals(sala.get(i)) && !rec2.get(i).isEmpty()) {
            viewHolder.nameOfOptions2.setText(options2.get(i) + "\n" + rec2.get(i));
            viewHolder.nameOfOptions2.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_alert, 0);
        }
        else {
            viewHolder.nameOfOptions2.setText(options2.get(i));
            viewHolder.nameOfOptions2.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        if(viewHolder.nameOfOptions2.getText().toString().equals("")) {
            viewHolder.CardOptions2.setVisibility(View.GONE);
        }
        else {
            viewHolder.CardOptions2.setVisibility(View.VISIBLE);
        }

        if(viewHolder.nameOfOptions1.getText().toString().equals("")) {
            viewHolder.CardOptions1.setVisibility(View.GONE);
        }
        else {
            viewHolder.CardOptions1.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount()
    {
        return sala.size();
    }

    @SuppressLint("SetTextI18n")
    class ViewHolder extends RecyclerView.ViewHolder
    {
        CardView cardView, CardOptions1, CardOptions2;
        TextView nameOfSala, nameOfSalaUP, nameOfProfessorUP, nameOfOptions1, nameOfOptions2;
        Button ActionAr, ActionAssistente, ActionRecursos;

        ViewHolder(final View itemView)
        {
            super(itemView);

            cardView = itemView.findViewById(R.id.CardClick);
            nameOfSala = itemView.findViewById(R.id.nameOfSala);
            CardOptions1 = itemView.findViewById(R.id.CardOptions1);
            CardOptions2 = itemView.findViewById(R.id.CardOptions2);
            nameOfOptions1 = itemView.findViewById(R.id.nameOfOptions1);
            nameOfOptions2 = itemView.findViewById(R.id.nameOfOptions2);

            cardView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(final View view)
                {
                    new ConnectMySqlGet(MyAdapterProf.this, sala.get(recyclerView.getChildLayoutPosition(itemView)), GetCodigo()).execute();

                    dialogAlert = new Dialog(context);
                    dialogAlert.setContentView(R.layout.dialog);
                    dialogAlert.setCancelable(false);
                    dialogAlert.show();

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final int position = recyclerView.getChildLayoutPosition(itemView);

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            ViewGroup dialog = (ViewGroup) View.inflate(context, R.layout.activity_config_card_click, null);
                            dialog.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left));

                            nameOfSalaUP = dialog.findViewById(R.id.nameOfSala);
                            nameOfProfessorUP = dialog.findViewById(R.id.nameOfProf);
                            ActionAr = dialog.findViewById(R.id.soc_ar);
                            ActionAssistente = dialog.findViewById(R.id.soc_assistente);
                            ActionRecursos = dialog.findViewById(R.id.soc_recursos);

                            nameOfSalaUP.setText(sala.get(position));
                            nameOfProfessorUP.setText("Prof°: " + GetCodigo());

                            builder.setNeutralButton("Cancelar", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    new MySQLDelete(MyAdapterProf.this, sala.get(position)).execute();
                                }
                            });

                            builder.setNegativeButton("Confirmar", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    dialogInterface.dismiss();
                                }
                            });

                            switch (arCondicionado) {
                                case 1:
                                    ActionAr.setText("Ar-condicionado");
                                    ActionAr.setEnabled(false);
                                    ActionAr.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.round4));
                                    break;

                                case 0:
                                    ActionAr.setText("Ar-condicionado");
                                    ActionAr.setEnabled(true);
                                    ActionAr.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.round));
                                    break;
                            }

                            switch (recursos) {
                                case 0:
                                    ActionRecursos.setText("Recursos");
                                    ActionRecursos.setEnabled(true);
                                    ActionRecursos.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.round2));
                                    break;
                                case 1:
                                    ActionRecursos.setText("Recurso solicitado");
                                    ActionRecursos.setEnabled(false);
                                    ActionRecursos.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.round4));
                                    break;
                            }

                            switch (assistente) {
                                case 0:
                                    ActionAssistente.setText("Assistente de aluno");
                                    ActionAssistente.setEnabled(true);
                                    ActionAssistente.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.round3));
                                    break;
                                case 1:
                                    ActionAssistente.setText("Assistente de aluno");
                                    ActionAssistente.setEnabled(false);
                                    ActionAssistente.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.round4));
                                    break;
                            }

                            ActionAr.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    new MySQLUpdateArCondicionado(MyAdapterProf.this, sala.get(position), 1, GetCodigo()).execute();
                                    notifyItemChanged(position);

                                    ActionAr.setText("Ar-condicionado");
                                    ActionAr.setEnabled(false);
                                    ActionAr.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.round4));
                                }
                            });

                            ActionAssistente.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    new MySQLUpdateAssistente(MyAdapterProf.this, sala.get(position), 1, GetCodigo()).execute();
                                    notifyItemChanged(position);

                                    ActionAssistente.setText("Assistente de aluno");
                                    ActionAssistente.setEnabled(false);
                                    ActionAssistente.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.round4));
                                }
                            });

                            ActionRecursos.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    ViewGroup dialog = (ViewGroup) View.inflate(context, R.layout.activity_choose_recurso, null);
                                    dialog.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left));

                                    Button chooseRecurso = dialog.findViewById(R.id.chooseRecurso);
                                    Button inputRecurso = dialog.findViewById(R.id.inputRecurso);

                                    chooseRecurso.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                            final ViewGroup dialog = (ViewGroup) View.inflate(context, R.layout.activity_choose_recurso2, null);
                                            dialog.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left));

                                            final Spinner spinner = dialog.findViewById(R.id.spinner_Select);

                                            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.recursos, android.R.layout.simple_spinner_item);
                                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            spinner.setAdapter(adapter);

                                            builder.setNeutralButton("Enviar", new DialogInterface.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i)
                                                {
                                                    new MySQLUpdateRecursos(MyAdapterProf.this, sala.get(position), 1, GetCodigo(), spinner.getSelectedItem().toString()).execute();

                                                    ActionRecursos.setText("Recursos");
                                                    ActionRecursos.setEnabled(false);
                                                    ActionRecursos.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.round4));

                                                    dialogInterface.dismiss();
                                                }
                                            });

                                            builder.setNegativeButton("Sair", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                }
                                            });

                                            builder.setView(dialog);
                                            AlertDialog alert = builder.create();
                                            alert.show();

                                            android.widget.Button pbutton = alert.getButton(DialogInterface.BUTTON_NEUTRAL);
                                            android.widget.Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);

                                            pbutton.setBackgroundColor(context.getResources().getColor(R.color.green));
                                            nbutton.setBackgroundColor(context.getResources().getColor(R.color.red));

                                            pbutton.setTextColor(Color.WHITE);
                                            nbutton.setTextColor(Color.WHITE);
                                        }
                                    });

                                    inputRecurso.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                            ViewGroup dialog = (ViewGroup) View.inflate(context, R.layout.activity_choose_recurso1, null);
                                            dialog.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left));

                                            final TextInputEditText nameRecurso = dialog.findViewById(R.id.nameRecurso);
                                            final TextInputLayout nameLayout = dialog.findViewById(R.id.textInputLayout);

                                            builder.setNeutralButton("Enviar", new DialogInterface.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i)
                                                {
                                                    if(nameRecurso.getText() != null && nameRecurso.getText().toString().isEmpty()) {
                                                        nameLayout.setError("Não deixe o campo vazio");
                                                        return;
                                                    }

                                                    new MySQLUpdateRecursos(MyAdapterProf.this, sala.get(position), 1, GetCodigo(), nameRecurso.getText().toString()).execute();

                                                    ActionRecursos.setText("Recursos");
                                                    ActionRecursos.setEnabled(false);
                                                    ActionRecursos.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.round4));

                                                    dialogInterface.dismiss();
                                                }
                                            });

                                            builder.setNegativeButton("Sair", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                }
                                            });

                                            builder.setView(dialog);
                                            AlertDialog alert = builder.create();
                                            alert.show();

                                            android.widget.Button pbutton = alert.getButton(DialogInterface.BUTTON_NEUTRAL);
                                            android.widget.Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);

                                            pbutton.setBackgroundColor(context.getResources().getColor(R.color.green));
                                            nbutton.setBackgroundColor(context.getResources().getColor(R.color.red));

                                            pbutton.setTextColor(Color.WHITE);
                                            nbutton.setTextColor(Color.WHITE);
                                        }
                                    });

                                    builder.setNegativeButton("Sair", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    });

                                    builder.setView(dialog);
                                    AlertDialog alert = builder.create();
                                    alert.show();

                                    android.widget.Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);

                                    nbutton.setBackgroundColor(context.getResources().getColor(R.color.red));

                                    nbutton.setTextColor(Color.WHITE);
                                }
                            });

                            builder.setView(dialog);

                            AlertDialog alert = builder.create();
                            alert.show();

                            android.widget.Button pbutton = alert.getButton(DialogInterface.BUTTON_NEUTRAL);
                            android.widget.Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);

                            pbutton.setBackgroundColor(context.getResources().getColor(R.color.red));
                            nbutton.setBackgroundColor(context.getResources().getColor(R.color.green));

                            pbutton.setTextColor(Color.WHITE);
                            nbutton.setTextColor(Color.WHITE);

                            dialogAlert.dismiss();
                        }
                    }, 1400);
                }
            });
        }
    }

    private static class ConnectMySqlGet extends AsyncTask<String, Void, String>
    {
        private WeakReference<MyAdapterProf> activity;
        private String sala;
        private String professor;

        ConnectMySqlGet(MyAdapterProf context, String sala, String professor)
        {
            activity = new WeakReference<>(context);
            this.sala = sala;
            this.professor = professor;
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
                    if(sala.equals(rs.getString(2)))
                    {
                        activity.get().arCondicionado = rs.getInt(3);
                        activity.get().recursos = rs.getInt(5);
                        activity.get().assistente = rs.getInt(7);
                    }
                }

                st.execute("update sala set professor='" + professor + "'" + "where sala='" + sala + "'");
            }
            catch (Exception e)
            {
                e.printStackTrace();

                activity.get().dialogAlert.dismiss();
            }
            return "task finished";
        }
    }

    private static class MySQLDelete extends AsyncTask<String, Void, String>
    {
        private WeakReference<MyAdapterProf> activity;
        private String sala;

        MySQLDelete(MyAdapterProf context, String sala)
        {
            activity = new WeakReference<>(context);
            this.sala = sala;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            activity.get().dialogAlert = new Dialog(activity.get().context);
            activity.get().dialogAlert.setContentView(R.layout.dialog);
            activity.get().dialogAlert.setCancelable(false);
            activity.get().dialogAlert.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(url, user, pass);

                Statement st = con.createStatement();

                st.execute("update sala set arCondicionado='0' where sala='" + sala + "'");
                st.execute("update sala set professor='' where sala='" + sala + "'");
                st.execute("update sala set recursos='0' where sala='" + sala + "'");
                st.execute("update sala set recursosTXT='' where sala='" + sala + "'");
                st.execute("update sala set assistente='0' where sala='" + sala + "'");
            }
            catch (Exception e)
            {
                e.printStackTrace();

                activity.get().dialogAlert.dismiss();
            }
            return "task finished";
        }

        @Override
        protected void onPostExecute(String result)
        {
            activity.get().dialogAlert.dismiss();
        }
    }

    private static class MySQLUpdateArCondicionado extends AsyncTask<String, Void, String>
    {
        private WeakReference<MyAdapterProf> activity;
        private String sala;
        private int arCondicionado;
        private String professor;

        MySQLUpdateArCondicionado(MyAdapterProf context, String sala, int arCondicionado, String professor)
        {
            activity = new WeakReference<>(context);
            this.sala = sala;
            this.arCondicionado = arCondicionado;
            this.professor = professor;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            activity.get().dialogAlert = new Dialog(activity.get().context);
            activity.get().dialogAlert.setContentView(R.layout.dialog);
            activity.get().dialogAlert.setCancelable(false);
            activity.get().dialogAlert.show();
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
                st.execute("update sala set professor='" + professor + "'" + "where sala='" + sala + "'");
            }
            catch (Exception e)
            {
                e.printStackTrace();

                activity.get().dialogAlert.dismiss();
            }
            return "task finished";
        }

        @Override
        protected void onPostExecute(String result)
        {
            activity.get().dialogAlert.dismiss();
        }
    }

    private static class MySQLUpdateAssistente extends AsyncTask<String, Void, String>
    {
        private WeakReference<MyAdapterProf> activity;
        private String sala;
        private int assistente;
        private String professor;

        MySQLUpdateAssistente(MyAdapterProf context, String sala, int assistente, String professor)
        {
            activity = new WeakReference<>(context);
            this.sala = sala;
            this.assistente = assistente;
            this.professor = professor;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            activity.get().dialogAlert = new Dialog(activity.get().context);
            activity.get().dialogAlert.setContentView(R.layout.dialog);
            activity.get().dialogAlert.setCancelable(false);
            activity.get().dialogAlert.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(url, user, pass);

                Statement st = con.createStatement();

                st.execute("update sala set assistente='" + assistente + "'" + "where sala='" + sala + "'");
                st.execute("update sala set professor='" + professor + "'" + "where sala='" + sala + "'");
            }
            catch (Exception e)
            {
                e.printStackTrace();

                activity.get().dialogAlert.dismiss();
            }
            return "task finished";
        }

        @Override
        protected void onPostExecute(String result)
        {
            activity.get().dialogAlert.dismiss();
        }
    }

    private static class MySQLUpdateRecursos extends AsyncTask<String, Void, String>
    {
        private WeakReference<MyAdapterProf> activity;
        private String sala;
        private int recursos;
        private String professor;
        private String recursosTXT;

        MySQLUpdateRecursos(MyAdapterProf context, String sala, int recursos, String professor, String recursosTXT)
        {
            activity = new WeakReference<>(context);
            this.sala = sala;
            this.recursos = recursos;
            this.professor = professor;
            this.recursosTXT = recursosTXT;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            activity.get().dialogAlert = new Dialog(activity.get().context);
            activity.get().dialogAlert.setContentView(R.layout.dialog);
            activity.get().dialogAlert.setCancelable(false);
            activity.get().dialogAlert.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(url, user, pass);

                Statement st = con.createStatement();

                st.execute("update sala set recursos='" + recursos + "'" + "where sala='" + sala + "'");
                st.execute("update sala set professor='" + professor + "'" + "where sala='" + sala + "'");
                st.execute("update sala set recursosTXT='" + recursosTXT + "'" + "where sala='" + sala + "'");
            }
            catch (Exception e)
            {
                e.printStackTrace();

                activity.get().dialogAlert.dismiss();
            }
            return "task finished";
        }

        @Override
        protected void onPostExecute(String result)
        {
            activity.get().dialogAlert.dismiss();
        }
    }

    private String GetCodigo()
    {
        StringBuilder datax = new StringBuilder();
        try
        {
            FileInputStream fIn = context.openFileInput(MainActivity.nameCod);
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