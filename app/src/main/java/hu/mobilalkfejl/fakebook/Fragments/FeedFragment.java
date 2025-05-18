package hu.mobilalkfejl.fakebook.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.mobilalkfejl.fakebook.Adapters.PostAdapter;
import hu.mobilalkfejl.fakebook.Models.Posts;
import hu.mobilalkfejl.fakebook.R;

public class FeedFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private final List<Posts> postList = new ArrayList<>();
    private final Map<String, String> userIdToNameMap = new HashMap<>();
    private FirebaseFirestore db;
    private DatabaseReference usersDbRef;
    private String currentUserId;

    public FeedFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        usersDbRef = FirebaseDatabase.getInstance().getReference("users");
        currentUserId = FirebaseAuth.getInstance().getUid();

        loadUsersThenPosts();

        return view;
    }

    private void loadUsersThenPosts() {
        usersDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userIdToNameMap.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String uid = userSnapshot.getKey();
                    String name = userSnapshot.child("nev").getValue(String.class);
                    if (name == null) {
                        name = "Ismeretlen felhasználó";
                    }
                    userIdToNameMap.put(uid, name);
                }
                loadPosts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Felhasználók betöltése sikertelen.", Toast.LENGTH_SHORT).show();
                Log.e("FeedFragment", "Failed to load users", error.toException());
            }
        });
    }

    private void loadPosts() {
        postList.clear();
        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Posts post = document.toObject(Posts.class);
                            post.setId(document.getId());
                            postList.add(post);
                        }
                        if (postAdapter == null) {
                            postAdapter = new PostAdapter(postList, getContext(), currentUserId, userIdToNameMap);
                            recyclerView.setAdapter(postAdapter);
                        } else {
                            postAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(getContext(), "Posztok betöltése sikertelen.", Toast.LENGTH_SHORT).show();
                        Log.e("FeedFragment", "Failed to load posts", task.getException());
                    }
                });
    }
}