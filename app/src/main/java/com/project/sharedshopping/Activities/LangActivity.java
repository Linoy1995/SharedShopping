package com.project.sharedshopping.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.sharedshopping.R;

import java.util.Locale;
import java.util.Objects;

public class LangActivity extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lang);

        Toolbar toolbar = findViewById(R.id.langToolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.change_lang);  //toolbar title

        //back arrow implementation
        toolbar.setNavigationIcon(getDrawable(R.drawable.ic_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        sharedPref = getSharedPreferences("Language", MODE_PRIVATE);
        String language = sharedPref.getString("lang", null);
        RadioButton en = findViewById(R.id.EnglishRB);
        RadioButton he = findViewById(R.id.HebrewRB);
        if (language != null)
            if (language.equals("en")){
                he.setChecked(false);
                  en.setChecked(true);
            }
        else{
                en.setChecked(false);
                he.setChecked(true);
            }

    }



    public void onRadioButtonClicked(View view) {
        // Check which radio button was clicked
        editor = sharedPref.edit();

        switch(view.getId()) {
            case R.id.EnglishRB:
                    editor.putString("lang","en");
                    break;
            case R.id.HebrewRB:
                    editor.putString("lang","he");
                    break;
        }
        editor.apply();
        Intent intent= new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);// clear the top stack
        startActivity(intent);
    }
}