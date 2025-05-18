package hu.mobilalkfejl.fakebook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;

import hu.mobilalkfejl.fakebook.Activities.FeedActivity;
import hu.mobilalkfejl.fakebook.Activities.LoginActivity;
import hu.mobilalkfejl.fakebook.Activities.RegisterActivity;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        Button loginButton = findViewById(R.id.loginButton);
        TextView registerButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();


            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Kérlek, töltsd ki az összes mezőt!", Toast.LENGTH_SHORT).show();
                return;
            }

            LoginActivity.login(email, password, new LoginActivity.OnAuthResultListener() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "Sikeres bejelentkezés!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, FeedActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "Bejelentkezési hiba: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("MainActivity", "Bejelentkezés sikertelen: " + e.getMessage());
                    });
                }
            }, MainActivity.this);
        });

        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}