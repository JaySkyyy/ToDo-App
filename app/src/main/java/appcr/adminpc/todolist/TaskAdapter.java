package appcr.adminpc.todolist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import appcr.adminpc.todolist.bottomSheetFragment.CreateDescriptionBottomSheetFragment;
import appcr.adminpc.todolist.bottomSheetFragment.CreateTaskBottomSheetFragment;
import appcr.adminpc.todolist.database.DatabaseClient;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private MainActivity context;
    private LayoutInflater inflater;
    private List<Task> taskList;
    public SimpleDateFormat dateFormat = new SimpleDateFormat("EE dd MMM yyyy", new Locale("ru", "RU"));
    public SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd-M-yyyy", new Locale("ru", "RU"));
    public SimpleDateFormat DateForColors = new SimpleDateFormat("dd-M-yyyy HH:mm", new Locale("ru", "RU"));
    Date date = null;
    Date dateTime = null;
    String outputDateString = null;
    CreateTaskBottomSheetFragment.setRefreshListener setRefreshListener;
    public void setTasks(List<Task> taskList) {
        this.taskList = taskList;
        notifyDataSetChanged();
    }
    public TaskAdapter(MainActivity context, List<Task> taskList,
                       CreateTaskBottomSheetFragment.setRefreshListener setRefreshListener) {
        this.context = context;
        this.taskList = taskList;
        this.setRefreshListener = setRefreshListener;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = inflater.inflate(R.layout.item_task, viewGroup, false);
        return new TaskViewHolder(view);
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
        }
    };

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.title.setText(task.getTaskTitle());
        holder.description.setText(task.getTaskDescrption());
        holder.description.setOnClickListener(view -> {
            CreateDescriptionBottomSheetFragment createDescriptionBottomSheetFragment = new CreateDescriptionBottomSheetFragment(context, taskList, setRefreshListener);
            createDescriptionBottomSheetFragment.setTaskId(task.getTaskId(), true, context);
            createDescriptionBottomSheetFragment.show(context.getSupportFragmentManager(), createDescriptionBottomSheetFragment.getTag());
        });
        holder.time.setText(task.getLastAlarm());
        holder.status.setText(task.isComplete() ? "ЗАВЕРШЕНО" : "В РАБОТЕ");
        holder.options.setOnClickListener(view -> showPopUpMenu(view, position));
        holder.categoryName.setText(task.getCategory());

        CardView cardView = holder.itemView.findViewById(R.id.cardView);
        try {
            date = inputDateFormat.parse(task.getDate());
            outputDateString = dateFormat.format(date);

            String[] items1 = outputDateString.split(" ");
            String day = items1[0];
            String dd = items1[1];
            String month = items1[2];

            holder.day.setText(day);
            holder.date.setText(dd);
            holder.month.setText(month);

            dateTime = DateForColors.parse(task.getTime());
            Calendar currentDate = Calendar.getInstance();
            Calendar taskDate = Calendar.getInstance();
            taskDate.setTime(dateTime);
            long differenceInMillis = taskDate.getTimeInMillis() - currentDate.getTimeInMillis();
            double differenceInDays = (double) differenceInMillis / TimeUnit.DAYS.toMillis(1);
            if (task.isComplete()) {
                cardView.setCardBackgroundColor(Color.parseColor("#70db70"));
            }else if (taskDate.getTimeInMillis() < currentDate.getTimeInMillis()) {
                cardView.setCardBackgroundColor(Color.parseColor("#ff4d4d"));
            }else if (differenceInDays < 1) {
                cardView.setCardBackgroundColor(Color.parseColor("#ffdb4d"));
            } else {
                cardView.setCardBackgroundColor(Color.WHITE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        handler.postDelayed(runnable, 15000);
    }

    public void showPopUpMenu(View view, int position) {
        final Task task = taskList.get(position);
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu, popupMenu.getMenu());
        if (task.isComplete()) {
            MenuItem completeItem = popupMenu.getMenu().findItem(R.id.menuComplete);
            completeItem.setVisible(false);
        }
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menuDelete:
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.AppTheme_Dialog);
                    alertDialogBuilder.setTitle(R.string.delete_confirmation).setMessage(R.string.sureToDelete).
                            setPositiveButton(R.string.yes, (dialog, which) -> {
                                deleteTaskFromId(task.getTaskId(), position);
                                try{
                                    CreateTaskBottomSheetFragment createTaskBottomSheetFragment = new CreateTaskBottomSheetFragment();
                                    createTaskBottomSheetFragment.setTaskId(task.getTaskId(), false, context, context);
                                    createTaskBottomSheetFragment.deleteAnAlarm(context);
                                }catch (Exception e){
                                    Log.d("DeleteAlarm1", "Error " + e);
                                }
                            })
                            .setNegativeButton(R.string.no, (dialog, which) -> dialog.cancel()).show();
                    break;
                case R.id.menuUpdate:
                    try {
                        CreateTaskBottomSheetFragment createTaskBottomSheetFragment = new CreateTaskBottomSheetFragment();
                        createTaskBottomSheetFragment.setTaskId(task.getTaskId(), true, context, context);
                        createTaskBottomSheetFragment.show(context.getSupportFragmentManager(), createTaskBottomSheetFragment.getTag());
                    }catch (Exception ex){
                        Log.d("UpdateCase", "Error2 " + ex);
                    }
                    break;
                case R.id.menuComplete:
                    AlertDialog.Builder completeAlertDialog = new AlertDialog.Builder(context, R.style.AppTheme_Dialog);
                    completeAlertDialog.setTitle(R.string.confirmation).setMessage(R.string.sureToMarkAsComplete).
                            setPositiveButton(R.string.yes, (dialog, which) -> {
                                try{
                                    CreateTaskBottomSheetFragment createTaskBottomSheetFragment = new CreateTaskBottomSheetFragment();
                                    createTaskBottomSheetFragment.setTaskId(task.getTaskId(), false, context, context);
                                    createTaskBottomSheetFragment.deleteAnAlarm(context);
                                }catch (Exception ex){
                                    Log.d("CompleteCase", "Error3 " + ex);
                                }
                                showCompleteDialog(task.getTaskId(), position);
                            })
                            .setNegativeButton(R.string.no, (dialog, which) -> dialog.cancel()).show();
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    public void showCompleteDialog(int taskId, int position) {
        Dialog dialog = new Dialog(context, R.style.AppTheme);
        dialog.setContentView(R.layout.dialog_completed_theme);
        Button close = dialog.findViewById(R.id.closeButton);
        close.setOnClickListener(view -> {

            updateTaskAsCompleted(taskId, position);
            dialog.dismiss();
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
    }

    public void updateTaskAsCompleted(int taskId, int position) {
        Task task = taskList.get(position);

        task.setComplete(true);

        AsyncTask.execute(() -> {
            DatabaseClient.getInstance(context)
                    .getAppDatabase()
                    .dataBaseAction()
                    .updateTaskStatus(taskId, true);

            context.runOnUiThread(() -> {
                notifyItemChanged(position);
                //setTasks(taskList);
            });
        });
    }

    private void deleteTaskFromId(int taskId, int position) {
        class GetSavedTasks extends AsyncTask<Void, Void, List<Task>> {
            @Override
            protected List<Task> doInBackground(Void... voids) {
                DatabaseClient.getInstance(context)
                        .getAppDatabase()
                        .dataBaseAction()
                        .deleteTaskFromId(taskId);
                return taskList;
            }

            @Override
            protected void onPostExecute(List<Task> tasks) {
                super.onPostExecute(tasks);
                removeAtPosition(position);

                setRefreshListener.refresh();
            }
        }
        GetSavedTasks savedTasks = new GetSavedTasks();
        savedTasks.execute();
    }

    private void removeAtPosition(int position) {
        taskList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, taskList.size());
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.day)
        TextView day;
        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.month)
        TextView month;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.description)
        TextView description;
        @BindView(R.id.status)
        TextView status;
        @BindView(R.id.options)
        ImageView options;
        @BindView(R.id.time)
        TextView time;
        @BindView(R.id.categoryName)
        TextView categoryName;
        TaskViewHolder(@NonNull View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
