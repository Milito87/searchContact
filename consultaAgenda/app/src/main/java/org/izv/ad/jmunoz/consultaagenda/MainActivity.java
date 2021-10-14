package org.izv.ad.jmunoz.consultaagenda;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final int CONTACT_PERMISSION = 1;

    private Button btSearch;
    private EditText etPhone;
    private TextView tvResult;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

        btSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchIfPermited();
            }});
        }

    private void initialize() {

        context=this;
        btSearch=findViewById(R.id.btSearch);
        etPhone=findViewById(R.id.etPhone);
        tvResult =findViewById(R.id.tvResult);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void explain() {
        showRationaleDialog("Read Contacts Permission Required", "Rationale explanation ...", Manifest.permission.READ_CONTACTS, CONTACT_PERMISSION);
        requestPermission();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermission() {

        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, CONTACT_PERMISSION);

    }

    private void search() {
        if(etPhone.getText().length() > 0) {

            Cursor cursor = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                    null,
                    null,
                    null
            );

            int numero = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int nombre = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

            tvResult.setText("");

            while (cursor.moveToNext()) {
                //visualiza los contactos cuyo telefono coincide con la busqueda secuencial
               if(searchLike(cursor.getString(numero))) {
                   tvResult.append("CONTACT: " + cursor.getString(nombre) + " PHONE: " + cursor.getString(numero) + "\n\n");
               }
            }

        }
        else{
            tvResult.setText("");
            Toast.makeText(context, "Empty phone number ...", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchIfPermited() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//version de android posterior o igual a 6

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {//tengo permiso

                search();

            }

            else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {

                explain();//explicar al usuario el porque pide permiso

            }
            else {
                requestPermission(); //primera vez que pide permiso
            }
        }
        else{//version anterior a la 6 y tengo el permiso

            search();

        }

    }

    private void showRationaleDialog(String title, String message, String permission, int requestCode){

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //no hace nada
                    }})
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        requestPermission();

                    }});
        builder.create().show();

    }
    //busqueda secuencial de numero de telefono
    private boolean searchLike(String numero){// String num telefono

        String cad = String.valueOf(etPhone.getText()); // String busqueda
        int count = 0;   //contador de coincidencias entre la String busqueda y la String num telefono
        int min = 0;     //posicion inicial para recorrer la String num telefono
        int j = min;     //posicion de caracteres del String num de telefono
        int i = 0;       //posicion de caracteres deL String busqueda


        while(i < cad.length() || count == cad.length()){
            //si conciden ambos caracteres
            if(compareNumber(i, cad, j, numero)){
                count++;
                //quedan caracteres por recorrer, ponemos el siguiente caracter (busqueda)
                if(i+1 < cad.length()) {
                    i++;
                }
                //no hay mas caracteres (busqueda) que recorrer se acaba la busqueda
                else{
                    break;
                }
                //quedan caracteres por recorrer, ponemos el siguiente caracter (num telefono)
                if(j+1 < numero.length()){
                    j++;
                    /*Cuando hay coincidencia en la busqueda establezco la posicion inicial del caracter,
                    * por la que empezara la siguiente busqueda, de lo contrario,
                    *  volveria a recorrer toda la cadena de caracteres entera*/
                    min=j;
                }
                //no hay mas caracteres (num telefono) que recorrer, ponemos la pos minima del caracter a recorrer
                //si quedan caracteres de busqueda por recorrer seguira buscando desde la pos minima establecida
                else{
                    j=min;
                }
            }
            //si no coinciden ambos caracteres
            else{
                //siguiente caracter (num telefono)
                if(j+1 < numero.length()){
                    j++;
                }
                else{
                    //no quedan caracteres (num telefono) que recorrer
                    //siguiente caracter (busqueda) y pos minima del caracter (num telefono)
                    if(i+1 < cad.length()){
                        i++;
                        j=min;
                    }
                    //no quedan caracteres (busqueda) y (num telefono) que recorrer
                    else{
                        break;
                    }
                }
            }
        }
        //si el numero de coincidencias (count) = tamaño de la String busqueda (cad.length())
        //quiere decir que existe un telefono que coincide con la secuencia de busqueda
        if(count == cad.length()){
            return true;
        }
        else{
            return false;
        }

    }
    //compara  el caracter de String cad con el caracter String num telefono en las posiciones indicadas
    private boolean compareNumber(int i, String cad, int j, String numero){
        if(cad.charAt(i) == numero.charAt(j)){
          return true;
        }
        else{
            return false;
        }
    }

}