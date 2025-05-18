package hu.mobilalkfejl.fakebook.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import hu.mobilalkfejl.fakebook.R;

public class ProfileActivity extends AppCompatActivity {

    private EditText edtUsername, edtFullname;
    private MaterialButton nextBtn;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ImageView profileImage = findViewById(R.id.profile_image);
        edtUsername = findViewById(R.id.edt_email);
        edtFullname = findViewById(R.id.edt_fullname);
        nextBtn = findViewById(R.id.Next_btn);
        progressBar = findViewById(R.id.progressbar);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        nextBtn.setOnClickListener(v -> updateProfile());

        loadProfileData();
    }

    private void loadProfileData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            dbRef.child("users").child(user.getUid()).get()
                    .addOnSuccessListener(dataSnapshot -> {
                        if (dataSnapshot.exists()) {
                            String email = dataSnapshot.child("email").getValue(String.class);
                            String fullname = dataSnapshot.child("nev").getValue(String.class);

                            edtUsername.setText(email != null ? email : "");
                            edtFullname.setText(fullname != null ? fullname : "");
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(ProfileActivity.this, "Hiba a profiladatok betöltésekor!", Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void updateProfile() {
        String email = edtUsername.getText().toString().trim();
        String fullname = edtFullname.getText().toString().trim();

        if (email.isEmpty() || fullname.isEmpty()) {
            Toast.makeText(this, "Kérlek töltsd ki az összes mezőt!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        nextBtn.setEnabled(false);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("email", email);
            profileData.put("nev", fullname);

            dbRef.child("users").child(user.getUid()).updateChildren(profileData)
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        nextBtn.setEnabled(true);
                        Toast.makeText(ProfileActivity.this, "Profil sikeresen frissítve!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        nextBtn.setEnabled(true);
                        Toast.makeText(ProfileActivity.this, "Hiba történt a frissítés során!", Toast.LENGTH_SHORT).show();
                    });
        } else {
            progressBar.setVisibility(View.GONE);
            nextBtn.setEnabled(true);
            Toast.makeText(this, "Felhasználó nem található!", Toast.LENGTH_SHORT).show();
        }
    }
}
