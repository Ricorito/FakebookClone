package hu.mobilalkfejl.fakebook.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import hu.mobilalkfejl.fakebook.MainActivity;
import hu.mobilalkfejl.fakebook.Models.Posts;
import hu.mobilalkfejl.fakebook.R;

public class AddFragment extends Fragment {

    public AddFragment() {
    }

    EditText edtPostsDesc;
    ImageView PostImage;
    MaterialButton PostsNow, CancelNow;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference mStorageRef;
    Uri selectedImageUri = null;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            getActivity().finish();
            return null;
        }

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mStorageRef = storage.getReference().child("post_images");

        edtPostsDesc = view.findViewById(R.id.post_desc);
        PostImage = view.findViewById(R.id.post_image);
        PostsNow = view.findViewById(R.id.publishPostsBtn);
        CancelNow = view.findViewById(R.id.cancelPosts);

        ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        PostImage.setImageURI(selectedImageUri);
                    }
                }
        );

        PostImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        PostsNow.setOnClickListener(v -> {
            String postText = edtPostsDesc.getText().toString().trim();

            if (postText.isEmpty()) {
                Toast.makeText(getContext(), "Írj valamit a poszthoz!", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = mAuth.getUid();
            if (userId == null) {
                Toast.makeText(getContext(), "Nincs bejelentkezett felhasználó!", Toast.LENGTH_SHORT).show();
                return;
            }
            long timestamp = System.currentTimeMillis();

            if (selectedImageUri != null) {
                StorageReference fileRef = mStorageRef.child(selectedImageUri.getLastPathSegment());
                UploadTask uploadTask = fileRef.putFile(selectedImageUri);

                uploadTask.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            Posts posts = new Posts(postText, uri.toString(), userId, timestamp);
                            posts.setLike(0);
                            savePostToDatabase(posts);
                        });
                    } else {
                        Toast.makeText(getContext(), "Sikertelen kép feltöltés!", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Posts posts = new Posts(postText, null, userId, timestamp);
                posts.setLike(0);
                savePostToDatabase(posts);
            }
        });

        CancelNow.setOnClickListener(v -> {
            edtPostsDesc.setText("");
            selectedImageUri = null;
            PostImage.setImageResource(R.drawable.posts_placeholder);

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
        });

        return view;
    }

    private void savePostToDatabase(Posts posts) {
        String userId = mAuth.getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Nincs bejelentkezett felhasználó!", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference newPostRef = db.collection("posts").document();
        posts.setId(newPostRef.getId());
        newPostRef.set(posts)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Sikeres posztolás Firestore-ba!", Toast.LENGTH_SHORT).show();

                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container, new FeedFragment());
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    } else {
                        Toast.makeText(getContext(), "Poszt mentése sikertelen Firestore-ba!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}