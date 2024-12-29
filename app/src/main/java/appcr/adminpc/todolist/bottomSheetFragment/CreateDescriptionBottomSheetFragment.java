package appcr.adminpc.todolist.bottomSheetFragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import appcr.adminpc.todolist.MainActivity;
import appcr.adminpc.todolist.R;
import appcr.adminpc.todolist.Task;
import appcr.adminpc.todolist.database.DatabaseClient;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CreateDescriptionBottomSheetFragment extends BottomSheetDialogFragment {

    Unbinder unbinder;
    @BindView(R.id.redactTaskDescription)
    EditText redactTaskDescription;
    @BindView(R.id.redactDescription)
    Button redactDescription;
    int taskId;
    boolean isEdit;
    Task task;
    MainActivity activity;
    public static int count = 0;
    CreateTaskBottomSheetFragment.setRefreshListener setRefreshListener;

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }
        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    public void setTaskId(int taskId, boolean isEdit, MainActivity activity) {
        this.taskId = taskId;
        this.isEdit = isEdit;
        this.activity = activity;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"RestrictedApi", "ClickableViewAccessibility"})
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_description, null);
        unbinder = ButterKnife.bind(this, contentView);
        dialog.setContentView(contentView);

        redactDescription.setOnClickListener(view -> {
            if(validateFields()) {
                updateDescription();
            }
        });
        if (isEdit) {
            showTaskFromId();
        }
    }

    public boolean validateFields() {
        if(redactTaskDescription.getText().toString().equalsIgnoreCase("")) {
            Toast.makeText(activity, "Введите описание", Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public CreateDescriptionBottomSheetFragment(MainActivity context, List<Task> taskList, CreateTaskBottomSheetFragment.setRefreshListener setRefreshListener) {
        this.setRefreshListener = setRefreshListener;
    }

    private void updateDescription() {
        class saveTaskInBackend extends AsyncTask<Void, Void, Void> {
            @SuppressLint("WrongThread")
            @Override
            protected Void doInBackground(Void... voids) {
                Task createTask = new Task();
                createTask.setTaskDescrption(redactTaskDescription.getText().toString());

                DatabaseClient.getInstance(getActivity()).getAppDatabase()
                        .dataBaseAction()
                        .updateDescription(taskId,
                                redactTaskDescription.getText().toString());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                setRefreshListener.refresh();
                Toast.makeText(getActivity(), "Ваша задача была добавлена", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        }
        saveTaskInBackend st = new saveTaskInBackend();
        st.execute();
    }

    private void showTaskFromId() {
        class showTaskFromId extends AsyncTask<Void, Void, Void> {
            @SuppressLint("WrongThread")
            @Override
            protected Void doInBackground(Void... voids) {
                task = DatabaseClient.getInstance(getActivity()).getAppDatabase()
                        .dataBaseAction().selectDataFromAnId(taskId);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                setDataInUI();
            }
        }
        showTaskFromId st = new showTaskFromId();
        st.execute();
    }

    private void setDataInUI() {
        redactTaskDescription.setText(task.getTaskDescrption());
    }
}
