package com.hf.todolist.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hf.todolist.R;

public class StatisticsFragment extends Fragment {

    private TextView textViewTotalTasks, textViewCompletedTasks, textViewPendingTasks;
    private FirebaseFirestore db;

    public StatisticsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        textViewTotalTasks = view.findViewById(R.id.textViewTotalTasks);
        textViewCompletedTasks = view.findViewById(R.id.textViewCompletedTasks);
        textViewPendingTasks = view.findViewById(R.id.textViewPendingTasks);

        db = FirebaseFirestore.getInstance();
        loadStatistics();

        return view;
    }

    private void loadStatistics() {
        // 从 Firestore 中加载统计数据
    }
}
