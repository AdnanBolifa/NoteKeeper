package com.theF0x.notekeeper;

import androidx.cardview.widget.CardView;

import com.theF0x.notekeeper.Models.Notes;

public interface NotesClickListener
{
    void onClick(Notes notes);
    void onLongClick(Notes notes, CardView cardView);
}
