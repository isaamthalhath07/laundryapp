package com.isaam.laundrytally;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The fixed list of items, grouped the way they get sorted before a laundry run. */
final class Catalog {

    static final class Group {
        final String title;
        final List<Item> items;

        Group(String title, Item... items) {
            this.title = title;
            List<Item> list = new ArrayList<>();
            Collections.addAll(list, items);
            this.items = Collections.unmodifiableList(list);
        }
    }

    static final List<Group> GROUPS;
    static final List<Item> ALL;

    static {
        List<Group> groups = new ArrayList<>();
        groups.add(new Group("Upper wear",
                new Item("shirt", "Shirt", null),
                new Item("tshirt", "T-shirt", null),
                new Item("blazer", "Blazer", null),
                new Item("suit", "Suit", "Counted as 1 item per suit")));
        groups.add(new Group("Lower wear",
                new Item("pants", "Pants", null),
                new Item("jeans", "Jeans", null)));
        groups.add(new Group("Night wear",
                new Item("pyjama", "Pyjama", null)));
        groups.add(new Group("Innerwear",
                new Item("underwear", "Underwear", null),
                new Item("vest", "Vest", null),
                new Item("socks", "Socks", "Count each pair as 1")));
        groups.add(new Group("Bath & bedding",
                new Item("towel", "Towel", null),
                new Item("bedsheet", "Bedsheet", null),
                new Item("pillowcover", "Pillow cover", null),
                new Item("blanket", "Blanket", null)));

        GROUPS = Collections.unmodifiableList(groups);

        List<Item> all = new ArrayList<>();
        for (Group g : groups) {
            all.addAll(g.items);
        }
        ALL = Collections.unmodifiableList(all);
    }

    private Catalog() {
    }
}
