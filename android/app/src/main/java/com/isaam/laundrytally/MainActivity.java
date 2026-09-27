package com.isaam.laundrytally;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS = "laundry_tally";
    private static final String KEY_NOTE = "note";
    private static final String COUNT_PREFIX = "count_";
    private static final int MAX_COUNT = 999;

    private final Map<String, Integer> counts = new HashMap<>();
    private final Map<String, TextView> countViews = new HashMap<>();
    private final Map<String, View> rowViews = new HashMap<>();

    private SharedPreferences prefs;
    private TextView totalView;
    private TextView totalLabel;
    private EditText noteView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        totalView = findViewById(R.id.total);
        totalLabel = findViewById(R.id.totalLabel);
        noteView = findViewById(R.id.note);

        TextView today = findViewById(R.id.today);
        today.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date()));

        loadCounts();
        buildList();
        updateTotal();

        noteView.setText(prefs.getString(KEY_NOTE, ""));
        noteView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                prefs.edit().putString(KEY_NOTE, s.toString()).apply();
            }
        });

        findViewById(R.id.share).setOnClickListener(v -> shareSummary());
        findViewById(R.id.reset).setOnClickListener(v -> confirmReset());
    }

    private void loadCounts() {
        for (Item item : Catalog.ALL) {
            int saved = prefs.getInt(COUNT_PREFIX + item.key, 0);
            counts.put(item.key, clamp(saved));
        }
    }

    private void buildList() {
        LinearLayout list = findViewById(R.id.list);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Catalog.Group group : Catalog.GROUPS) {
            TextView header = (TextView) inflater.inflate(R.layout.item_group_header, list, false);
            header.setText(group.title);
            list.addView(header);

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_card));
            card.setDividerDrawable(ContextCompat.getDrawable(this, R.drawable.divider_line));
            card.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            card.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            for (Item item : group.items) {
                card.addView(createRow(inflater, card, item));
            }
            list.addView(card);
        }
    }

    private View createRow(LayoutInflater inflater, ViewGroup parent, Item item) {
        View row = inflater.inflate(R.layout.item_row, parent, false);

        TextView name = row.findViewById(R.id.name);
        name.setText(item.name);

        TextView hint = row.findViewById(R.id.hint);
        if (TextUtils.isEmpty(item.hint)) {
            hint.setVisibility(View.GONE);
        } else {
            hint.setText(item.hint);
            hint.setVisibility(View.VISIBLE);
        }

        TextView count = row.findViewById(R.id.count);
        count.setContentDescription(item.name);
        count.setOnClickListener(v -> askForCount(item));

        Button minus = row.findViewById(R.id.minus);
        minus.setContentDescription(getString(R.string.remove_one) + " " + item.name);
        minus.setOnClickListener(v -> setCount(item.key, get(item.key) - 1));

        Button plus = row.findViewById(R.id.plus);
        plus.setContentDescription(getString(R.string.add_one) + " " + item.name);
        plus.setOnClickListener(v -> setCount(item.key, get(item.key) + 1));

        countViews.put(item.key, count);
        rowViews.put(item.key, row);
        paintRow(item.key);
        return row;
    }

    private int get(String key) {
        Integer value = counts.get(key);
        return value == null ? 0 : value;
    }

    private static int clamp(int value) {
        if (value < 0) {
            return 0;
        }
        return Math.min(value, MAX_COUNT);
    }

    private void setCount(String key, int value) {
        int next = clamp(value);
        counts.put(key, next);
        prefs.edit().putInt(COUNT_PREFIX + key, next).apply();
        paintRow(key);
        updateTotal();
    }

    private void paintRow(String key) {
        TextView count = countViews.get(key);
        if (count != null) {
            count.setText(String.valueOf(get(key)));
        }
        View row = rowViews.get(key);
        if (row != null) {
            row.setBackgroundColor(ContextCompat.getColor(
                    this, get(key) > 0 ? R.color.row_active : android.R.color.transparent));
        }
    }

    private void updateTotal() {
        int total = 0;
        for (Item item : Catalog.ALL) {
            total += get(item.key);
        }
        totalView.setText(String.valueOf(total));
        totalLabel.setText(total == 1 ? R.string.item_total : R.string.items_total);
    }

    private void askForCount(Item item) {
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_count, null);
        EditText input = content.findViewById(R.id.value);
        input.setText(String.valueOf(get(item.key)));
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(this)
                .setTitle(item.name)
                .setMessage(R.string.set_count)
                .setView(content)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    int value = 0;
                    try {
                        String text = input.getText().toString().trim();
                        if (!text.isEmpty()) {
                            value = Integer.parseInt(text);
                        }
                    } catch (NumberFormatException ignored) {
                        // Leave the count at zero when the text is not a number.
                    }
                    setCount(item.key, value);
                })
                .show();
    }

    private void confirmReset() {
        int total = 0;
        for (Item item : Catalog.ALL) {
            total += get(item.key);
        }
        if (total == 0 && noteView.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.nothing_to_reset, Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_title)
                .setMessage(R.string.clear_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.clear_confirm, (dialog, which) -> {
                    for (Item item : Catalog.ALL) {
                        setCount(item.key, 0);
                    }
                    noteView.setText("");
                    Toast.makeText(this, R.string.cleared, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private String summary() {
        StringBuilder sb = new StringBuilder();
        String label = noteView.getText().toString().trim();
        sb.append(getString(R.string.share_title));
        if (!label.isEmpty()) {
            sb.append(" - ").append(label);
        }
        sb.append('\n');
        sb.append(DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date()));
        sb.append("\n\n");

        int total = 0;
        for (Item item : Catalog.ALL) {
            int n = get(item.key);
            if (n > 0) {
                sb.append(item.name).append(": ").append(n).append('\n');
                total += n;
            }
        }
        if (total == 0) {
            sb.append("(no items counted yet)\n");
        }
        sb.append("\nTotal items: ").append(total);
        return sb.toString();
    }

    private void shareSummary() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_title));
        send.putExtra(Intent.EXTRA_TEXT, summary());
        startActivity(Intent.createChooser(send, getString(R.string.share)));
    }
}
