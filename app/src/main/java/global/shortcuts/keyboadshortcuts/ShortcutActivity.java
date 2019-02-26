package global.shortcuts.keyboadshortcuts;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
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
    public static final int GALLERY_REQUEST_CODE = 30;
    public static final String VOTE_RECEIVER = "VOTE_RECEIVER";
    public static final String SHORTCUT_ID = "SHORTCUT_ID";
    public static final String VOTE_TYPE = "VOTE_TYPE";
    public static final int UP_VOTE = 0;
    public static final int DOWN_VOTE = 1;
    @BindView(R.id.edtShortcutName)
    EditText edtShortcutName;

    @BindView(R.id.actKeys)
    AutoCompleteTextView actKeys;

    @BindView(R.id.lblAllKeys)
    TextView lblAllKeys;

    List<String> allKeys;

    @BindView(R.id.imageView)
    ImageView imageView;

    Uri imageUri;
    
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

        // this is what we want the pending intent to do.
        Intent intent = new Intent(this, VoteReceiver.class);
        intent.setAction(VOTE_RECEIVER);
        intent.putExtra(SHORTCUT_ID, 0);
        intent.putExtra(VOTE_TYPE, UP_VOTE);

        // now, wrap our intent in a pending intent.
        PendingIntent votePendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 10, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // this is what we want the pending intent to do.
        Intent downIntent = new Intent(this, VoteReceiver.class);
        downIntent.setAction(VOTE_RECEIVER);
        downIntent.putExtra(SHORTCUT_ID, 0);
        downIntent.putExtra(VOTE_TYPE, DOWN_VOTE);

        // now, wrap our intent in a pending intent.
        PendingIntent voteDownPendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 20, downIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, SHORTCUT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Notification Title")
                .setContentText("Notification Text")
                .addAction(R.drawable.ic_up_vote, "Vote Up", votePendingIntent)
                .addAction(R.drawable.ic_down_vote, "Vote Down", voteDownPendingIntent);

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
        Intent intent = new Intent(Intent.ACTION_PICK);
        File filePictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        Uri data = Uri.parse(filePictureDirectory.getPath());
        String type = "image/*";
        intent.setDataAndType(data, type);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                imageUri = data.getData();
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageUri);
                try {
                    Bitmap bitmap = ImageDecoder.decodeBitmap(source);
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    // just don't show the image.
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * Button click handler for save event.
     */
    @OnClick(R.id.btnSave)
    public void saveShortcut() {
        if (imageUri != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();

            final StorageReference imageRef = storageReference.child("images/" + imageUri.getLastPathSegment());
            UploadTask uploadTask = imageRef.putFile(imageUri);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // TODO handle this error properly and/or inform the user
                    int i = 1 + 1;
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    StorageMetadata metadata = taskSnapshot.getMetadata();
                    Task<Uri> downloadUrlTask = imageRef.getDownloadUrl();
                    downloadUrlTask.addOnSuccessListener(new OnSuccessListener<Uri>() {

                        @Override
                        public void onSuccess(Uri uri) {
                            String link = uri.toString();
                        }
                    });
                }
            });
        }


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
