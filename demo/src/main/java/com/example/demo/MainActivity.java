package com.example.demo;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.editdateview.EditDateView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    ImageButton calendarButton;
    EditDateView editDate;
    DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        datePickerDialog = new DatePickerDialog(this);
        calendarButton = findViewById(R.id.calendarBtn);
           editDate = findViewById(R.id.editTextNumber);
        editDate.setDateViewFormat(EditDateView.DefaultSettings.DATE_VIEW_FORMAT_DMY);
        editDate.setDayPlaceholder('*');
        editDate.setDateSeparator(EditDateView.DefaultSettings.DATE_SEPARATOR_DOT);
        setListeners();
    }

    private void setListeners (){
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (editDate.getDay() != 0 && editDate.getMonth() != 0 && editDate.getYear() != 0) {
                    datePickerDialog.updateDate(editDate.getYear(), editDate.getMonth() - 1, editDate.getDay());
                } else {
                    Calendar calendar = Calendar.getInstance();
                    datePickerDialog.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
                }
                datePickerDialog.show();
            }
        });

        datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                editDate.setDateInt(dayOfMonth, month + 1, year);
            }
        });
    }
}