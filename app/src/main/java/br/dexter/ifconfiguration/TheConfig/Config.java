package br.dexter.ifconfiguration.TheConfig;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import br.dexter.ifconfiguration.MainActivity;
import br.dexter.ifconfiguration.R;

public class Config extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        setTitle("IFMA Campus Caxias");

        if(GetCodigo().contains("Professor")) {
            Intent intent = new Intent(this, Professores.class);
            startActivity(intent);
            finish();
        } else if(GetCodigo().contains("Técnico Administrativo")) {
            Intent intent = new Intent(this, TecAdministrativo.class);
            startActivity(intent);
            finish();
        } else if(GetCodigo().contains("Assistente de Aluno")) {
            Intent intent = new Intent(this, Assistente.class);
            startActivity(intent);
            finish();
        }
    }

    public String GetCodigo()
    {
        StringBuilder datax = new StringBuilder();
        try
        {
            FileInputStream fIn = openFileInput(MainActivity.profissionCod);
            InputStreamReader isr = new InputStreamReader(fIn);
            BufferedReader buffreader = new BufferedReader(isr);

            String readString = buffreader.readLine();
            while (readString != null) {
                datax.append(readString);
                readString = buffreader.readLine();
            }

            isr.close();
        } catch (IOException ioe) {
            ioe.printStackTrace() ;
        }
        return datax.toString();
    }
}