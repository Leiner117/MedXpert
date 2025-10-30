package com.tec.medxpert.ui.stepper.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tec.medxpert.R;
import com.tec.medxpert.ui.stepper.model.AccordionItem;

import java.util.List;

public class AccordionAdapter extends RecyclerView.Adapter<AccordionAdapter.AccordionViewHolder> {

    private final List<AccordionItem> items;

    public AccordionAdapter(List<AccordionItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public AccordionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_accordion, parent, false);
        return new AccordionViewHolder(view);
    }

    @NonNull
    @Override
    public void onBindViewHolder(@NonNull AccordionViewHolder holder, int position) {
        AccordionItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class AccordionViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView;

        AccordionViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
        }

        void bind(AccordionItem item) {
            titleTextView.setText(item.title);
            descriptionTextView.setText(item.description);
            descriptionTextView.setVisibility(item.isExpanded ? View.VISIBLE : View.GONE);

            titleTextView.setOnClickListener(v -> {
                item.isExpanded = !item.isExpanded;
                notifyItemChanged(getAdapterPosition());
            });
        }
    }
}
