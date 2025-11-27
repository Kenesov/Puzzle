package vetro.uz.puzzle;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final int SIZE = 4;
    private Button[][] buttons = new Button[SIZE][SIZE];
    private CoordinateData emptyCoordinate = new CoordinateData(3,3);
    private List<Integer> values = new ArrayList<>(15);
    private int count = 0;
    private TextView textCount;
    private ImageView btnRestart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout containerBtn = findViewById(R.id.containerBtn);
        textCount = findViewById(R.id.txtcount);
        btnRestart = findViewById(R.id.btn_restart);

        btnRestart.setOnClickListener(v -> loadData());

        for (int i=0; i<containerBtn.getChildCount(); i++){
            Button currentButton = (Button) containerBtn.getChildAt(i);
            currentButton.setOnClickListener(this::onClick);
            CoordinateData currentCoordinate = new CoordinateData(i/SIZE, i%SIZE);
            currentButton.setTag(currentCoordinate);
            buttons[i/SIZE][i%SIZE] = currentButton;
        }
        generateNumbers();
        loadData();

    }

    public void generateNumbers(){
        for (int i=1; i<16; i++){
            values.add(i);
        }
    }

    public void loadData(){

        do {
            Collections.shuffle(values);
        } while (!isSolvable(values));

        for (int i=0; i<15; i++){
            buttons[i/4][i%4].setText(String.valueOf(values.get(i)));
        }

        buttons[3][3].setText("");
        emptyCoordinate.setX(3);
        emptyCoordinate.setY(3);

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
    private int findEmptyRowFromBottom(int[] arr){
        int emptyIndex = 15;
        int row = emptyIndex / 4;
        return 4 - row;
    }

    private boolean isSolvable(List<Integer> list) {

        int[] arr = new int[16];
        for (int i = 0; i < 15; i++) {
            arr[i] = list.get(i);
        }
        arr[15] = 0;

        int invCount = getInvCount(arr);
        int emptyRow = findEmptyRowFromBottom(arr);

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
}