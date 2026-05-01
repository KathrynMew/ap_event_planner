package com.example.project_4_event_planner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button goToLogin = findViewById(R.id.goToLoginButton);
        goToLogin.setOnClickListener(v -> {
            Intent login = new Intent(getApplicationContext(), Login.class);
            startActivity(login);
        });

        Button goToSignUp = findViewById(R.id.goToRegisterButton);
        goToSignUp.setOnClickListener(v -> {
            Intent signup = new Intent(getApplicationContext(), UserRegistration.class);
            startActivity(signup);
        });
    }
}