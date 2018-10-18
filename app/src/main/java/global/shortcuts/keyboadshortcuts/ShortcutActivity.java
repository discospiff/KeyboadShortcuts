package global.shortcuts.keyboadshortcuts;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ShortcutActivity extends ShortcutBaseActivity {

    @BindView(R.id.edtShortcutName)
    EditText edtShortcutName;

    @BindView(R.id.actKeys)
    AutoCompleteTextView actKeys;

    @BindView(R.id.lblAllKeys)
    TextView lblAllKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shortcut);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @OnClick(R.id.btnAddKey)
    public void onBtnAddKeyClicked() {
        // Toast.makeText(getApplicationContext(), "We are here", Toast.LENGTH_LONG).show();
        String key = actKeys.getText().toString();
        String currentKeys = lblAllKeys.getText().toString();
        lblAllKeys.setText(currentKeys + key + " " );
        actKeys.setText("");
    }

    /**
     * Button click handler for save event.
     */
    @OnClick(R.id.btnSave)
    public void saveShortcut() {
        // TODO trim excess space.
    }

}
