package com.hf.todolist;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hf.todolist.adapter.CategoryAdapter;
import com.hf.todolist.dao.CategoryDAO;

import java.util.ArrayList;
import java.util.List;

public class CategoryManagementActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {

    private RecyclerView recyclerView;
    private CategoryAdapter categoryAdapter;
    private CategoryDAO categoryDAO;
    private List<String> categoryList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        categoryDAO = new CategoryDAO(this);
        categoryDAO.open();

        categoryList = categoryDAO.getAllCategories();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        categoryAdapter = new CategoryAdapter(categoryList, this);
        recyclerView.setAdapter(categoryAdapter);

        FloatingActionButton fab = findViewById(R.id.fab_add_category);
        fab.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("新增分类");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("添加", (dialog, which) -> {
            String newCategory = input.getText().toString().trim();
            if (!newCategory.isEmpty()) {
                categoryDAO.addCategory(newCategory);
                categoryList.add(newCategory);
                categoryAdapter.notifyItemInserted(categoryList.size() - 1);
            }
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showEditCategoryDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修改分类");

        final EditText input = new EditText(this);
        input.setText(categoryList.get(position));
        builder.setView(input);

        builder.setPositiveButton("修改", (dialog, which) -> {
            String updatedCategory = input.getText().toString().trim();
            if (!updatedCategory.isEmpty()) {
                String oldCategory = categoryList.get(position);
                categoryDAO.updateCategory(oldCategory, updatedCategory);
                categoryList.set(position, updatedCategory);
                categoryAdapter.notifyItemChanged(position);
            }
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showDeleteCategoryDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除分类");
        builder.setMessage("确定要删除该分类吗？");

        builder.setPositiveButton("删除", (dialog, which) -> {
            String category = categoryList.get(position);
            categoryDAO.deleteCategory(category);
            categoryList.remove(position);
            categoryAdapter.notifyItemRemoved(position);
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public static List<String> getCategoryList(Context context) {
        CategoryDAO categoryDAO = new CategoryDAO(context);
        categoryDAO.open(); // 打开数据库连接
        List<String> categories = categoryDAO.getAllCategories(); // 从数据库中获取分类列表
        categoryDAO.close(); // 关闭数据库连接
        return categories;
    }

    @Override
    public void onCategoryClick(int position) {
        showEditCategoryDialog(position);
    }

    @Override
    public void onCategoryLongClick(int position) {
        showDeleteCategoryDialog(position);
    }

    @Override
    protected  void onDestroy() {
        super.onDestroy();
        categoryDAO.close();
    }
}
