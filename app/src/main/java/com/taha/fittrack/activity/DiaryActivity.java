package com.taha.fittrack.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.taha.fittrack.model.Foods;
import com.taha.fittrack.utils.FormulaCalculations;
import com.taha.fittrack.R;
import com.taha.fittrack.model.Workouts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class DiaryActivity extends AppCompatActivity
{

    private Toolbar toolbar;
    private CircleImageView toolbarUserImage;


    private FirebaseAuth firebaseAuth;
    private DatabaseReference foodDiaryDatabaseReference, foodDatabaseReference, userDatabaseReference, waterDiaryDatabaseReference,
            workoutDiaryDatabaseReference, tdeeDiaryDatabaseReference;


    private String strUserCurrentWeight, strUserByear, strUserAge, strUserHeight, strUserGender, strUserActivityLevel,
            strUserFood="0", strUserCarbs="0.0", strUserFat="0.0", strUserProtein="0.0", strUserWorkout="0", userServingSize,
            strUserDailyCalorieIntake, strUserCaloriesRemaining;

    private LinearLayout dateContainer;
    private TextView diaryDate;

    private ImageView dateLeftArrow, dateRightArrow;

    private TextView userCaloriesRemaining, userFoodCalories, userWorkoutCalories, userDailyCaloriesIntake, userCarbs, userFat, userProtein;

    private RecyclerView userBreakfastFoodList, userLunchFoodList, userDinnerFoodList, userSnacksFoodList, userWorkoutList;

    private TextView waterGlasses;
    private LinearLayout warerGlassesContainer;

    private String currentUserID;

    private long diariesFoodCount, diariesWorkoutCount;

    private String intentFrom, intentUserID;

    private String currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        Intent intent = getIntent();
        intentFrom = intent.getExtras().getString("intentFrom");
        if(intentFrom.equals("ViewAnotherUserProfile"))
        {
            intentUserID = intent.getExtras().getString("intentUserID");
        }


        /* Adding tool bar & title to diary activity and hiding user image */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Diary");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbarUserImage = (CircleImageView) findViewById(R.id.toolbar_user_image);
        toolbarUserImage.setVisibility(View.GONE);


        /* getting current date */
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currenDate = new SimpleDateFormat("dd-MMM-yyyy");
        currentDate = currenDate.format(calendar.getTime());

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();

        foodDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Foods");
        foodDiaryDatabaseReference = FirebaseDatabase.getInstance().getReference().child("FoodDiaries");
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        waterDiaryDatabaseReference = FirebaseDatabase.getInstance().getReference().child("WaterDiaries");
        workoutDiaryDatabaseReference = FirebaseDatabase.getInstance().getReference().child("WorkoutDiaries");
        tdeeDiaryDatabaseReference = FirebaseDatabase.getInstance().getReference().child("TDEE");

        dateContainer = (LinearLayout) findViewById(R.id.diary_date_layout);
        diaryDate = (TextView) findViewById(R.id.diary_date);
        dateLeftArrow = (ImageView) findViewById(R.id.diary_date_left_arrow);
        dateRightArrow = (ImageView) findViewById(R.id.diary_date_right_arrow);

        userCaloriesRemaining = (TextView) findViewById(R.id.diary_caloriecard_calorieremaining);
        userDailyCaloriesIntake = (TextView) findViewById(R.id.diary_caloriecard_dailycalorieintake);
        userFoodCalories = (TextView) findViewById(R.id.diary_caloriecard_foodcalorie);
        userWorkoutCalories = (TextView) findViewById(R.id.diary_caloriecard_workoutcalories);


        userCarbs = (TextView) findViewById(R.id.diary_summary_card_carbs);
        userFat = (TextView) findViewById(R.id.diary_summary_card_fat);
        userProtein = (TextView) findViewById(R.id.diary_summary_card_protein);


        userBreakfastFoodList = (RecyclerView)findViewById(R.id.diary_breakfast_foodlist);
        userBreakfastFoodList.setNestedScrollingEnabled(false);

        userLunchFoodList = (RecyclerView)findViewById(R.id.diary_lunch_foodlist);
        userLunchFoodList.setNestedScrollingEnabled(false);

        userDinnerFoodList = (RecyclerView)findViewById(R.id.diary_dinner_foodlist);
        userDinnerFoodList.setNestedScrollingEnabled(false);

        userSnacksFoodList = (RecyclerView)findViewById(R.id.diary_snack_foodlist);
        userSnacksFoodList.setNestedScrollingEnabled(false);

        userWorkoutList = (RecyclerView)findViewById(R.id.diary_workout_list);
        userWorkoutList.setNestedScrollingEnabled(false);

        waterGlasses = (TextView) findViewById(R.id.diary_water_glasses);
        warerGlassesContainer = (LinearLayout) findViewById(R.id.diary_water_glasses_container);


        LinearLayoutManager breakfastLinearLayoutManager = new LinearLayoutManager(this);
        breakfastLinearLayoutManager.setReverseLayout(true);
        breakfastLinearLayoutManager.setStackFromEnd(true);
        userBreakfastFoodList.setLayoutManager(breakfastLinearLayoutManager);


        LinearLayoutManager lunchLinearLayoutManager = new LinearLayoutManager(this);
        lunchLinearLayoutManager.setReverseLayout(true);
        lunchLinearLayoutManager.setStackFromEnd(true);
        userLunchFoodList.setLayoutManager(lunchLinearLayoutManager);

        LinearLayoutManager dinnerLinearLayoutManager = new LinearLayoutManager(this);
        dinnerLinearLayoutManager.setReverseLayout(true);
        dinnerLinearLayoutManager.setStackFromEnd(true);
        userDinnerFoodList.setLayoutManager(dinnerLinearLayoutManager);

        LinearLayoutManager snacksLinearLayoutManager = new LinearLayoutManager(this);
        snacksLinearLayoutManager.setReverseLayout(true);
        snacksLinearLayoutManager.setStackFromEnd(true);
        userSnacksFoodList.setLayoutManager(snacksLinearLayoutManager);

        LinearLayoutManager workoutLinearLayoutManager = new LinearLayoutManager(this);
        workoutLinearLayoutManager.setReverseLayout(true);
        workoutLinearLayoutManager.setStackFromEnd(true);
        userWorkoutList.setLayoutManager(workoutLinearLayoutManager);


        diaryDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PopupCalendar();
            }
        });

        dateLeftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateDate(-1);
            }
        });

        dateRightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateDate(1);
            }
        });

        if(intentFrom.equals("ViewAnotherUserProfile") && !currentUserID.equals(intentUserID))
        {
            GetUserData(intentUserID, currentDate);
            DisplayUserFoods(intentUserID, currentDate, "breakfast", userBreakfastFoodList);
            DisplayUserFoods(intentUserID, currentDate, "lunch", userLunchFoodList);
            DisplayUserFoods(intentUserID, currentDate, "dinner", userDinnerFoodList);
            DisplayUserFoods(intentUserID, currentDate, "snack", userSnacksFoodList);
            DisplayUserAllWorkouts(intentUserID, currentDate);
            DisplayUserWaterGlasses(intentUserID, currentDate);
        }
        else
        {
            GetUserData(currentUserID, currentDate);
            DisplayUserFoods(currentUserID, currentDate, "breakfast", userBreakfastFoodList);
            DisplayUserFoods(currentUserID, currentDate, "lunch", userLunchFoodList);
            DisplayUserFoods(currentUserID, currentDate, "dinner", userDinnerFoodList);
            DisplayUserFoods(currentUserID, currentDate, "snack", userSnacksFoodList);
            DisplayUserAllWorkouts(currentUserID, currentDate);
            DisplayUserWaterGlasses(currentUserID, currentDate);
        }
    }

    private void PopupCalendar()
    {
        final Calendar myCalendar = Calendar.getInstance();

        Calendar today = Calendar.getInstance();

        SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
        final String todayDateStr = currentDateFormat.format(today.getTime());

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day)
            {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);

                /* date formats*/
                SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.US);
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.US);
                SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.US);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

                String diaryChangedDate;

                String selectedDate = dateFormat.format(myCalendar.getTime());

                if (selectedDate.equals(todayDateStr))
                {
                    diaryDate.setText("Today");
                }
                else
                {
                    diaryDate.setText(dayFormat.format(myCalendar.getTime()) + " " +
                            monthFormat.format(myCalendar.getTime()) + " " +
                            yearFormat.format(myCalendar.getTime()));
                }

                diaryChangedDate = selectedDate;


                if(intentFrom.equals("ViewAnotherUserProfile") && !currentUserID.equals(intentUserID))
                {
                    GetUserData(intentUserID, diaryChangedDate);
                    DisplayUserFoods(intentUserID, diaryChangedDate, "breakfast", userBreakfastFoodList);
                    DisplayUserFoods(intentUserID, diaryChangedDate, "lunch", userLunchFoodList);
                    DisplayUserFoods(intentUserID, diaryChangedDate, "dinner", userDinnerFoodList);
                    DisplayUserFoods(intentUserID, diaryChangedDate, "snack", userSnacksFoodList);
                    DisplayUserAllWorkouts(intentUserID, diaryChangedDate);
                    DisplayUserWaterGlasses(intentUserID, diaryChangedDate);
                }
                else
                {
                    GetUserData(currentUserID, diaryChangedDate);
                    DisplayUserFoods(currentUserID, diaryChangedDate, "breakfast", userBreakfastFoodList);
                    DisplayUserFoods(currentUserID, diaryChangedDate, "lunch", userLunchFoodList);
                    DisplayUserFoods(currentUserID, diaryChangedDate, "dinner", userDinnerFoodList);
                    DisplayUserFoods(currentUserID, diaryChangedDate, "snack", userSnacksFoodList);
                    DisplayUserAllWorkouts(currentUserID, diaryChangedDate);
                    DisplayUserWaterGlasses(currentUserID, diaryChangedDate);
                }
            }
        };

        /* set today date */
        new DatePickerDialog(this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),  myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void navigateDate(int daysToAdd) {
        try {
            Calendar today = Calendar.getInstance();
            SimpleDateFormat storageFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);
            String todayStorage = storageFormat.format(today.getTime());

            String currentDateText = diaryDate.getText().toString();
            Calendar calendar = Calendar.getInstance();

            if (currentDateText.equals("Today")) {

                calendar = Calendar.getInstance();
            } else {

                Date date = displayFormat.parse(currentDateText);
                calendar.setTime(date);
            }

            calendar.add(Calendar.DAY_OF_MONTH, daysToAdd);

            String storageDate = storageFormat.format(calendar.getTime());
            String displayDate;

            if (storageDate.equals(todayStorage)) {
                displayDate = "Today";
            } else {
                displayDate = displayFormat.format(calendar.getTime());
            }

            diaryDate.setText(displayDate);

            if(intentFrom.equals("ViewAnotherUserProfile") && !currentUserID.equals(intentUserID)) {
                GetUserData(intentUserID, storageDate);
                DisplayUserFoods(intentUserID, storageDate, "breakfast", userBreakfastFoodList);
                DisplayUserFoods(intentUserID, storageDate, "lunch", userLunchFoodList);
                DisplayUserFoods(intentUserID, storageDate, "dinner", userDinnerFoodList);
                DisplayUserFoods(intentUserID, storageDate, "snack", userSnacksFoodList);
                DisplayUserAllWorkouts(intentUserID, storageDate);
                DisplayUserWaterGlasses(intentUserID, storageDate);
            } else {
                GetUserData(currentUserID, storageDate);
                DisplayUserFoods(currentUserID, storageDate, "breakfast", userBreakfastFoodList);
                DisplayUserFoods(currentUserID, storageDate, "lunch", userLunchFoodList);
                DisplayUserFoods(currentUserID, storageDate, "dinner", userDinnerFoodList);
                DisplayUserFoods(currentUserID, storageDate, "snack", userSnacksFoodList);
                DisplayUserAllWorkouts(currentUserID, storageDate);
                DisplayUserWaterGlasses(currentUserID, storageDate);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void GetUserData(final String userID, final String date)
    {
        userDatabaseReference.child(userID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(intentFrom.equals("ViewAnotherUserProfile") && !currentUserID.equals(intentUserID))
                    {
                        if(dataSnapshot.hasChild("username"))
                        {
                            String retrieveUserName = dataSnapshot.child("username").getValue().toString();
                            if(!TextUtils.isEmpty(retrieveUserName))
                            {
                                /* getting first name of the user */
                                String arr[] = retrieveUserName.split(" ", 2);
                                getSupportActionBar().setTitle(arr[0]+"'s"+" Diary");
                            }
                        }
                    }


                    if(dataSnapshot.hasChild("usercurrentweight"))
                    {
                        strUserCurrentWeight = dataSnapshot.child("usercurrentweight").getValue().toString();
                    }


                    if(dataSnapshot.hasChild("userheight"))
                    {
                        strUserHeight = dataSnapshot.child("userheight").getValue().toString();
                    }

                    if(dataSnapshot.hasChild("userbyear"))
                    {
                        strUserByear = dataSnapshot.child("userbyear").getValue().toString();
                    }

                    if(dataSnapshot.hasChild("usergender"))
                    {
                        strUserGender = dataSnapshot.child("usergender").getValue().toString();
                    }

                    if(dataSnapshot.hasChild("useractivitylevel"))
                    {
                        strUserActivityLevel = dataSnapshot.child("useractivitylevel").getValue().toString();
                    }

                    GetUserFoodData(userID, date);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void GetUserFoodData(final String userID, final String date)
    {
        foodDiaryDatabaseReference.child(userID).child(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final long totalEntries = dataSnapshot.getChildrenCount();
                    final int[] processedCount = {0};

                    for (final DataSnapshot ds : dataSnapshot.getChildren()) {

                        if (ds.exists()) {
                            String foodID = ds.child("foodID").getValue(String.class);

                            final String servingSize;

                            if (ds.hasChild("numberOfServing")) {
                                servingSize = ds.child("numberOfServing").getValue().toString();
                            }
                            else
                            {
                                servingSize = "1";
                            }

                            if (!TextUtils.isEmpty(foodID)) {
                                foodDatabaseReference.child(foodID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot foodSnapshot) {
                                        if (foodSnapshot.exists()) {
                                            if (foodSnapshot.hasChild("foodcalories")) {
                                                String foodCalories = foodSnapshot.child("foodcalories").getValue().toString();
                                                double calories = Double.parseDouble(servingSize) * Double.parseDouble(foodCalories);
                                                strUserFood = String.format(Locale.US, "%.0f", Double.parseDouble(strUserFood) + calories);
                                            }

                                            if (foodSnapshot.hasChild("foodcarbs")) {
                                                String foodCarbs = foodSnapshot.child("foodcarbs").getValue().toString();
                                                double carbs = Double.parseDouble(servingSize) * Double.parseDouble(foodCarbs);
                                                strUserCarbs = String.format(Locale.US, "%.1f", Double.parseDouble(strUserCarbs) + carbs);
                                            }

                                            if (foodSnapshot.hasChild("foodprotein")) {
                                                String foodProtein = foodSnapshot.child("foodprotein").getValue().toString();
                                                double protein = Double.parseDouble(servingSize) * Double.parseDouble(foodProtein);
                                                strUserProtein = String.format(Locale.US, "%.1f", Double.parseDouble(strUserProtein) + protein);
                                            }

                                            if (foodSnapshot.hasChild("foodfat")) {
                                                String foodFat = foodSnapshot.child("foodfat").getValue().toString();
                                                double fat = Double.parseDouble(servingSize) * Double.parseDouble(foodFat);
                                                strUserFat = String.format(Locale.US, "%.1f", Double.parseDouble(strUserFat) + fat);
                                            }
                                        }
                                        processedCount[0]++;

                                        if (processedCount[0] == totalEntries) {
                                            GetUserWorkoutData(userID, date);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        processedCount[0]++;
                                        if (processedCount[0] == totalEntries) {
                                            GetUserWorkoutData(userID, date);
                                        }
                                    }
                                });
                            }
                            else {
                                processedCount[0]++;
                                if (processedCount[0] == totalEntries) {
                                    GetUserWorkoutData(userID, date);
                                }
                            }
                        }
                        else {
                            processedCount[0]++;
                            if (processedCount[0] == totalEntries) {
                                GetUserWorkoutData(userID, date);
                            }
                        }
                    }
                }
                else {
                    GetUserWorkoutData(userID, date);
                }
            }
                @Override
                public void onCancelled (@NonNull DatabaseError databaseError)
                {
                    GetUserWorkoutData(userID, date);
                }
        });
    }

    private void GetUserWorkoutData(final String userID, final String date)
    {
        workoutDiaryDatabaseReference.child(userID).child(date).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    /* counting the user workout */
                    diariesWorkoutCount = dataSnapshot.getChildrenCount();

                    for(final DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        /* getting workout type */
                        String workoutType = ds.getKey();

                        workoutDiaryDatabaseReference.child(userID).child(date).child(workoutType).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if(dataSnapshot.hasChild("calories"))
                                {
                                    String workoutCalories = dataSnapshot.child("calories").getValue().toString();
                                    strUserWorkout = String.valueOf(Integer.parseInt(strUserWorkout) + Integer.parseInt(workoutCalories));
                                }

                                diariesWorkoutCount = diariesWorkoutCount - 1;

                                /* checking whether  finished the calculation or not */
                                if(diariesWorkoutCount == 0)
                                {
                                    GetUserTDEEData(userID, date);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError)
                            {

                            }
                        });


                    }
                }
                else
                {
                    GetUserTDEEData(userID, date);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void GetUserTDEEData(String userID, String date)
    {
        tdeeDiaryDatabaseReference.child(userID).child(date)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                            strUserDailyCalorieIntake = snapshot.getValue().toString();
                        }
                        else
                        {
                            strUserDailyCalorieIntake = "2300";
                        }
                        SetSummaryCardsDetails();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }


    private void SetSummaryCardsDetails()
    {
        FormulaCalculations fc = new FormulaCalculations();

        userDailyCaloriesIntake.setText(strUserDailyCalorieIntake);

        userFoodCalories.setText(strUserFood);
        userWorkoutCalories.setText(strUserWorkout);
        userCarbs.setText(strUserCarbs+" g");
        userFat.setText(strUserFat+" g");
        userProtein.setText(strUserProtein+" g");

        strUserCaloriesRemaining = fc.CaloriesRemaining(strUserDailyCalorieIntake, strUserFood, strUserWorkout);
        userCaloriesRemaining.setText(strUserCaloriesRemaining);

        if(Integer.parseInt(strUserCaloriesRemaining) < 0)
        {
            userCaloriesRemaining.setTextColor(getResources().getColor(R.color.WarningTextColor));
        }

        strUserFood="0";
        strUserCarbs="0";
        strUserFat="0";
        strUserProtein="0";
        strUserWorkout="0";
    }

    private void DisplayUserFoods(final String userID, final String date, final String foodType, final RecyclerView recyclerView)
    {

        Query userFoodQuery = foodDiaryDatabaseReference.child(userID).child(date).orderByChild("foodType").equalTo(foodType);

        FirebaseRecyclerOptions<Foods> options =
                new FirebaseRecyclerOptions.Builder<Foods>()
                .setQuery(userFoodQuery, Foods.class)
                .build();

        FirebaseRecyclerAdapter<Foods, UserFoodsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Foods, UserFoodsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final UserFoodsViewHolder userFoodsViewHolder, int i, @NonNull Foods foodNutrition)
                    {
                        final String diaryEntryKey = getRef(i).getKey();
                        if(!TextUtils.isEmpty(diaryEntryKey))
                        {

                            foodDiaryDatabaseReference.child(userID).child(date).child(diaryEntryKey).addListenerForSingleValueEvent(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                {
                                    if(dataSnapshot.exists())
                                    {
                                        final String foodID = dataSnapshot.child("foodID").getValue(String.class);
                                        final String servings;

                                        if(dataSnapshot.hasChild("numberOfServing"))
                                        {
                                            servings = dataSnapshot.child("numberOfServing").getValue(String.class);
                                        }
                                        else
                                        {
                                            servings = "1";
                                        }

                                        foodDatabaseReference.child(foodID).addListenerForSingleValueEvent(new ValueEventListener()
                                        {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                            {
                                                if(dataSnapshot.exists())
                                                {
                                                    if(dataSnapshot.hasChild("foodname"))
                                                    {
                                                        String foodName = dataSnapshot.child("foodname").getValue(String.class);
                                                        userFoodsViewHolder.layoutfoodname.setText(foodName);
                                                    }
                                                    if(dataSnapshot.hasChild("foodcalories"))
                                                    {
                                                        String foodCalories = dataSnapshot.child("foodcalories").getValue(String.class);
                                                        foodCalories = String.format(Locale.US,"%.0f cal",(Double.parseDouble(servings) * Double.parseDouble(foodCalories)));
                                                        userFoodsViewHolder.layoutfoodcalorie.setText(foodCalories);
                                                    }

                                                    userFoodsViewHolder.foodItemContainer.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            Intent viewFoodIntent = new Intent(DiaryActivity.this, ViewFoodActivity.class);
                                                            viewFoodIntent.putExtra("diaryEntryKey", diaryEntryKey);
                                                            viewFoodIntent.putExtra("diaryDate", date);
                                                            viewFoodIntent.putExtra("foodType", foodType);

                                                            startActivityForResult(viewFoodIntent, 1);
                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError)
                                            {
                                            }
                                        });
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError)
                                {

                                }
                            });
                        }

                    }

                    @NonNull
                    @Override
                    public UserFoodsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_food_layout, parent, false);
                        return new UserFoodsViewHolder(view);
                    }
                };

        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    private void DisplayUserAllWorkouts(String userID, String date)
    {
        Query userWorkouts = workoutDiaryDatabaseReference.child(userID).child(date);

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Workouts>()
                        .setQuery(userWorkouts, Workouts.class)
                        .build();

        FirebaseRecyclerAdapter<Workouts, UserFoodsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Workouts, UserFoodsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserFoodsViewHolder userFoodsViewHolder, int i, @NonNull Workouts workouts)
            {
                final String workoutType = getRef(i).getKey();
                userFoodsViewHolder.layoutfoodname.setText(workoutType);
                userFoodsViewHolder.layoutfoodcalorie.setText(workouts.getCalories());
            }

            @NonNull
            @Override
            public UserFoodsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_food_layout, parent, false);
                UserFoodsViewHolder userFoodsViewHolder = new UserFoodsViewHolder(view);
                return userFoodsViewHolder;
            }
        };
        userWorkoutList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class UserFoodsViewHolder extends RecyclerView.ViewHolder
    {
        TextView layoutfoodname, layoutfoodcalorie;
        RelativeLayout foodItemContainer;

        public UserFoodsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            layoutfoodname = (TextView)itemView.findViewById(R.id.userfood_name);
            layoutfoodcalorie = (TextView)itemView.findViewById(R.id.userfood_calories);
            foodItemContainer = (RelativeLayout) itemView.findViewById(R.id.food_item_container);
        }
    }

    private void DisplayUserWaterGlasses(String userID, String date)
    {
        warerGlassesContainer.setVisibility(View.GONE);

        waterDiaryDatabaseReference.child(userID).child(date).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("glasses"))
                    {
                        if(!TextUtils.isEmpty(dataSnapshot.child("glasses").getValue().toString()) &&
                                (Integer.parseInt(dataSnapshot.child("glasses").getValue().toString()) > 0))
                        {
                            warerGlassesContainer.setVisibility(View.VISIBLE);
                            waterGlasses.setText(dataSnapshot.child("glasses").getValue().toString());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {

            String displayDate = diaryDate.getText().toString();
            String dateToUse;

            if (displayDate.equals("Today")) {

                dateToUse = currentDate;
            } else {
                dateToUse = displayDate;
            }

            if(intentFrom.equals("ViewAnotherUserProfile") && !currentUserID.equals(intentUserID)) {
                GetUserData(intentUserID, dateToUse);
            } else {
                GetUserData(currentUserID, dateToUse);
            }
        }
    }


    /* toolbar back button click action */
    @Override
    public boolean onSupportNavigateUp()
    {
        //onBackPressed();
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.putExtra("intentFrom", "home");
        startActivity(mainIntent);
        finish();
        return true;
    }
}
