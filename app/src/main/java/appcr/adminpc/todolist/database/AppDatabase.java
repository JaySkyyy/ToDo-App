package appcr.adminpc.todolist.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import appcr.adminpc.todolist.Task;

@Database(entities = {Task.class}, version = 1, exportSchema = false)
public  abstract class AppDatabase extends RoomDatabase {

    public abstract OnDataBaseAction dataBaseAction();
}

