package com.example.firebaserealtimedatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText edtName;
    private Button btnAdd;
    private Spinner spinGenres;

    DatabaseReference databaseArtist;

    private ListView listViewArtist;
    private List<Artist> artistList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseArtist = FirebaseDatabase.getInstance().getReference("artist");

        edtName = findViewById(R.id.edt_name);
        btnAdd = findViewById(R.id.btn_add);
        spinGenres = findViewById(R.id.spin_genres);

        listViewArtist = findViewById(R.id.listViewArtist);

        artistList = new ArrayList<>();
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addArtist();
            }
        });
    }

    private void addArtist(){
        String name = edtName.getText().toString().trim();
        String genre = spinGenres.getSelectedItem().toString();

        if(!TextUtils.isEmpty(name)){
            String id = databaseArtist.push().getKey();

            Artist artist = new Artist(id,name,genre);

            databaseArtist.child(id).setValue(artist);

            Toast.makeText(this, "Artist added", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "You should enter a name!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        databaseArtist.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                artistList.clear();

                for(DataSnapshot artistSnapshot: dataSnapshot.getChildren()){
                    Artist artist = artistSnapshot.getValue(Artist.class);

                    artistList.add(artist);

                }

                ArtistList adapter = new ArtistList(MainActivity.this, artistList);
                listViewArtist.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
