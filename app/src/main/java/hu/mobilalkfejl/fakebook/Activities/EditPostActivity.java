package hu.mobilalkfejl.fakebook.Activities;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.util.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import hu.mobilalkfejl.fakebook.R;

public class EditPostActivity extends AppCompatActivity {

    private EditText descEditText;
    private ImageView postImageView;
    private Button saveButton, changeImageButton;
    private ProgressBar progressBar;

    private String postId;
    private String imageUrl;

    private FirebaseFirestore db;
    private StorageReference storageRef;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        descEditText = findViewById(R.id.edit_post_desc);
        postImageView = findViewById(R.id.edit_post_image);
        saveButton = findViewById(R.id.button_save_post);
        changeImageButton = findViewById(R.id.button_change_image);
        progressBar = findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        postId = getIntent().getStringExtra("postId");

        loadPostData();

        changeImageButton.setOnClickListener(v -> openFileChooser());

        saveButton.setOnClickListener(v -> savePost());
    }

    private void loadPostData() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("posts").document(postId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String desc = documentSnapshot.getString("desc");
                        imageUrl = documentSnapshot.getString("image");

                        descEditText.setText(desc);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this).load(imageUrl).into(postImageView);
                        }
                    } else {
                        Toast.makeText(this, "Poszt nem található", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba történt a poszt betöltésekor", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            postImageView.setImageURI(imageUri);
        }
    }

    private void savePost() {
        String newDesc = descEditText.getText().toString().trim();
        if (newDesc.isEmpty()) {
            descEditText.setError("A leírás nem lehet üres");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        if (imageUri != null) {
            StorageReference fileRef = storageRef.child("post_images/" + postId + ".jpg");
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        updatePostInFirestore(newDesc, downloadUrl);
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Kép feltöltése sikertelen", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        saveButton.setEnabled(true);
                    });
        } else {
            updatePostInFirestore(newDesc, imageUrl);
        }
    }

    private void updatePostInFirestore(String desc, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("desc", desc);
        updates.put("image", imageUrl);

        db.collection("posts").document(postId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Poszt sikeresen frissítve", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Poszt frissítése sikertelen", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                });
    }
}