package com.tec.medxpert.ui.stepper.model;

public class AccordionItem {
    public final String title;
    public final String description;
    public boolean isExpanded = false;

    public AccordionItem(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
