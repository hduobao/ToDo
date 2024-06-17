package com.hf.todolist.fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hf.todolist.R;
import com.hf.todolist.common.VerticalSpaceItemDecoration;
import com.hf.todolist.adapter.HfTaskAdapter;
import com.hf.todolist.model.HfTask;

import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment {

    private RecyclerView recyclerViewTasks;
    private HfTaskAdapter taskAdapter;
    private List<HfTask> taskList;

    private boolean showCompletedTasks;

    public TasksFragment() {
        // Required empty public constructor
    }

    public TasksFragment(List<HfTask> taskList, HfTaskAdapter taskAdapter) {
        this.taskList = taskList;
        this.taskAdapter = taskAdapter;
    }

    public TasksFragment(List<HfTask> taskList, HfTaskAdapter taskAdapter, boolean showCompletedTasks) {
        this.taskList = taskList;
        this.taskAdapter = taskAdapter;
        this.showCompletedTasks = showCompletedTasks;
    }

    public HfTaskAdapter getTaskAdapter() {
        return taskAdapter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        recyclerViewTasks = view.findViewById(R.id.recyclerViewTasks);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getActivity()));

        // 添加自定义ItemDecoration
        int verticalSpace = 8; // 8dp 间距
        recyclerViewTasks.addItemDecoration(new VerticalSpaceItemDecoration(verticalSpace));

        recyclerViewTasks.setAdapter(taskAdapter);

        return view;
    }


    public void updateTaskList(List<HfTask> updatedTaskList) {
        this.taskList = updatedTaskList;
        if (taskAdapter != null) {
            taskAdapter.updateTaskList(taskList);
        }
    }

    public static TasksFragment newInstance(List<HfTask> taskList, boolean showCompletedTasks) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("taskList", new ArrayList<>(taskList));
        args.putBoolean("showCompletedTasks", showCompletedTasks);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            taskList = getArguments().getParcelableArrayList("taskList");
            taskAdapter = getArguments().getParcelable("taskAdapter");
        }
    }

}
