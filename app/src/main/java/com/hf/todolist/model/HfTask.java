package com.hf.todolist.model;

import android.os.Parcel;
import android.os.Parcelable;

public class HfTask implements Parcelable {
    private long id;
    private String name;
    private String category;
    private String dueDate;
    private int priority;
    private boolean completed; // 新增的属性，标记任务是否已完成

    public HfTask() {
        // 默认构造函数
    }

    public HfTask(String name, String category, String dueDate, int priority, boolean completed) {
        this.name = name;
        this.category = category;
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = completed;
    }

//    public HfTask(String name, String category, String dueDate, int priority) {
//        this(name, category, dueDate, priority, false);
//    }

    protected HfTask(Parcel in) {
        id = in.readLong();
        name = in.readString();
        category = in.readString();
        dueDate = in.readString();
        priority = in.readInt();
        completed = in.readByte() != 0; // 从 Parcel 中读取 boolean
    }

    public static final Creator<HfTask> CREATOR = new Creator<HfTask>() {
        @Override
        public HfTask createFromParcel(Parcel in) {
            return new HfTask(in);
        }

        @Override
        public HfTask[] newArray(int size) {
            return new HfTask[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(category);
        dest.writeString(dueDate);
        dest.writeInt(priority);
        dest.writeByte((byte) (completed ? 1 : 0)); // 将 boolean 写入 Parcel
    }
}
