package vetro.uz.puzzle;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private int SIZE;
    private Button[][] buttons;
    private CoordinateData emptyCoordinate;
    private List<Integer> values = new ArrayList<>();
    private int count = 0;
    private TextView textCount;
    private ImageView btnRestart;
    private SharedPreferences prefs;
    private LinearLayout containerBtn;

    private Button btn3x3;
    private Button btn4x4;
    private Button btn5x5;




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

        prefs = getSharedPreferences("puzzle_game", MODE_PRIVATE);

        loadView();

        btnRestart.setOnClickListener(v -> {
            clearGameState();
            loadData();
        });




    }

    private void loadView(){
        btn3x3 = findViewById(R.id.btn_3x3);
        btn4x4 = findViewById(R.id.btn_4x4);
        btn5x5 = findViewById(R.id.btn_5x5);
        containerBtn = findViewById(R.id.containerBtn);
        textCount = findViewById(R.id.txtcount);
        btnRestart = findViewById(R.id.btn_restart);

        btn3x3.setOnClickListener(v -> startGame(3));
        btn4x4.setOnClickListener(v -> startGame(4));
        btn5x5.setOnClickListener(v -> startGame(5));

        int savedSize = prefs.getInt("game_size", 4);
        startGame(savedSize);
    }
    private void startGame(int size) {
        int oldSize = prefs.getInt("game_size", 4);
        if (oldSize != size) {
            clearGameState();
        }

        SIZE = size;
        buttons = new Button[SIZE][SIZE];
        emptyCoordinate = new CoordinateData(SIZE - 1, SIZE - 1);

        prefs.edit().putInt("game_size", SIZE).apply();

        createButtons();
        generateNumbers();

        if (!loadGameState()) {
            loadData();
        }
    }

    private void createButtons() {
        containerBtn.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        int containerWidth = containerBtn.getLayoutParams().width;
        int containerHeight = containerBtn.getLayoutParams().height;
        float density = getResources().getDisplayMetrics().density;

        int containerSizePx;
        if (containerWidth > 0) {
            containerSizePx = Math.min(containerWidth, containerHeight);
        } else {
            containerSizePx = (int) (360 * density);
        }

        int paddingPx = (int) (12 * density);
        int availableSize = containerSizePx - paddingPx;
        int buttonSize = availableSize / SIZE;

        for (int i = 0; i < SIZE; i++) {
            LinearLayout row = (LinearLayout) inflater.inflate(R.layout.item_layout, containerBtn, false);

            for (int j = 0; j < SIZE; j++) {
                Button btn = (Button) inflater.inflate(R.layout.item_button_number, row, false);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(buttonSize, buttonSize);
                params.setMargins(2, 2, 2, 2);
                btn.setLayoutParams(params);
                btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28 - SIZE * 3);
                btn.setOnClickListener(this::onClick);

                CoordinateData coordinate = new CoordinateData(i, j);
                btn.setTag(coordinate);
                buttons[i][j] = btn;

                row.addView(btn);
            }
            containerBtn.addView(row);
        }
    }
    private void saveGameState() {
        SharedPreferences.Editor editor = prefs.edit();

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                editor.putString("btn_" + i + "_" + j, buttons[i][j].getText().toString());
            }
        }

        editor.putInt("empty_x", emptyCoordinate.getX());
        editor.putInt("empty_y", emptyCoordinate.getY());

        editor.putInt("move_count", count);

        editor.putBoolean("has_data", true);
        editor.apply();
    }

    private boolean loadGameState() {

        if (!prefs.getBoolean("has_data", false))
            return false;

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                String text = prefs.getString("btn_" + i + "_" + j, "");
                buttons[i][j].setText(text);
            }
        }

        int ex = prefs.getInt("empty_x", 3);
        int ey = prefs.getInt("empty_y", 3);
        emptyCoordinate.setX(ex);
        emptyCoordinate.setY(ey);

        // Count
        count = prefs.getInt("move_count", 0);
        textCount.setText(String.valueOf(count));

        return true;
    }
    private void clearGameState() {
        prefs.edit().clear().apply();
    }



    public void generateNumbers(){
        values.clear();
        int totalCells = SIZE * SIZE - 1;
        for (int i = 1; i <= totalCells; i++){
            values.add(i);
        }
    }

    public void loadData(){
        do {
            Collections.shuffle(values);
        } while (!isSolvable(values));

        int totalCells = SIZE * SIZE - 1;
        for (int i = 0; i < totalCells; i++){
            buttons[i / SIZE][i % SIZE].setText(String.valueOf(values.get(i)));
        }

        buttons[SIZE - 1][SIZE - 1].setText("");
        emptyCoordinate.setX(SIZE - 1);
        emptyCoordinate.setY(SIZE - 1);

        count = 0;
        textCount.setText("0");
    }


    public void onClick(View view){
        Button currentButton = (Button) view;
        CoordinateData currentCoordinate = (CoordinateData) view.getTag();
        int difX = Math.abs(currentCoordinate.getX() - emptyCoordinate.getX());
        int difY = Math.abs(currentCoordinate.getY() - emptyCoordinate.getY());
        if (difY+difX == 1){
            Button emptyButton = buttons[emptyCoordinate.getX()][emptyCoordinate.getY()];
            emptyButton.setText(currentButton.getText());
            currentButton.setText("");
            emptyCoordinate = currentCoordinate;
            count++;
            textCount.setText(String.valueOf(count));
            saveGameState();
            if (checkWin()){
                Toast.makeText(this,"Tabriklayman, siz yuttingiz!",Toast.LENGTH_LONG).show();
            }

        }

    }
    private int getInvCount(int[] arr){
        int inv_count = 0;
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[i] != 0 && arr[j] != 0 && arr[i] > arr[j]) {
                    inv_count++;
                }
            }
        }
        return inv_count;
    }

    private int findEmptyRowFromBottom(){
        int emptyIndex = SIZE * SIZE - 1;
        int row = emptyIndex / SIZE;
        return SIZE - row;
    }

    private boolean isSolvable(List<Integer> list) {
        int totalCells = SIZE * SIZE;
        int[] arr = new int[totalCells];

        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        arr[totalCells - 1] = 0;

        int invCount = getInvCount(arr);

        if (SIZE % 2 == 1) {
            return invCount % 2 == 0;
        }

        int emptyRow = findEmptyRowFromBottom();
        if (emptyRow % 2 == 1) {
            return invCount % 2 == 0;
        } else {
            return invCount % 2 == 1;
        }
    }


    private boolean checkWin(){
        int count = 1;
        for (int i=0; i<SIZE; i++){
            for (int j=0; j<SIZE; j++){
                String text = buttons[i][j].getText().toString();

                if (i == SIZE-1 && j == SIZE -1){
                    return text.equals("");
                }

                if (!text.equals(String.valueOf(count))) return false;
                count++;
            }
        }
        return true;
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                outState.putString("btn_" + i + "_" + j, buttons[i][j].getText().toString());
            }
        }

        outState.putInt("empty_x", emptyCoordinate.getX());
        outState.putInt("empty_y", emptyCoordinate.getY());
        outState.putInt("move_count", count);
    }


    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                buttons[i][j].setText(savedInstanceState.getString("btn_" + i + "_" + j));
            }
        }
        emptyCoordinate.setX(savedInstanceState.getInt("empty_x"));
        emptyCoordinate.setY(savedInstanceState.getInt("empty_y"));
        count = savedInstanceState.getInt("move_count");
        textCount.setText(String.valueOf(count));
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveGameState();
    }


}