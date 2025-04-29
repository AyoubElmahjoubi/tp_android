package com.example.tp_app;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Acceuil extends AppCompatActivity {

    private static final String HTTP_URL = "https://belatar.name/rest/profile.php?login=test&passwd=test&id=9998&notes=true";
    private static final String HTTP_FETCH_NOTES_URL = "https://belatar.name/rest/notes.php?login=test&passwd=test&student_id=";
    private static final String HTTP_IMAGES = "https://belatar.name/images/";
    private Etudiant etd;
    private List<Grade> notesList = new ArrayList<>();
    private List<Grade> locallyAddedGrades = new ArrayList<>(); 
    private GradeAdapter noteAdapter;
    private EditText txtNom, txtPrenom, txtClasse;
    private ActivityResultLauncher<Intent> addGradeLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        addGradeLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String matiere = result.getData().getStringExtra("matiere");
                    float score = result.getData().getFloatExtra("score", 0);
                    
                    Grade newGrade = new Grade(matiere, score);
                    locallyAddedGrades.add(newGrade);
                    
                    updateCombinedGradesList();
                    
                    Toast.makeText(this, "Note ajoutée: " + matiere + " - " + score, Toast.LENGTH_SHORT).show();
                }
            }
        );

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main);

            txtNom = findViewById(R.id.edit_nom);
            txtPrenom = findViewById(R.id.edit_prenom);
            txtClasse = findViewById(R.id.edit_classe);

            ListView listView = findViewById(R.id.notes_list);
            noteAdapter = new GradeAdapter(this, notesList);
            listView.setAdapter(noteAdapter);

            ImageButton callButton = findViewById(R.id.btn_call);
            callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    makePhoneCall();
                }
            });

            Button addGradeButton = findViewById(R.id.btn_add_grade);
            addGradeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAddGradeForm();
                }
            });

            txtNom.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus && etd != null) {
                    etd.setNom(txtNom.getText().toString());
                    fetchStudentNotes(etd.getId());
                }
            });

            txtPrenom.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus && etd != null) {
                    etd.setPrénom(txtPrenom.getText().toString());
                    fetchStudentNotes(etd.getId());
                }
            });

            txtClasse.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus && etd != null) {
                    etd.setClasse(txtClasse.getText().toString());
                    fetchStudentNotes(etd.getId());
                }
            });

        } else {
            setContentView(R.layout.activity_main);
            txtNom = findViewById(R.id.edit_nom);
            txtPrenom = findViewById(R.id.edit_prenom);
            txtClasse = findViewById(R.id.edit_classe);

            ImageButton callButton = findViewById(R.id.btn_call);
            callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    makePhoneCall();
                }
            });
        }

        Log.d("CycleVie", "onCreate() appelé");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("CycleVie", "onStart() appelé");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("CycleVie", "onResume() appelé");

        notesList.clear();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, HTTP_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(Acceuil.class.getSimpleName(), response.toString());
                        try {
                            etd = new Etudiant(
                                    response.getInt("id"),
                                    response.getString("nom"),
                                    response.getString("prenom"),
                                    response.getString("classe"),
                                    null,
                                    response.getString("phone")
                            );

                            VollySingleton.getInstance(getApplicationContext()).getImageLoader()
                                    .get(HTTP_IMAGES + response.getString("photo"), new ImageLoader.ImageListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.e(Acceuil.class.getSimpleName(), "Error loading image: " + error.getMessage());
                                        }

                                        @Override
                                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                                            etd.setPhoto(response.getBitmap());
                                            ImageView img = findViewById(R.id.imageProfile);
                                            img.setImageBitmap(etd.getPhoto());
                                        }
                                    });

                            txtNom.setText(etd.getNom());
                            txtPrenom.setText(etd.getPrénom());
                            txtClasse.setText(etd.getClasse());

                            int orientation = getResources().getConfiguration().orientation;
                            if (orientation == Configuration.ORIENTATION_LANDSCAPE && response.has("notes")) {
                                JSONArray notesArray = response.getJSONArray("notes");
                                processNotesArray(notesArray);
                            }

                        } catch (JSONException e) {
                            Log.e(Acceuil.class.getSimpleName(), "JSON parsing error: " + e.getMessage());
                            Toast.makeText(Acceuil.this, "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Acceuil.class.getSimpleName(), "Network error: " + error.getMessage());
                Toast.makeText(Acceuil.this, "Erreur de connexion au serveur", Toast.LENGTH_SHORT).show();
            }
        });

        VollySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void processNotesArray(JSONArray notesArray) throws JSONException {
        notesList.clear();

        for (int i = 0; i < notesArray.length(); i++) {
            JSONObject noteObj = notesArray.getJSONObject(i);
            String label = noteObj.getString("label");
            double score = noteObj.getDouble("score");

            notesList.add(new Grade(label, score));
        }

        updateCombinedGradesList();
    }
    
    private void updateCombinedGradesList() {
        List<Grade> combinedList = new ArrayList<>(notesList);
        combinedList.addAll(locallyAddedGrades);
        
        if (noteAdapter != null) {
            noteAdapter.clear();
            for (Grade grade : combinedList) {
                noteAdapter.add(grade);
            }
            noteAdapter.notifyDataSetChanged();
        }
    }

    private void fetchStudentNotes(int studentId) {
        String url = HTTP_FETCH_NOTES_URL + studentId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.has("notes")) {
                                JSONArray notesArray = response.getJSONArray("notes");
                                processNotesArray(notesArray);
                            }
                        } catch (JSONException e) {
                            Log.e(Acceuil.class.getSimpleName(), "JSON parsing error: " + e.getMessage());
                            Toast.makeText(Acceuil.this, "Erreur lors du chargement des notes", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Acceuil.class.getSimpleName(), "Network error: " + error.getMessage());
                Toast.makeText(Acceuil.this, "Erreur de connexion au serveur", Toast.LENGTH_SHORT).show();
            }
        });

        VollySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("CycleVie", "onPause() appelé");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("CycleVie", "onStop() appelé");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("CycleVie", "onDestroy() appelé");
    }

    public void OnClickHandler(View view) {
        if (etd != null) {
            etd.setNom(txtNom.getText().toString());
            etd.setPrénom(txtPrenom.getText().toString());
            etd.setClasse(txtClasse.getText().toString());

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                fetchStudentNotes(etd.getId());
            }

            Toast.makeText(this, "Données enregistrées", Toast.LENGTH_SHORT).show();
            Log.d("CycleVie", "Bouton Enregistrer cliqué");
        }
    }

    private void makePhoneCall() {
        if (etd != null && etd.getPhone() != null && !etd.getPhone().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + etd.getPhone()));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Numéro de téléphone non disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void openAddGradeForm() {
        if (etd != null) {
            Intent intent = new Intent(this, AddGradeActivity.class);
            intent.putExtra("student_id", etd.getId());
            addGradeLauncher.launch(intent);
        } else {
            Toast.makeText(this, "Aucun étudiant sélectionné", Toast.LENGTH_SHORT).show();
        }
    }
}