package com.hf.todolist;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.hf.todolist.adapter.HfTaskAdapter;
import com.hf.todolist.adapter.ViewPagerAdapter;
import com.hf.todolist.dao.CategoryDAO;
import com.hf.todolist.dao.TaskDAO;
import com.hf.todolist.fragment.TasksFragment;
import com.hf.todolist.model.HfTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private HfTaskAdapter taskAdapter;
    private List<HfTask> taskList;
    private Spinner spinnerCategory;
    private List<String> categoryList;

    private CategoryDAO categoryDAO;
    private TaskDAO taskDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化视图元素
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recyclerView);

        // 初始化数据库访问对象
        categoryDAO = new CategoryDAO(this);
        categoryDAO.open();

        taskDAO = new TaskDAO(this);
        taskDAO.open();

        // 获取分类列表
        categoryList = getCategoryList();

        // 初始化任务列表
        taskList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new HfTaskAdapter(taskList, this, categoryList, taskDAO);
        recyclerView.setAdapter(taskAdapter);

        // 设置ViewPager和TabLayout
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        // 设置FloatingActionButton点击事件
        fab.setOnClickListener(v -> taskAdapter.showAddTaskDialog());

        // 加载任务列表
        loadTasks();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                showFilterDialog();
                return true;
            case R.id.action_menu:
                showPopupMenu(findViewById(R.id.action_menu));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.menu_main, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_personal_info:
                    Toast.makeText(MainActivity.this, "个人信息", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.action_add_category:
                    Intent intent = new Intent(MainActivity.this, CategoryManagementActivity.class);
                    startActivity(intent);
                    return true;
                default:
                    return false;
            }
        });
        popup.show();
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_filter, null);
        builder.setView(dialogView);

        Spinner spinnerSortOrder = dialogView.findViewById(R.id.spinnerSortOrder);
        Spinner spinnerCategoryFilter = dialogView.findViewById(R.id.spinnerCategoryFilter);
        Spinner spinnerPriorityFilter = dialogView.findViewById(R.id.spinnerPriorityFilter);

        ArrayAdapter<String> sortOrderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"时间降序", "时间升序"});
        sortOrderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortOrder.setAdapter(sortOrderAdapter);

        List<String> categoryFilterList = new ArrayList<>();
        categoryFilterList.add("全部");
        categoryFilterList.addAll(categoryList);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryFilterList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoryFilter.setAdapter(categoryAdapter);

        List<String> priorityFilterList = new ArrayList<>();
        priorityFilterList.add("全部");
        for (int i = 1; i <= 5; i++) {
            priorityFilterList.add(String.valueOf(i));
        }
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorityFilterList);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriorityFilter.setAdapter(priorityAdapter);

        builder.setPositiveButton("应用", (dialog, which) -> {
            String sortOrder = (String) spinnerSortOrder.getSelectedItem();
            String categoryFilter = (String) spinnerCategoryFilter.getSelectedItem();
            String priorityFilter = (String) spinnerPriorityFilter.getSelectedItem();

            applyFilters(sortOrder, categoryFilter, priorityFilter);
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void applyFilters(String sortOrder, String categoryFilter, String priorityFilter) {
        List<HfTask> filteredList = new ArrayList<>(taskList);

        // 根据选择的排序方式排序
        if (sortOrder.equals("时间降序")) {
            Collections.sort(filteredList, (t1, t2) -> t2.getDueDate().compareTo(t1.getDueDate()));
        } else {
            Collections.sort(filteredList, (t1, t2) -> t1.getDueDate().compareTo(t2.getDueDate()));
        }

        // 过滤分类
        if (!categoryFilter.equals("全部")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                filteredList.removeIf(task -> !task.getCategory().equals(categoryFilter));
            }
        }

        // 过滤优先级
        if (!priorityFilter.equals("全部")) {
            int priority = Integer.parseInt(priorityFilter);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                filteredList.removeIf(task -> task.getPriority() != priority);
            }
        }

        // 更新适配器数据
        taskAdapter.updateTaskList(filteredList);
        Toast.makeText(this, "筛选已应用", Toast.LENGTH_SHORT).show();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // 第一个标签页用于未完成的任务
        TasksFragment pendingTasksFragment = new TasksFragment(new ArrayList<>(), new HfTaskAdapter(new ArrayList<>(), this, categoryList, taskDAO));
        adapter.addFragment(pendingTasksFragment, "待办事项");

        // 第二个标签页用于已完成的任务
        TasksFragment completedTasksFragment = new TasksFragment(new ArrayList<>(), new HfTaskAdapter(new ArrayList<>(), this, categoryList, taskDAO));
        adapter.addFragment(completedTasksFragment, "历史记录");

        viewPager.setAdapter(adapter);

        // 监听ViewPager的页面切换，刷新数据
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Do nothing
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    // 当前显示的是待办事项页面
                    TasksFragment pendingFragment = (TasksFragment) adapter.getItem(position);
                    pendingFragment.updateTaskList(taskDAO.getPendingTasks());
                } else if (position == 1) {
                    // 当前显示的是历史记录页面
                    TasksFragment completedFragment = (TasksFragment) adapter.getItem(position);
                    completedFragment.updateTaskList(taskDAO.getCompletedTasks());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Do nothing
            }
        });
    }





    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
        // 打印调试信息
        System.out.println("MainActivity resumed, tasks reloaded.");
    }

    private void loadTasks() {
        if (taskDAO != null) {
            taskList.clear();
            List<HfTask> tasksFromDB = taskDAO.getAllTasks();
            taskList.addAll(tasksFromDB);

            // 分别筛选未完成和已完成的任务
            List<HfTask> pendingTasks = new ArrayList<>();
            List<HfTask> completedTasks = new ArrayList<>();

            for (HfTask task : taskList) {
                if (task.isCompleted()) {
                    completedTasks.add(task);
                } else {
                    pendingTasks.add(task);
                }
            }

            // 更新“待办事项”标签页
            TasksFragment pendingTasksFragment = (TasksFragment) ((ViewPagerAdapter) viewPager.getAdapter()).getItem(0);
            HfTaskAdapter pendingAdapter = pendingTasksFragment.getTaskAdapter();
            pendingAdapter.updateTaskList(pendingTasks);

            // 更新“历史记录”标签页
            TasksFragment completedTasksFragment = (TasksFragment) ((ViewPagerAdapter) viewPager.getAdapter()).getItem(1);
            HfTaskAdapter completedAdapter = completedTasksFragment.getTaskAdapter();
            completedAdapter.updateTaskList(completedTasks);

            // 打印调试信息
            for (HfTask task : taskList) {
                System.out.println("Loaded Task ID: " + task.getId() + ", Name: " + task.getName() + ", isCompleted: " + task.isCompleted());
            }
        }
    }



    public List<String> getCategoryList() {
        return CategoryManagementActivity.getCategoryList(this); // 从数据库获取分类列表
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        categoryDAO.close(); // 关闭数据库连接
        taskDAO.close(); // 关闭数据库连接
    }
}
