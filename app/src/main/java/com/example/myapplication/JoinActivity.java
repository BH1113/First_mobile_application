package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class JoinActivity extends AppCompatActivity {

    EditText userId, userPass, repassWord;
    Button btnInsert, btnFalse;
    UserDB myDB;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join);
        setTitle("회원 가입");

        userId = (EditText) findViewById(R.id.userID);
        userPass = (EditText) findViewById(R.id.userPass);
        repassWord = (EditText) findViewById(R.id.repass);

        btnInsert = (Button) findViewById(R.id.insertBtn);
        btnFalse = (Button) findViewById(R.id.falseBtn);
        myDB = new UserDB(this);


        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String id = userId.getText().toString().trim();
                String pass = userPass.getText().toString().trim();
                String repass = repassWord.getText().toString().trim();

                if (id.equals("") || pass.equals("")){
                    Toast.makeText(getApplicationContext(), "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_LONG).show();
                }else {
                    if (pass.equals(repass)){
                        Boolean checkuser = myDB.checkUserName(id);
                        if (checkuser == false){
                            Boolean insert = myDB.insertData(id, pass);
                            if (insert == true){
                                Toast.makeText(getApplicationContext(), id + "님 회원가입을 축하드립니다", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                            }else {
                                Toast.makeText(getApplicationContext(), "회원가입 실패", Toast.LENGTH_LONG).show();
                            }
                        }else
                            Toast.makeText(getApplicationContext(), "아이디가 이미 존재합니다", Toast.LENGTH_LONG).show();
                    }else
                        Toast.makeText(getApplicationContext(),"비밀번호가 일치하지않습니다", Toast.LENGTH_LONG).show();
                }
            }
        });
        btnFalse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "취소했습니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
