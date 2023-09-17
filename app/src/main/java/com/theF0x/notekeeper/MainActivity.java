package com.theF0x.notekeeper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.theF0x.notekeeper.Adapters.NotesListAdapter;
import com.theF0x.notekeeper.Database.RoomDB;
import com.theF0x.notekeeper.Models.Notes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener
{

    RecyclerView recyclerView;
    NotesListAdapter notesListAdapter;
    List<Notes> notes = new ArrayList<>();
    RoomDB database;
    FloatingActionButton floatingButton;
    SearchView searchView_home;
    Notes selectedNote;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_home);
        floatingButton = findViewById(R.id.fab_add);
        searchView_home = findViewById(R.id.searchView_home);

        database = RoomDB.getInstance(this);
        notes = database.MainDAO().getAll();

        //Refresh
        updateRecycler(notes);

        floatingButton.setOnClickListener(view ->
        {
            //adding any request code you want
            //101 adding a note
            //102 editing a note
            startActivityForResult(new Intent(MainActivity.this, NotesTakerActivity.class), 101);

        });

        searchView_home.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String newText)
    {
        List<Notes> filteredList = new ArrayList<>();
        for (Notes singleNote : notes)
        {
            if (singleNote.getTitle().toLowerCase().contains(newText.toLowerCase()) ||
                singleNote.getNotes().toLowerCase().contains(newText.toLowerCase()))
            {
                filteredList.add(singleNote);
            }
        }
        notesListAdapter.filterList(filteredList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101)
        {
                if (resultCode == Activity.RESULT_OK)
                {
                    Notes new_notes = (Notes) data.getSerializableExtra("note");
                    database.MainDAO().insert(new_notes);
                    notes.clear();
                    notes.addAll(database.MainDAO().getAll());
                    notesListAdapter.notifyDataSetChanged();
                }
        }
        else if (requestCode == 102)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                Notes new_notes = (Notes) data.getSerializableExtra("note");
                database.MainDAO().update(new_notes.getId(), new_notes.getTitle(), new_notes.getNotes());
                notes.clear();
                notes.addAll(database.MainDAO().getAll());
                notesListAdapter.notifyDataSetChanged();
            }
        }
    }
    public int spanCount = 1; //How many columns do you need in the layout.
    private void updateRecycler(List<Notes> notes)
    {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, LinearLayoutManager.VERTICAL));
        notesListAdapter = new NotesListAdapter(MainActivity.this, notes, notesClickListener);
        recyclerView.setAdapter(notesListAdapter);
    }
    private final NotesClickListener notesClickListener = new NotesClickListener()
    {
        @Override
        public void onClick(Notes notes)
        {
            Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
            intent.putExtra("old_note", notes);
            startActivityForResult(intent, 102); //editing data request code 102
        }

        @Override
        public void onLongClick(Notes notes, CardView cardView)
        {
            selectedNote = new Notes();
            selectedNote = notes;
            showPopUp(cardView);
        }
    };

    private void showPopUp(CardView cardView)
    {
        PopupMenu popupMenu = new PopupMenu(this, cardView);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem)
    {
        switch (menuItem.getItemId())
        {
            case R.id.pin:
                  if (selectedNote.isPinned())
                  {
                    database.MainDAO().pin(selectedNote.getId(), false);
                    Toast.makeText(MainActivity.this, "Unpinned", Toast.LENGTH_SHORT).show();
                  }
                  else
                  {
                      database.MainDAO().pin(selectedNote.getId(), true);
                      Toast.makeText(MainActivity.this, "Pinned", Toast.LENGTH_SHORT).show();
                  }
                  notes.clear();
                  notes.addAll(database.MainDAO().getAll());
                  notesListAdapter.notifyDataSetChanged();
                  return true;
            case R.id.delete:
                database.MainDAO().delete(selectedNote);
                notes.remove(selectedNote);
                notesListAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Note Deleted!", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.changeColor:
                 notesListAdapter.color_code = notesListAdapter.getRandomColor();
                Toast.makeText(this, "Coming Soon!", Toast.LENGTH_SHORT).show();
            default:
                return false;
        }

    }
    //Option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.pinned:
            case R.id.about:
                Toast.makeText(this, "Coming Soon!", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.linear_layout:
                spanCount = 1;
                updateRecycler(notes);
                return true;
            case R.id.grid_layout:
                spanCount = 2;
                updateRecycler(notes);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}