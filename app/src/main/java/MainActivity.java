import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.android.UniPlugin.R;
import com.chenzhouli.doodle.doodleModule;


public class MainActivity extends AppCompatActivity {
    private String FilePathString = "/storage/emulated/0/Pictures/1657243983746.jpg";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button saveButton = findViewById(R.id.saveButton);
                        doodleModule doodleModule = new doodleModule();
        //保存当前图片到相册
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                doodleModule.drawPic(FilePathString);

            }
        });

        //监听getInfo按钮，点击获取图片路径
        Button getInfoButton = findViewById(R.id.getInfo);
        getInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 JSONObject path=doodleModule.getimgPath();
                System.out.println(path);
            }
        });




    }


}