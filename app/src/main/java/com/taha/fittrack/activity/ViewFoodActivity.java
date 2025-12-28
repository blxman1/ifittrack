package com.taha.fittrack.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.taha.fittrack.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewFoodActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView toolbarUserImage;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference foodDiaryDatabaseReference, foodDatabaseReference, userDatabaseReference;

    private TextView foodName, servingSize, numberOfServings, calories, carbs, fat, protein, createdBy;
    private Button updateBtn, deleteBtn;
    private LinearLayout numberOfServingsContainer;

    private String foodNameValue, servingSizeValue, numberOfServingValue, servingSizeUnitValue,
            caloriesValue, carbsValue, fatValue, proteinValue, foodCreator, createdByValue;

    private String currentUserID;
    private String diaryDate;
    private String diaryEntryKey;
    private String foodType;
    private String foodID;

    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        /* Adding tool bar & title */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Food Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        foodDiaryDatabaseReference = FirebaseDatabase.getInstance().getReference().child("FoodDiaries");
        foodDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Foods");
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        // Get data from intent
        Intent intent = getIntent();
        diaryEntryKey = intent.getStringExtra("diaryEntryKey");
        diaryDate = intent.getStringExtra("diaryDate");
        foodType = intent.getStringExtra("foodType");

        // Get current date if not provided
        if (TextUtils.isEmpty(diaryDate)) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
            diaryDate = currentDateFormat.format(calendar.getTime());
        }

        // Initialize views
        foodName = (TextView) findViewById(R.id.addfood_foodname);
        servingSize = (TextView) findViewById(R.id.add_food_serving_size);
        numberOfServings = (TextView) findViewById(R.id.add_food_number_of_serving);
        calories = (TextView) findViewById(R.id.addfood_calories);
        carbs = (TextView) findViewById(R.id.addfood_carbs);
        fat = (TextView) findViewById(R.id.addfood_fat);
        protein = (TextView) findViewById(R.id.addfood_protein);
        createdBy = (TextView) findViewById(R.id.addfood_createdby);
        numberOfServingsContainer = findViewById(R.id.add_food_number_of_servings_container);

        updateBtn = (Button) findViewById(R.id.addfood_addbutton);
        deleteBtn = (Button) findViewById(R.id.addfood_deletebutton);

        deleteBtn.setVisibility(View.VISIBLE);
        updateBtn.setText("Update");

        loadingbar = new ProgressDialog(this);

        // Load food details
        loadFoodDetails();

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFoodInDiary();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFoodFromDiary();
            }
        });

        numberOfServings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupnumberOfServingEditDialog();
            }
        });
    }

    private void loadFoodDetails() {
        if (!TextUtils.isEmpty(diaryEntryKey)) {
            foodDiaryDatabaseReference.child(currentUserID).child(diaryDate).child(diaryEntryKey)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                foodID = dataSnapshot.child("foodID").getValue(String.class);
                                if (dataSnapshot.hasChild("numberOfServing")) {
                                    numberOfServingValue = dataSnapshot.child("numberOfServing").getValue().toString();
                                    numberOfServings.setText(numberOfServingValue);
                                }
                                else
                                {
                                    numberOfServingValue = "1";
                                    numberOfServings.setText("1");
                                }

                                if (!TextUtils.isEmpty(foodID)) {
                                    loadFoodNutritionDetails();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
        }
        else {
            finish();
        }
    }

    private void loadFoodNutritionDetails() {
        foodDatabaseReference.child(foodID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("foodname")) {
                        foodNameValue = dataSnapshot.child("foodname").getValue().toString();
                        foodName.setText(foodNameValue);
                    }

                    if (dataSnapshot.hasChild("foodservingsize") && dataSnapshot.hasChild("foodservingsizeunit")) {
                        servingSizeValue = dataSnapshot.child("foodservingsize").getValue().toString();
                        servingSizeUnitValue = dataSnapshot.child("foodservingsizeunit").getValue().toString();
                        servingSize.setText(servingSizeValue + " " + servingSizeUnitValue);
                    }

                    if (dataSnapshot.hasChild("foodcalories")) {
                        caloriesValue = dataSnapshot.child("foodcalories").getValue().toString();
                        updateNutritionDisplay();
                    }

                    if (dataSnapshot.hasChild("foodcarbs")) {
                        carbsValue = dataSnapshot.child("foodcarbs").getValue().toString();
                        updateNutritionDisplay();
                    }

                    if (dataSnapshot.hasChild("foodfat")) {
                        fatValue = dataSnapshot.child("foodfat").getValue().toString();
                        updateNutritionDisplay();
                    }

                    if (dataSnapshot.hasChild("foodprotein")) {
                        proteinValue = dataSnapshot.child("foodprotein").getValue().toString();
                        updateNutritionDisplay();
                    }

                    if (dataSnapshot.hasChild("foodcreator")) {
                        foodCreator = dataSnapshot.child("foodcreator").getValue().toString();
                        userDatabaseReference.child(foodCreator).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.hasChild("username")) {
                                        createdByValue = dataSnapshot.child("username").getValue().toString();
                                        createdBy.setText(createdByValue);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                createdBy.setText("FitTrack Team");
                            }
                        });
                    } else {
                        createdBy.setText("FitTrack Team");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void updateNutritionDisplay() {
        if (!TextUtils.isEmpty(numberOfServingValue) && !TextUtils.isEmpty(caloriesValue)) {
            String newCaloriesValue = String.format(Locale.US, "%.0f",
                    (Double.parseDouble(numberOfServingValue) * Double.parseDouble(caloriesValue)));
            calories.setText(newCaloriesValue + " Calories");
        }

        if (!TextUtils.isEmpty(numberOfServingValue) && !TextUtils.isEmpty(carbsValue)) {
            String newCarbsValue = String.format(Locale.US, "%.1f",
                    (Double.parseDouble(numberOfServingValue) * Double.parseDouble(carbsValue)));
            carbs.setText(newCarbsValue + "g");
        }

        if (!TextUtils.isEmpty(numberOfServingValue) && !TextUtils.isEmpty(fatValue)) {
            String newFatValue = String.format(Locale.US, "%.1f",
                    (Double.parseDouble(numberOfServingValue) * Double.parseDouble(fatValue)));
            fat.setText(newFatValue + "g");
        }

        if (!TextUtils.isEmpty(numberOfServingValue) && !TextUtils.isEmpty(proteinValue)) {
            String newProteinValue = String.format(Locale.US, "%.1f",
                    (Double.parseDouble(numberOfServingValue) * Double.parseDouble(proteinValue)));
            protein.setText(newProteinValue + "g");
        }
    }

    private void PopupnumberOfServingEditDialog() {
        final Dialog numberOfServingEditDialog = new Dialog(this);
        numberOfServingEditDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        numberOfServingEditDialog.setContentView(R.layout.number_of_servings_edit_layout);
        numberOfServingEditDialog.setTitle("Edit Serving Size");
        numberOfServingEditDialog.show();
        Window servingSizeEditWindow = numberOfServingEditDialog.getWindow();
        servingSizeEditWindow.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final EditText numberOfServingsInput = (EditText) numberOfServingEditDialog.findViewById(R.id.number_of_servings_dialog_input);
        final TextView errorMsg = (TextView) numberOfServingEditDialog.findViewById(R.id.number_of_servings_dialog_error);
        errorMsg.setVisibility(View.GONE);

        // Set current value
        if (!TextUtils.isEmpty(numberOfServingValue)) {
            numberOfServingsInput.setText(numberOfServingValue);
            numberOfServingsInput.setSelection(numberOfServingsInput.getText().length());
        }

        Button cancelBtn = (Button) numberOfServingEditDialog.findViewById(R.id.number_of_servings_dialog_cancel_button);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberOfServingEditDialog.cancel();
            }
        });

        Button submitBtn = (Button) numberOfServingEditDialog.findViewById(R.id.number_of_servings_dialog_submit_button);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = numberOfServingsInput.getText().toString();
                if (TextUtils.isEmpty(input) || Double.parseDouble(input) <= 0) {
                    errorMsg.setVisibility(View.VISIBLE);
                } else {
                    numberOfServingValue = input;
                    numberOfServings.setText(numberOfServingValue);
                    numberOfServingEditDialog.cancel();
                    updateNutritionDisplay();
                }
            }
        });
    }

    private void updateFoodInDiary() {
        if (TextUtils.isEmpty(numberOfServingValue) || Double.parseDouble(numberOfServingValue) <= 0) {
            return;
        }

        loadingbar = new ProgressDialog(this);
        String ProgressDialogMessage = "Updating Food...";
        SpannableString spannableMessage = new SpannableString(ProgressDialogMessage);
        spannableMessage.setSpan(new RelativeSizeSpan(1.3f), 0, spannableMessage.length(), 0);
        loadingbar.setMessage(spannableMessage);
        loadingbar.show();
        loadingbar.setCanceledOnTouchOutside(false);
        loadingbar.setCancelable(false);

        HashMap<String, Object> diaryMap = new HashMap<>();
        diaryMap.put("numberOfServing", numberOfServingValue);

        foodDiaryDatabaseReference.child(currentUserID).child(diaryDate).child(diaryEntryKey)
                .updateChildren(diaryMap)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        loadingbar.dismiss();
                        if (task.isSuccessful()) {

                            setResult(RESULT_OK);
                            finish();
                        }
                    }
                });
    }

    private void deleteFoodFromDiary() {
        // Show confirmation dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Delete Food");
        builder.setMessage("Are you sure you want to delete this food from your diary?");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            loadingbar = new ProgressDialog(ViewFoodActivity.this);
            loadingbar.setMessage("Deleting food...");
            loadingbar.show();

            foodDiaryDatabaseReference.child(currentUserID).child(diaryDate).child(diaryEntryKey)
                    .removeValue()
                    .addOnCompleteListener(task -> {
                        loadingbar.dismiss();
                        if (task.isSuccessful()) {

                            setResult(RESULT_OK);
                            finish();
                        }
                    });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}