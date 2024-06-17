package com.hf.todolist.adapter;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hf.todolist.R;
import com.hf.todolist.dao.TaskDAO;
import com.hf.todolist.model.HfTask;

import java.util.Calendar;
import java.util.List;

public class HfTaskAdapter extends RecyclerView.Adapter<HfTaskAdapter.TaskViewHolder> {
    private List<HfTask> taskList;
    private Context context;
    private List<String> categoryList;
    private TaskDAO taskDAO;

    private OnTaskStatusChangedListener statusChangedListener;

    public interface OnTaskStatusChangedListener {
        void onTaskStatusChanged(HfTask task);
    }

    public HfTaskAdapter(List<HfTask> taskList, Context context, List<String> categoryList, TaskDAO taskDAO) {
        this.taskList = taskList;
        this.context = context;
        this.categoryList = categoryList;
        this.taskDAO = taskDAO;
    }

    public HfTaskAdapter(List<HfTask> taskList, Context context, List<String> categoryList, TaskDAO taskDAO, OnTaskStatusChangedListener statusChangedListener) {
        this.taskList = taskList;
        this.context = context;
        this.categoryList = categoryList;
        this.taskDAO = taskDAO;
        this.statusChangedListener = statusChangedListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        HfTask task = taskList.get(position);

        // Resetting views to avoid recycling issues
        holder.taskName.setText("");
        holder.category.setText("");
        holder.dueDate.setText("");
        holder.priority.setText("");

        // Setting new data
        holder.taskName.setText(task.getName());
        holder.category.setText(task.getCategory());
        holder.dueDate.setText(task.getDueDate());
        holder.priority.setText(String.valueOf(task.getPriority()));

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(context,
                R.array.task_status_array, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerStatus.setAdapter(statusAdapter);
        holder.spinnerStatus.setSelection(task.isCompleted() ? 0 : 1);

        holder.itemView.setOnClickListener(v -> showEditTaskDialog(task, holder.getAdapterPosition()));
        holder.itemView.setOnLongClickListener(v -> {
            showDeleteTaskDialog(task, holder.getAdapterPosition());
            return true;
        });

        // 在 HfTaskAdapter 内部的状态变化监听器中
        holder.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                boolean completed = pos == 0;
                if (task.isCompleted() != completed) {
                    task.setCompleted(completed);
                    int rowsAffected = taskDAO.updateTask(task);
                    if (rowsAffected > 0) {
                        // 更新任务列表
                        taskList.set(holder.getAdapterPosition(), task);
                        notifyItemChanged(holder.getAdapterPosition());
                        if (statusChangedListener != null) {
                            statusChangedListener.onTaskStatusChanged(task);
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

    }


    private void showDeleteTaskDialog(HfTask task, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("删除任务");
        builder.setMessage("确定要删除该任务吗？");

        builder.setPositiveButton("删除", (dialog, which) -> {
            try {
                System.out.println("删除任务 ID: " + task.getId() + " 位置: " + position);

                taskDAO.deleteTask(task.getId());

                taskList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, taskList.size());

                if (taskList.isEmpty()) {
                    notifyDataSetChanged();
                }

                Toast.makeText(context, "任务已删除", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, "删除任务时出错", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateTaskList(List<HfTask> taskList) {
        this.taskList = taskList;
        notifyDataSetChanged();
    }

    public void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        EditText editTextTaskName = dialogView.findViewById(R.id.editTextTaskName);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        EditText editTextDueDate = dialogView.findViewById(R.id.editTextDueDate);
        EditText editTextPriority = dialogView.findViewById(R.id.editTextPriority);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // 点击事件显示日期选择器
        editTextDueDate.setOnClickListener(v -> showDatePickerDialog(editTextDueDate));

        builder.setPositiveButton("添加", (dialog, which) -> {
            String name = editTextTaskName.getText().toString();
            String category = (String) spinnerCategory.getSelectedItem();
            String dueDate = editTextDueDate.getText().toString();
            int priority;
            try {
                priority = Integer.parseInt(editTextPriority.getText().toString());
            } catch (NumberFormatException e) {
                priority = 1; // 默认优先级
            }

            HfTask newTask = new HfTask(name, category, dueDate, priority, false);
            taskDAO.addTask(newTask);

            // 更新任务列表
            taskList.add(newTask);
            updateTaskList(taskList);
            notifyDataSetChanged();

            Toast.makeText(context, "任务已添加", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());

        builder.show();
    }


    private void showEditTaskDialog(HfTask task, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        EditText editTextTaskName = dialogView.findViewById(R.id.editTextTaskName);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        EditText editTextDueDate = dialogView.findViewById(R.id.editTextDueDate);
        EditText editTextPriority = dialogView.findViewById(R.id.editTextPriority);

        editTextTaskName.setText(task.getName());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        int categoryPosition = adapter.getPosition(task.getCategory());
        if (categoryPosition >= 0) {
            spinnerCategory.setSelection(categoryPosition);
        }

        editTextDueDate.setText(task.getDueDate());
        editTextPriority.setText(String.valueOf(task.getPriority()));

        // 点击事件显示日期选择器
        editTextDueDate.setOnClickListener(v -> showDatePickerDialog(editTextDueDate));

        builder.setPositiveButton("保存", (dialog, which) -> {
            String name = editTextTaskName.getText().toString();
            String category = (String) spinnerCategory.getSelectedItem();
            String dueDate = editTextDueDate.getText().toString();
            int priority;
            try {
                priority = Integer.parseInt(editTextPriority.getText().toString());
            } catch (NumberFormatException e) {
                priority = 1;
            }

            task.setName(name);
            task.setCategory(category);
            task.setDueDate(dueDate);
            task.setPriority(priority);

            taskDAO.updateTask(task);

            notifyItemChanged(position);
            Toast.makeText(context, "任务已修改", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showDatePickerDialog(final EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                    editText.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskName;
        TextView category;
        TextView dueDate;
        TextView priority;
        Spinner spinnerStatus;

        TaskViewHolder(View view) {
            super(view);
            taskName = view.findViewById(R.id.taskName);
            category = view.findViewById(R.id.category);
            dueDate = view.findViewById(R.id.dueDate);
            priority = view.findViewById(R.id.priority);
            spinnerStatus = view.findViewById(R.id.spinnerStatus);
        }
    }
}
