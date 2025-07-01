package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.karyawan.MenuActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginTabFragment extends Fragment {

    private EditText login_user, login_password;
    private Button login_button;
    private DatabaseReference mDatabase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_tab, container, false);

        // Inisialisasi Firebase dengan URL yang sesuai
        mDatabase = FirebaseDatabase.getInstance("https://db-convection-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        // Inisialisasi elemen UI
        login_user = view.findViewById(R.id.login_user);
        login_password = view.findViewById(R.id.login_password);
        login_button = view.findViewById(R.id.login_button);

        // Button Login Click Listener
        login_button.setOnClickListener(v -> {
            String username = login_user.getText().toString().trim();
            String password = login_password.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getActivity(), "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validasi user ke Firebase
            validateUser(username, password);
        });

        return view;
    }

    private void validateUser(String username, String password) {
        mDatabase.child("users").orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String dbPassword = userSnapshot.child("password").getValue(String.class);
                        String role = userSnapshot.child("role").getValue(String.class);
                        String userId = userSnapshot.getKey(); // Mendapatkan ID pengguna dari Firebase

                        if (dbPassword != null && dbPassword.equals(password)) {
                            // Simpan userId di SharedPreferences setelah login berhasil
                            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("userId", userId);
                            editor.apply();

                            // Cek role dan pindah ke halaman yang sesuai
                            if ("karyawan".equals(role)) {
                                Toast.makeText(getActivity(), "Login Sukses sebagai Karyawan", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getActivity(), com.example.myapplication.karyawan.MenuActivity.class);
                                startActivity(intent);
                                requireActivity().finish();
                            } else if ("admin".equals(role)) {
                                Toast.makeText(getActivity(), "Login Sukses sebagai Admin", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getActivity(), com.example.myapplication.MenuActivity.class);
                                startActivity(intent);
                                requireActivity().finish();
                            } else {
                                Toast.makeText(getActivity(), "Role tidak dikenali", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), "Password salah", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), "Email tidak ditemukan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Gagal mengambil data pengguna", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
