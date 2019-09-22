package com.example.firebaserealtimedatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String ARTIST_NAME = "artistname";
    public static final String ARTIST_ID = "artistid";

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

        listViewArtist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Artist artist = artistList.get(i);

                Intent intent = new Intent(getApplicationContext(), AddTrackActivity.class);

                intent.putExtra(ARTIST_ID, artist.getArtistId());
                intent.putExtra(ARTIST_NAME, artist.getArtistName());

                startActivity(intent);
            }
        });

        listViewArtist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Artist artist = artistList.get(i);

                showUpdateDialog(artist.getArtistId(), artist.getArtistName());

                return true;
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
    // Menampilkan Dialog
    private void showUpdateDialog(final String artistId, String artistName){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.update_dialog,null);

        dialogBuilder.setView(dialogView);

        final EditText edtNameUpdate = dialogView.findViewById(R.id.edt_name_update);
        final TextView tvNameUpdate = dialogView.findViewById(R.id.tv_name_update);
        final Button btnUpdate =  dialogView.findViewById(R.id.btn_update);
        final Button btnDelete = dialogView.findViewById(R.id.btn_delete);
        final Spinner spinUpdateGenre = dialogView.findViewById(R.id.spin_update_genre);

        dialogBuilder.setTitle("Updating Artist :" + artistName);

        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.show();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = edtNameUpdate.getText().toString().trim();
                String genre = spinUpdateGenre.getSelectedItem().toString();

                if(TextUtils.isEmpty(name)){
                    edtNameUpdate.setError("Name Required");
                    return;
                }
                updateArtist(artistId, name, genre);

                alertDialog.dismiss();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteArtist(artistId);
            }
        });

    }
    private void deleteArtist(String id){
        DatabaseReference dbArtist = FirebaseDatabase.getInstance().getReference("artist").child(id);
        DatabaseReference dbTracks = FirebaseDatabase.getInstance().getReference("tracks").child(id);

        dbArtist.removeValue();
        dbTracks.removeValue();
        Toast.makeText(this, "Artist deleted succesfully", Toast.LENGTH_SHORT).show();
    }

    private boolean updateArtist(String id, String name, String genre){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("artist").child(id);

        Artist artist = new Artist(id,name,genre);

        databaseReference.setValue(artist);
        Toast.makeText(this, "Artist updated Successfuly", Toast.LENGTH_SHORT).show();

        return true;
    }
}
