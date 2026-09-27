package com.isaam.laundrytally;

/** One countable laundry item. */
final class Item {

    final String key;
    final String name;
    final String hint;

    Item(String key, String name, String hint) {
        this.key = key;
        this.name = name;
        this.hint = hint;
    }
}
