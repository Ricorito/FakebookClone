package hu.mobilalkfejl.fakebook.Activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import hu.mobilalkfejl.fakebook.MainActivity;
import hu.mobilalkfejl.fakebook.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etNev, etDateOfBirth, etConfirmPassword;
    private CheckBox cbAgeAgreement;
    private Calendar calendar;
    private DatePickerDialog.OnDateSetListener dateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etNev = findViewById(R.id.nevEditText);
        etEmail = findViewById(R.id.emailEditText);
        etPassword = findViewById(R.id.passwordEditText);
        etConfirmPassword = findViewById(R.id.confirmPasswordEditText);
        etDateOfBirth = findViewById(R.id.dateOfBirthEditText);
        cbAgeAgreement = findViewById(R.id.ageCheckBox);
        Button btnRegister = findViewById(R.id.registerButton);
        TextView backToLoginTextView = findViewById(R.id.backToLoginTextView);

        calendar = Calendar.getInstance();

        etDateOfBirth.setOnClickListener(view -> {
            Calendar minDate = Calendar.getInstance();
            minDate.set(1900, 0, 1);
            Calendar maxDate = Calendar.getInstance();

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    RegisterActivity.this,
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
            datePickerDialog.show();
        });

        dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateInEditText();
        };

        btnRegister.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            String nev = etNev.getText().toString().trim();
            String dateOfBirthStr = etDateOfBirth.getText().toString().trim();

            if (dateOfBirthStr.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Kérlek add meg a születési dátumot!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(RegisterActivity.this, "A jelszavak nem egyeznek!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!cbAgeAgreement.isChecked()) {
                Toast.makeText(RegisterActivity.this, "El kell fogadnod a felhasználói nyilatkozatot!", Toast.LENGTH_SHORT).show();
                return;
            }

            int age = calculateAge(dateOfBirthStr);
            if (age < 13) {
                Toast.makeText(RegisterActivity.this, "Legalább 13 évesnek kell lenned a regisztrációhoz!", Toast.LENGTH_SHORT).show();
                return;
            }

            LoginActivity.register(email, password, nev, age, new LoginActivity.OnAuthResultListener() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "Sikeres regisztráció!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });
                }
                @Override
                public void onFailure(Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "Hiba: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }, RegisterActivity.this);
        });

        backToLoginTextView.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
    private int calculateAge(String dateOfBirthStr) {
        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dateOfBirth = sdf.parse(dateOfBirthStr);
            Calendar birthCalendar = Calendar.getInstance();
            assert dateOfBirth != null;
            birthCalendar.setTime(dateOfBirth);

            Calendar now = Calendar.getInstance();
            int age = now.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR);

            if (now.get(Calendar.MONTH) < birthCalendar.get(Calendar.MONTH) ||
                    (now.get(Calendar.MONTH) == birthCalendar.get(Calendar.MONTH) &&
                            now.get(Calendar.DAY_OF_MONTH) < birthCalendar.get(Calendar.DAY_OF_MONTH))) {
                age--;
            }

            return age;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Hiba a dátum formátumban!", Toast.LENGTH_SHORT).show();
            return -1;
        }
    }
    private void updateDateInEditText() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        etDateOfBirth.setText(sdf.format(calendar.getTime()));
    }
}