package hu.mobilalkfejl.fakebook.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

import hu.mobilalkfejl.fakebook.Activities.EditPostActivity;
import hu.mobilalkfejl.fakebook.Models.Posts;
import hu.mobilalkfejl.fakebook.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final List<Posts> postList;
    private final Context context;
    private final String currentUserId;
    private final Map<String, String> userIdToNameMap;
    private FirebaseFirestore db;

    public PostAdapter(List<Posts> postList, Context context, String currentUserId, Map<String, String> userIdToNameMap) {
        this.postList = postList;
        this.context = context;
        this.currentUserId = currentUserId;
        this.userIdToNameMap = userIdToNameMap;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Posts post = postList.get(position);
        SharedPreferences prefs = context.getSharedPreferences("liked_posts", Context.MODE_PRIVATE);
        boolean alreadyLiked = prefs.getBoolean(post.getId(), false);

        int likeCount = post.getLike();
        holder.likeCountTextView.setText(String.valueOf(likeCount));

        if (alreadyLiked) {
            holder.likeButton.setImageResource(R.drawable.ic_like);
        } else {
            holder.likeButton.setImageResource(R.drawable.ic_unlike);
        }

        holder.likeButton.setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.like_scale);
            holder.likeButton.startAnimation(animation);

            boolean isLikedNow = prefs.getBoolean(post.getId(), false);
            SharedPreferences.Editor editor = prefs.edit();

            if (isLikedNow) {
                int newLikeCount = post.getLike() - 1;
                db.collection("posts")
                        .document(post.getId())
                        .update("like", newLikeCount)
                        .addOnSuccessListener(aVoid -> {
                            post.setLike(newLikeCount);
                            holder.likeCountTextView.setText(String.valueOf(newLikeCount));
                            holder.likeButton.setImageResource(R.drawable.ic_unlike);
                            editor.remove(post.getId());
                            editor.apply();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Unlike sikertelen", Toast.LENGTH_SHORT).show()
                        );
            } else {
                int newLikeCount = post.getLike() + 1;
                db.collection("posts")
                        .document(post.getId())
                        .update("like", newLikeCount)
                        .addOnSuccessListener(aVoid -> {
                            post.setLike(newLikeCount);
                            holder.likeCountTextView.setText(String.valueOf(newLikeCount));
                            holder.likeButton.setImageResource(R.drawable.ic_like);
                            editor.putBoolean(post.getId(), true);
                            editor.apply();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Like sikertelen", Toast.LENGTH_SHORT).show()
                        );
            }
        });
        holder.descTextView.setText(post.getDesc());

        long timestampMillis = post.getTimestamp();
        Date date = new Date(timestampMillis);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(date);
        holder.timestampTextView.setText(formattedDate);

        String userName = userIdToNameMap.get(post.getUserId());
        if (userName == null) userName = "Ismeretlen felhasználó";
        holder.userNameTextView.setText(userName);

        if (post.getImage() != null && !post.getImage().isEmpty()) {
            Glide.with(context)
                    .load(post.getImage())
                    .placeholder(R.drawable.posts_placeholder)
                    .into(holder.postImageView);
        } else {
            holder.postImageView.setImageResource(R.drawable.posts_placeholder);
        }

        if (post.getUserId() != null && post.getUserId().equals(currentUserId)) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.editButton.setVisibility(View.VISIBLE);
        } else {
            holder.deleteButton.setVisibility(View.GONE);
            holder.editButton.setVisibility(View.GONE);
        }

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Poszt törlése")
                    .setMessage("Biztosan törölni szeretnéd ezt a posztot?")
                    .setPositiveButton("Igen", (dialog, which) -> deletePost(post, position))
                    .setNegativeButton("Mégse", null)
                    .show();
        });

        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditPostActivity.class);
            intent.putExtra("postId", post.getId());
            context.startActivity(intent);
        });
    }

    private void deletePost(Posts post, int position) {
        if (post.getId() == null) {
            Toast.makeText(context, "Nem sikerült a poszt törlése: hiányzó ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("posts")
                .document(post.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Poszt sikeresen törölve.", Toast.LENGTH_SHORT).show();
                    postList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, postList.size());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Poszt törlése sikertelen.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView descTextView, userNameTextView;
        ImageView postImageView;
        ImageButton deleteButton;
        ImageButton editButton;
        TextView timestampTextView;
        ImageButton likeButton;
        TextView likeCountTextView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            descTextView = itemView.findViewById(R.id.post_desc);
            userNameTextView = itemView.findViewById(R.id.post_user_name);
            postImageView = itemView.findViewById(R.id.post_image);
            editButton = itemView.findViewById(R.id.post_edit_button);
            deleteButton = itemView.findViewById(R.id.post_delete_button);
            timestampTextView = itemView.findViewById(R.id.post_timestamp);
            likeButton = itemView.findViewById(R.id.post_like_button);
            likeCountTextView = itemView.findViewById(R.id.post_like_count);

        }
    }
}