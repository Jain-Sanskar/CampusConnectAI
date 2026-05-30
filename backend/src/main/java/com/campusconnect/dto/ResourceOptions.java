package com.campusconnect.dto;

import java.util.List;

/**
 * The set of values currently in use for category, subject and type.
 * The admin form uses these to build dropdowns; new values entered there
 * automatically appear here once a resource is saved with them.
 */
public class ResourceOptions {

    private List<String> categories;
    private List<String> subjects;
    private List<String> types;

    public ResourceOptions() {
    }

    public ResourceOptions(List<String> categories, List<String> subjects, List<String> types) {
        this.categories = categories;
        this.subjects = subjects;
        this.types = types;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }
}
