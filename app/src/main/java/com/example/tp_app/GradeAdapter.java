package com.example.tp_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GradeAdapter extends ArrayAdapter<Grade> {
    private Context context;
    private List<Grade> notes;

    public GradeAdapter(Context context, List<Grade> notes) {
        super(context, R.layout.note_item, notes);
        this.context = context;
        this.notes = notes;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.note_item, parent, false);
        }
        Grade currentNote = notes.get(position);
        ImageView imageView = listItem.findViewById(R.id.note_icon);
        TextView labelView = listItem.findViewById(R.id.note_label);
        TextView scoreView = listItem.findViewById(R.id.note_score);
        if (currentNote.getScore() >= 12.0) {
            imageView.setImageResource(R.drawable.ic_like);
        } else {
            imageView.setImageResource(R.drawable.ic_dislike);
        }

        labelView.setText(currentNote.getLabel());
        scoreView.setText(String.format("%.1f", currentNote.getScore()));

        return listItem;
    }
}
