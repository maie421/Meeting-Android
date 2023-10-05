package com.example.meeting_android.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.meeting_android.R;
import com.example.meeting_android.databinding.ActivityTermsBinding;

public class TermsActivity extends AppCompatActivity {
    private ActivityTermsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_terms);

        binding.termsConditionsButton.setOnClickListener(v->{
            Intent intent = new Intent(this, TermsConditionsActivity.class);
            startActivity(intent);
        });

        binding.privacyButton.setOnClickListener(v->{
            Intent intent = new Intent(this, PrivacyActivity.class);
            startActivity(intent);
        });

        binding.allCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.termsConditionsCheckBox.setChecked(true);
                binding.privacyCheckBox.setChecked(true);
            } else {
                binding.termsConditionsCheckBox.setChecked(false);
                binding.privacyCheckBox.setChecked(false);
            }
        });

        binding.registerButton.setOnClickListener(v->{
            if (binding.termsConditionsCheckBox.isChecked() && binding.privacyCheckBox.isChecked()){
                Intent intent = new Intent(this, SignupActivity.class);
                startActivity(intent);
                finish();
            }else{
                Toast.makeText(this, "약관에 동의해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}