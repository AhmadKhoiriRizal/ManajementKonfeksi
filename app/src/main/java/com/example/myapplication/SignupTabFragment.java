package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;
import java.util.regex.Pattern;

public class SignupTabFragment extends Fragment {

    private EditText register_user, register_password, register_confpassword;
    private Button register_button;
    private DatabaseReference mDatabase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_tab, container, false);

        // Inisialisasi Firebase Database dengan URL yang sesuai
        mDatabase = FirebaseDatabase.getInstance("https://db-convection-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        // Pastikan akun admin dibuat (jika belum ada)
        createAdminAccount();

        // Inisialisasi elemen UI
        register_user = view.findViewById(R.id.register_user);
        register_password = view.findViewById(R.id.register_password);
        register_confpassword = view.findViewById(R.id.register_confpassword);
        register_button = view.findViewById(R.id.register_button);

        // Event klik tombol Register
        register_button.setOnClickListener(v -> {
            String username = register_user.getText().toString().trim();
            String password = register_password.getText().toString().trim();
            String confirmPassword = register_confpassword.getText().toString().trim();

            // Validasi input
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getActivity(), "Username dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidEmail(username)) {
                Toast.makeText(getActivity(), "Format email tidak valid", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getActivity(), "Password dan Konfirmasi Password tidak cocok", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cek apakah username sudah ada di database
            mDatabase.child("users").orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(getActivity(), "Email sudah terdaftar", Toast.LENGTH_SHORT).show();
                    } else {
                        // Jika belum ada, buat user baru
                        String userId = generateUniqueId();
                        User newUser = new User(username, password, "karyawan", userId);

                        mDatabase.child("users").child(userId).setValue(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getActivity(), "Registrasi Berhasil, Silakan Login", Toast.LENGTH_SHORT).show();

                                    // Pindah ke Tab Login setelah registrasi berhasil
                                    ViewPager2 viewPager = requireActivity().findViewById(R.id.view_pager);
                                    viewPager.setCurrentItem(0); // Berpindah ke tab pertama (Login)
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getActivity(), "Gagal menyimpan data pengguna", Toast.LENGTH_SHORT).show();
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Gagal memeriksa username", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return view;
    }

    // Fungsi untuk membuat ID unik
    private String generateUniqueId() {
        String chars = "0123456789";
        StringBuilder uniqueId = new StringBuilder("APSE");
        Random random = new Random();

        for (int i = 0; i < 4; i++) {
            int index = random.nextInt(chars.length());
            uniqueId.append(chars.charAt(index));
        }

        return uniqueId.toString();
    }

    // Fungsi untuk membuat akun admin jika belum ada
    private void createAdminAccount() {
        String adminEmail = "admin@gmail.com";
        mDatabase.child("users").orderByChild("username").equalTo(adminEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    String userId = "ADMIN123";
                    User adminUser = new User(adminEmail, "admin123", "admin", userId);
                    mDatabase.child("users").child(userId).setValue(adminUser)
                            .addOnSuccessListener(aVoid -> {
//                                    Toast.makeText(getActivity(), "Akun admin berhasil dibuat", Toast.LENGTH_SHORT).show())
//                            .addOnFailureListener(e ->
//                                    Toast.makeText(getActivity(), "Gagal membuat akun admin", Toast.LENGTH_SHORT).show());

                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Gagal memeriksa akun admin", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidEmail(String email) {
        return Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$").matcher(email).matches();
    }

    // Kelas model untuk user
    public static class User {
        public String username;
        public String password;
        public String role;
        public String userId;

        public User() {
        }

        public User(String username, String password, String role, String userId) {
            this.username = username;
            this.password = password;
            this.role = role;
            this.userId = userId;
        }
    }
}
