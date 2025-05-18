package hu.mobilalkfejl.fakebook.Activities;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.app.Activity;
import android.util.Log;

import java.util.Objects;

public class LoginActivity {

    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public static void register(String email, String password, String nev, int kor, OnAuthResultListener listener, Activity activity) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            DatabaseReference userRef = FirebaseDatabase.getInstance("https://fakebook-fcc50-default-rtdb.europe-west1.firebasedatabase.app").getReference("users");
                            userRef.child(user.getUid())
                                    .setValue(new User(email, nev, kor))
                                    .addOnSuccessListener(aVoid -> {
                                        listener.onSuccess(user);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FirebaseAuthManager", "Hiba az adatok írása közben: " + e.getMessage());
                                        listener.onFailure(e);
                                    });
                        } else {
                            Log.e("FirebaseAuthManager", "FirebaseUser null, nem sikerült regisztrálni");
                            listener.onFailure(new Exception("A felhasználó objektum null."));
                        }
                    } else {
                        Log.e("FirebaseAuthManager", "Sikertelen felhasználó létrehozás: " + Objects.requireNonNull(task.getException()).getMessage());
                        listener.onFailure(task.getException());
                    }
                });
    }

    public static void login(String email, String password, OnAuthResultListener listener, Activity activity) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        listener.onSuccess(user);
                    } else {
                        Log.e("FirebaseAuthManager", "Bejelentkezés sikertelen: " + Objects.requireNonNull(task.getException()).getMessage());
                        listener.onFailure(task.getException());
                    }
                });
    }

    public static void logout() {
        mAuth.signOut();
    }

    public interface OnAuthResultListener {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception e);
    }

    public static class User {
        public String email;
        public String nev;
        public int kor;

        public User() {}

        public User(String email, String nev, int kor) {
            this.email = email;
            this.nev = nev;
            this.kor = kor;
        }
    }
}