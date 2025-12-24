package com.example.myreadbookapplication.activity.Admin;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdminFragmentPagerAdapter extends FragmentStateAdapter {
    
    private final List<Fragment> fragments = new ArrayList<>();
    private final List<String> titles = new ArrayList<>();
    
    public AdminFragmentPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    
    public void addFragment(Fragment fragment, String title) {
        fragments.add(fragment);
        titles.add(title);
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position >= 0 && position < fragments.size()) {
            return fragments.get(position);
        }
        return fragments.get(0); // Default
    }
    
    @Override
    public int getItemCount() {
        return fragments.size();
    }
    
    public Fragment getFragment(int position) {
        if (position >= 0 && position < fragments.size()) {
            return fragments.get(position);
        }
        return null;
    }
    
    public String getTitle(int position) {
        if (position >= 0 && position < titles.size()) {
            return titles.get(position);
        }
        return "";
    }
}

