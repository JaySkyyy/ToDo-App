package appcr.adminpc.todolist.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import appcr.adminpc.todolist.Task;

@Dao
public interface OnDataBaseAction {

    @Query("SELECT * FROM Task")
    List<Task> getAllTasksList();

    @Query("DELETE FROM Task")
    void truncateTheList();

    @Insert
    long insertDataIntoTaskList(Task task);

    @Query("UPDATE Task SET isComplete = :isComplete WHERE taskId = :taskId")
    void updateTaskStatus(int taskId, boolean isComplete);

    @Query("DELETE FROM Task WHERE taskId = :taskId")
    void deleteTaskFromId(int taskId);

    @Query("SELECT * FROM Task WHERE taskId = :taskId")
    Task selectDataFromAnId(int taskId);

    @Query("UPDATE Task SET taskTitle = :taskTitle, taskDescription = :taskDescription, date = :taskDate, " +
            "lastAlarm = :taskTime WHERE taskId = :taskId")
    void updateAnExistingRow(int taskId, String taskTitle, String taskDescription , String taskDate, String taskTime);

    @Query("UPDATE Task SET taskDescription = :taskDescription WHERE taskId = :taskId")
    void updateDescription(int taskId, String taskDescription);

    @Query("SELECT * FROM Task WHERE (:category IS 'Все задачи' OR category = :category)")
    List<Task> getTasksByCategory(String category);

    @Query("SELECT * FROM Task WHERE date = :taskDate")
    List<Task> getTasksByDate(String taskDate);
}
