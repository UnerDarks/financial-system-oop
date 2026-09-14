package io.github.unerdarks.models;

import io.github.unerdarks.enums.CategoryType;

import java.util.UUID;

public class Category {
    private UUID id;
    private String name;
    private CategoryType type;
    private Category parent;

    private Category() {}

    public static Category create(String name, CategoryType type, Category parent) {
        Category category = new Category();

        category.id = UUID.randomUUID();
        category.name = name;
        category.type = type;
        category.parent = parent;

        category.validate();

        return category;
    }

    private void validate() {
        if (name == null || name.trim().isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        if (parent != null && parent.getType() != type) {
            throw new IllegalArgumentException("Subcategory type must match parent category type");
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CategoryType getType() {
        return type;
    }

    public Category getParent() {
        return parent;
    }
}
