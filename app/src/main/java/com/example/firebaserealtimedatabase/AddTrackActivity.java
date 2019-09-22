package com.example.firebaserealtimedatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddTrackActivity extends AppCompatActivity {

    private TextView tvArtistName;
    private EditText edtArtistTrack;
    private SeekBar seekRating;
    private Button btnAddTrack;

    private ListView listViewTracks;
    private List<Track> trackList;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_track);

        tvArtistName = findViewById(R.id.tv_artist_name);
        edtArtistTrack = findViewById(R.id.edt_track_name);
        seekRating = findViewById(R.id.seek_rating);
        btnAddTrack = findViewById(R.id.btn_add_track);

        listViewTracks = findViewById(R.id.listViewTrack);

        Intent intent = getIntent();

        String id = intent.getStringExtra(MainActivity.ARTIST_ID);
        String name = intent.getStringExtra(MainActivity.ARTIST_NAME);

        tvArtistName.setText(name);

        databaseReference = FirebaseDatabase.getInstance().getReference("tracks").child(id);

        trackList = new ArrayList<>();
        btnAddTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTrack();
            }
        });
    }

    private void saveTrack(){
        String trackName = edtArtistTrack.getText().toString().trim();
        int rating = seekRating.getProgress();

        if(!TextUtils.isEmpty(trackName)){
            String id = databaseReference.push().getKey();

            Track track = new Track(id, trackName, rating);
            databaseReference.child(id).setValue(track);
            Toast.makeText(this, "Track save successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Track name should not be empty", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                trackList.clear();

                for(DataSnapshot trackSnapshot: dataSnapshot.getChildren()){
                    Track track = trackSnapshot.getValue(Track.class);

                    trackList.add(track);

                }

                TrackList adapter = new TrackList(AddTrackActivity.this, trackList);
                listViewTracks.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
