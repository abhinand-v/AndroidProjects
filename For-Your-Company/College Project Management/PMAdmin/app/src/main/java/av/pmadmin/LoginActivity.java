package av.pmadmin;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button lbtn = findViewById(R.id.btn_login);
        final EditText etx_pin = findViewById(R.id.etp);
        lbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etx_pin.getText().toString().equals("9898"))
                    startActivity(new Intent(LoginActivity.this,MainActivity.class));
                finish();
            }
        });
    }
}
