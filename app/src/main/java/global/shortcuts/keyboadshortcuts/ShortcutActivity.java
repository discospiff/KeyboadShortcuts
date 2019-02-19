package global.shortcuts.keyboadshortcuts;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import global.shortcuts.keyboadshortcuts.dto.Shortcut;

public class ShortcutActivity extends ShortcutBaseActivity {

    public static final int READ_STORAGE_REQUEST_CODE = 1997;
    public static final String SHORTCUT_CHANNEL_ID = "SHORTCUT_CHANNEL_ID";
    @BindView(R.id.edtShortcutName)
    EditText edtShortcutName;

    @BindView(R.id.actKeys)
    AutoCompleteTextView actKeys;

    @BindView(R.id.lblAllKeys)
    TextView lblAllKeys;

    List<String> allKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shortcut);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        allKeys = new ArrayList<String>();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAndShowNotification();
            }
        });

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference reference = firebaseDatabase.getReference();
        reference.child("root").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                for(DataSnapshot child : children) {
                    Shortcut shortcut = child.getValue(Shortcut.class);
                    Toast.makeText(getApplicationContext(), "Shortcut: " + shortcut.getName() + " Keys: " + shortcut.getKeys(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // initialize our notification channel
        createNotificationChannel();
    }

   private void createNotificationChannel() {
        // define the channel
        String name = "Shortcuts";
        NotificationChannel channel = new NotificationChannel(SHORTCUT_CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("A channel for shortcuts");
        // create the channel on the system.
       NotificationManager notificationManager = getSystemService(NotificationManager.class);
       notificationManager.createNotificationChannel(channel);
   }

    private void createAndShowNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, SHORTCUT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Notification Title")
                .setContentText("Notification Text");

        // TODO do we have an image?
        ImageDecoder.Source source = ImageDecoder.createSource(getResources(), R.drawable.ic_add);
        try {
            Bitmap bitmap = ImageDecoder.decodeBitmap(source);
            // if we are here, there was not an exception thrown, so we have a bitmap.
            mBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));
        } catch (IOException e) {
            // if we can't get the bitmap, no problem!  Just use a normal notification.
            e.printStackTrace();
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, mBuilder.build());

    }

    @OnClick(R.id.btnAddKey)
    public void onBtnAddKeyClicked() {
        // Toast.makeText(getApplicationContext(), "We are here", Toast.LENGTH_LONG).show();
        String key = actKeys.getText().toString();
        allKeys.add(key);
        String currentKeys = lblAllKeys.getText().toString();
        lblAllKeys.setText(currentKeys + key + " " );
        actKeys.setText("");

    }

    @OnClick(R.id.btnOpenGallery)
    public void onBtnOpenGalleryClicked() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            String[] permissionRequest = {Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(permissionRequest, READ_STORAGE_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // are we hearing back from read storage?
        if(requestCode == READ_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // we are OK to invoke the gallery!
                openGallery();
            } else {
                // the permission was not granted.
                Toast.makeText(this, R.string.storage_permission, Toast.LENGTH_LONG).show();
            }
        }

    }

    private void openGallery() {
        // TODO add open gallery logic here.
        Toast.makeText(this, "Gallery Opened", Toast.LENGTH_LONG).show();
    }

    /**
     * Button click handler for save event.
     */
    @OnClick(R.id.btnSave)
    public void saveShortcut() {
        Shortcut shortcut = new Shortcut();
        shortcut.setName(edtShortcutName.getText().toString());
        shortcut.setKeys(allKeys);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference reference = firebaseDatabase.getReference();
        reference.child("root").push().setValue(shortcut);

        allKeys = new ArrayList<String>();
        lblAllKeys.setText("");

    }

}
