package hu.mobilalkfejl.fakebook.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import hu.mobilalkfejl.fakebook.Fragments.AddFragment;
import hu.mobilalkfejl.fakebook.Fragments.FeedFragment;
import hu.mobilalkfejl.fakebook.Fragments.NotificationFragment;
import hu.mobilalkfejl.fakebook.Fragments.ProfileFragment;
import hu.mobilalkfejl.fakebook.Fragments.SearchFragment;
import hu.mobilalkfejl.fakebook.MainActivity;
import hu.mobilalkfejl.fakebook.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FeedActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    BottomNavigationView bottomNavigationView;
    Toolbar toolbar;

    private TextView tooltip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        init();
    }

    private void init() {
        bottomNavigationView = findViewById(R.id.bottom_nav);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Fakebook");
        toolbar.setTitleTextColor(Color.WHITE);
        if (toolbar.getOverflowIcon() != null) {
            toolbar.getOverflowIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }

        tooltip = new TextView(this);
        tooltip.setText("Fejlesztés alatt");
        tooltip.setTextColor(Color.WHITE);
        tooltip.setBackgroundResource(R.drawable.tooltip);
        tooltip.setPadding(20, 10, 20, 10);
        tooltip.setVisibility(View.INVISIBLE);

        RelativeLayout rootLayout = findViewById(R.id.root_layout);
        rootLayout.addView(tooltip);

        loadFragment(new FeedFragment());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                ft.replace(R.id.container, new FeedFragment());
            } else if (id == R.id.nav_search) {
                ft.replace(R.id.container, new SearchFragment());
            } else if (id == R.id.nav_add) {
                ft.replace(R.id.container, new AddFragment());
            } else if (id == R.id.nav_notifications) {
                ft.replace(R.id.container, new NotificationFragment());

                animateNavIcon(bottomNavigationView, R.id.nav_notifications);
                showTooltipOverIcon(R.id.nav_notifications);

            } else if (id == R.id.nav_profile) {
                ft.replace(R.id.container, new ProfileFragment());
            }
            ft.commit();
            return true;
        });
    }

    private void animateNavIcon(BottomNavigationView bottomNav, int itemId) {
        View menuItemView = bottomNav.findViewById(itemId);
        if (menuItemView != null) {
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            menuItemView.startAnimation(shake);
        }
    }

    private void showTooltipOverIcon(int itemId) {
        View menuItemView = bottomNavigationView.findViewById(itemId);
        if (menuItemView == null) return;

        int[] location = new int[2];
        menuItemView.getLocationOnScreen(location);

        int x = location[0] + menuItemView.getWidth() / 2 - tooltip.getWidth() / 2;
        int y = location[1] - tooltip.getHeight() - 20; // 20px-rel az ikon fölé

        tooltip.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int tooltipWidth = tooltip.getMeasuredWidth();
        int tooltipHeight = tooltip.getMeasuredHeight();


        x = location[0] + menuItemView.getWidth() / 2 - tooltipWidth / 2;
        y = location[1] - tooltipHeight - 30;

        tooltip.setX(x);
        tooltip.setY(y);

        tooltip.setVisibility(View.VISIBLE);

        ObjectAnimator jumpAnim = ObjectAnimator.ofFloat(tooltip, "translationY", y + 20, y);
        jumpAnim.setDuration(400);
        jumpAnim.setRepeatCount(3);
        jumpAnim.setRepeatMode(ValueAnimator.REVERSE);
        jumpAnim.start();

        tooltip.postDelayed(() -> tooltip.setVisibility(View.INVISIBLE), 2000);
    }

    private void loadFragment(androidx.fragment.app.Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container, fragment);
        ft.commit();
    }
}

