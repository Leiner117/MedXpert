package com.tec.medxpert.ui.chat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tec.medxpert.R;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatListFragment extends Fragment {
    private ChatsListViewModel viewModel;
    private EditText searchView;
    private ImageView filterButton;
    private ChatListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ChatsListViewModel.class);

        RecyclerView chatRecyclerView = view.findViewById(R.id.chat_recycler_view);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        searchView = view.findViewById(R.id.searchEditText);
        filterButton = view.findViewById(R.id.filterButton);

        adapter = new ChatListAdapter(new ArrayList<>());
        chatRecyclerView.setAdapter(adapter);

        final String doctorUid = "7oqLYPG2ZxTXYxzdgqHVEqtGaSm1";
        final String doctorName = "Dr. José Pablo Badilla Peralta";

        adapter.setOnChatClickListener(patientChat -> {
            Bundle bundle = new Bundle();
            bundle.putString("doctorUid", doctorUid);
            bundle.putString("doctorName", doctorName);
            bundle.putString("patientUid", patientChat.getUserID());
            bundle.putString("patientName", patientChat.getName());
            ChatFragment chatFragment = new ChatFragment();
            chatFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, chatFragment)
            .addToBackStack(null)
            .commit();
        });

        setupListeners();
        observeViewModel();

        viewModel.loadPatients();
    }


    private void setupListeners() {
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        filterButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), filterButton, 0, 0, R.style.PopupMenuStyle);
            popupMenu.getMenuInflater().inflate(R.menu.filter_chats, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.filter_by_name) {
                    viewModel.setSelectedFilter("name");
                } else if (id == R.id.filter_by_id) {
                    viewModel.setSelectedFilter("idNumber");
                }
                return true;
            });
            popupMenu.show();
        });
    }

    private void observeViewModel() {
        viewModel.getFilteredPatients().observe(getViewLifecycleOwner(), patients -> {
            adapter.updatePatients(patients);
        });

        viewModel.getSelectedFilter().observe(getViewLifecycleOwner(), filter -> {
            if ("name".equals(filter)) {
                searchView.setHint(R.string.search_by_name);
            } else if ("idNumber".equals(filter)) {
                searchView.setHint(R.string.search_by_id);
            }
        });
    }
}
