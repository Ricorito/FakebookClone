package hu.mobilalkfejl.fakebook.Fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import hu.mobilalkfejl.fakebook.Activities.ProfileActivity;
import hu.mobilalkfejl.fakebook.MainActivity;
import hu.mobilalkfejl.fakebook.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final int EDIT_PROFILE_REQUEST = 100;
    private static final int PICK_IMAGE_REQUEST = 1;

    private DatabaseReference myRef;
    private TextView fullname, email;
    private CircleImageView ProfileImage;
    private Uri imageUri;
    private UploadTask uploadTask;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference myStorageRef = storage.getReference().child("images");

        fullname = view.findViewById(R.id.fullname_profile);
        email = view.findViewById(R.id.email_profile);
        MaterialButton btnEditProfile = view.findViewById(R.id.btnEditProfile);
        MaterialButton btnLogout = view.findViewById(R.id.logout_profile);
        ProfileImage = view.findViewById(R.id.profile_image);

        btnEditProfile.setOnClickListener(v -> {
            Log.d(TAG, "Edit profile button clicked");
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST);
        });

        btnLogout.setOnClickListener(v -> {
            Log.d(TAG, "Logout button clicked");
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        ProfileImage.setOnClickListener(v -> {
            Log.d(TAG, "Profile image clicked");
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        loadUserData();
        return view;
    }

    private void loadUserData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(getContext(), "Felhasználó nincs bejelentkezve!", Toast.LENGTH_SHORT).show();
            return;
        }

        myRef.child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("nev").getValue(String.class);
                    String emailValue = snapshot.child("email").getValue(String.class);

                    if (snapshot.hasChild("profile")) {
                        String profileUrl = snapshot.child("profile").getValue(String.class);
                        if (profileUrl != null && !profileUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(profileUrl)
                                    .placeholder(R.drawable.avatar_profile)
                                    .into(ProfileImage);
                        } else {
                            ProfileImage.setImageResource(R.drawable.avatar_profile);
                        }
                    } else {
                        ProfileImage.setImageResource(R.drawable.avatar_profile);
                    }

                    fullname.setText(name != null ? name : "Nincs adat");
                    email.setText(emailValue != null ? emailValue : "Nincs adat");

                } else {
                    Toast.makeText(getContext(), "Felhasználói adat nem található!", Toast.LENGTH_SHORT).show();
                    fullname.setText("Nincs adat");
                    email.setText("Nincs adat");
                    ProfileImage.setImageResource(R.drawable.avatar_profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Adatbázis hiba: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == RESULT_OK) {
            loadUserData();
            Toast.makeText(getContext(), "Profil sikeresen frissítve!", Toast.LENGTH_SHORT).show();
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST);
        }
    }
}