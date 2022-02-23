package com.example.messages;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.Distribution;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.OrderBy;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class Conversation extends AppCompatActivity {

    ArrayList<Message> msgs=new ArrayList<>();;
    ListView list;
    EditText et;
    FloatingActionButton b;
    FloatingActionButton add;

    boolean first=true;
    int y=0;
    int prevColor= 0;int prevColor2=0;
    ArrayList<Boolean> bools=new ArrayList<>();
    boolean sTime =false; int sLastPos=0;
    boolean rTime = false; int rLastPos=0;ConversationAdapter conversationAdapter;
    Uri imguri;String picturepath=""; boolean done=false;String selectedUser;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==111 && resultCode==RESULT_OK) {
            imguri = data.getData();
            done = true;
            FirebaseStorage storage=FirebaseStorage.getInstance();
            StorageReference storageReference=storage.getReference();
            final FirebaseFirestore db=FirebaseFirestore.getInstance();
            Log.d("IAS", picturepath+" "+done+"1");
            if (done) {
                done=false;
                Log.d("IAS", "REACHED");
                StorageReference imgref = storageReference.child("images/" + UUID.randomUUID().toString());
                imgref.putFile(imguri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        picturepath = taskSnapshot.getMetadata().getPath();
                        Log.d("IAS", picturepath);
                        msgs.add(new Message(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "", selectedUser, Timestamp.now()));
                        y = msgs.size();
                        msgs.get(y - 1).setPicturepath(picturepath);
                        Log.d("IAS", "hey"+msgs.get(y-1).getPicturepath());
                        db.collection(FirebaseAuth.getInstance().getCurrentUser().getEmail()).document(selectedUser).collection("Messages").add(msgs.get(y - 1)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                msgs.get(y - 1).setId(task.getResult().getId());
                                db.collection(FirebaseAuth.getInstance().getCurrentUser().getEmail()).document(selectedUser).collection("Messages").document(task.getResult().getId()).set(msgs.get(y - 1));
                            }
                        });
                        db.collection(selectedUser).document(FirebaseAuth.getInstance().getCurrentUser().getEmail()).collection("Messages").add(msgs.get(y - 1));
                        db.collection(selectedUser).document("picture").addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                                if (documentSnapshot.get("color") != null)
                                    prevColor2 = Math.toIntExact((long) (documentSnapshot.get("color")));
                            }
                        });
                        db.collection(FirebaseAuth.getInstance().getCurrentUser().getEmail()).document("picture").addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                                if (documentSnapshot.get("color") != null)
                                    prevColor = Math.toIntExact((long) (documentSnapshot.get("color")));
                            }
                        });
                        db.collection(FirebaseAuth.getInstance().getCurrentUser().getEmail()).document(selectedUser).set(new Contact(selectedUser, msgs.get(y - 1), msgs.get(y - 1).getTime(), prevColor));
                        db.collection(selectedUser).document(FirebaseAuth.getInstance().getCurrentUser().getEmail()).set(new Contact(FirebaseAuth.getInstance().getCurrentUser().getEmail(), msgs.get(y - 1), msgs.get(y - 1).getTime(), prevColor2));
                        conversationAdapter.notifyDataSetChanged();
                    }
                });

            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_conversation);
        et=findViewById(R.id.entertext);
        b=findViewById(R.id.floatingActionButton);
        add=findViewById(R.id.floatingActionButton2);
        selectedUser=getIntent().getStringExtra("selecteduser");
        try {
            this.getSupportActionBar().setTitle(selectedUser);
            //getSupportActionBar().setTitle(Html.fromHtml("<font color='#000'>"+selectedUser+"</font>",1));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch(Exception e){}
        String useremail=FirebaseAuth.getInstance().getCurrentUser().getEmail();
        list=findViewById(R.id.convo);
         conversationAdapter=new ConversationAdapter(this,R.layout.received_message,msgs);
        list.setAdapter(conversationAdapter);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();



        final Query q=db.collection(useremail).document(selectedUser).collection("Messages").orderBy("time", Query.Direction.ASCENDING);
        q.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                List<DocumentSnapshot> docs=queryDocumentSnapshots.getDocuments();

                if(first){
                    for(int x=0;x<docs.size();x++){
                        msgs.add(null);
                    }
                    first=false;
                }
                else{
                    for(int x=0;x<docs.size()-msgs.size();x++){
                        msgs.add(null);
                    }
                }
                boolean here=false;
                Log.d("TAG123","ASDASD" +docs.size());
                for(int x=0;x<docs.size();x++){
                    if(x<msgs.size()) {
                        Log.d("TAG123", "" + docs.get(x).getData());
                        msgs.set(x, docs.get(x).toObject(Message.class));
                        conversationAdapter.notifyDataSetChanged();
                    }

                }
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                if(msgs.get(i).getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Conversation.this);
                    builder.setTitle(msgs.get(i).getTime().toDate().toString());
                    String[] options = {"Delete"};
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {

                                    FirebaseFirestore.getInstance().collection(FirebaseAuth.getInstance().getCurrentUser().getEmail()).document(selectedUser).collection("Messages").document(msgs.get(i).getId()).delete();
                                    msgs.remove(i);

                            }
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                return true;
            }
        });
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String t=et.getText().toString();
                if(!t.equals("")) {
                    msgs.add(new Message(FirebaseAuth.getInstance().getCurrentUser().getEmail(), t, selectedUser,Timestamp.now()));
                    y = msgs.size();
                    db.collection(FirebaseAuth.getInstance().getCurrentUser().getEmail()).document(selectedUser).collection("Messages").add(msgs.get(y - 1)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    msgs.get(y-1).setId(task.getResult().getId());
                                    db.collection(FirebaseAuth.getInstance().getCurrentUser().getEmail()).document(selectedUser).collection("Messages").document(task.getResult().getId()).set(msgs.get(y-1));
                                }
                            });
                    db.collection(selectedUser).document(FirebaseAuth.getInstance().getCurrentUser().getEmail()).collection("Messages").add(msgs.get(y - 1));
                    db.collection(selectedUser).document("picture").addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                            if(documentSnapshot.get("color")!=null)
                                prevColor2=Math.toIntExact((long)(documentSnapshot.get("color")));
                        }
                    });
                    db.collection(FirebaseAuth.getInstance().getCurrentUser().getEmail()).document("picture").addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                            if(documentSnapshot.get("color")!=null)
                                prevColor=Math.toIntExact((long)(documentSnapshot.get("color")));
                        }
                    });
                    db.collection(FirebaseAuth.getInstance().getCurrentUser().getEmail()).document(selectedUser).set(new Contact(selectedUser,msgs.get(y-1),msgs.get(y-1).getTime(),prevColor));
                    db.collection(selectedUser).document(FirebaseAuth.getInstance().getCurrentUser().getEmail()).set(new Contact(FirebaseAuth.getInstance().getCurrentUser().getEmail(),msgs.get(y-1),msgs.get(y-1).getTime(), prevColor2));
                    conversationAdapter.notifyDataSetChanged();
                    et.setText("");
                    //InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    //inputMethodManager.hideSoftInputFromWindow(et.getWindowToken(), 0);

                }
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Choose picture"),111);

            }
        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(msgs.get(position).getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                    if(!sTime) {
                        sLastPos=position;
                        final LinearLayout sentlayout = view.findViewById(R.id.sentlay);
                        final TextView txt = new TextView(getApplicationContext());
                        txt.setText(msgs.get(position).getTime().toDate().toString().substring(0, 16));
                        txt.setId(View.generateViewId());
                        txt.setTextColor(Color.GRAY);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        txt.setLayoutParams(params);
                        (sentlayout).addView(txt);
                        sTime = true;
                        CountDownTimer countDownTimer = new CountDownTimer(3000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {

                            }

                            @Override
                            public void onFinish() {
                                sentlayout.removeView(txt);
                                sTime=false;
                            }
                        };
                        countDownTimer.start();
                    }else{

                    }
                }else{
                    if(!rTime ) {
                        rLastPos=position;
                        final LinearLayout receivedlayout = view.findViewById(R.id.veclay);
                        final TextView txt = new TextView(getApplicationContext());
                        txt.setText(msgs.get(position).getTime().toDate().toString().substring(0, 16));
                        txt.setId(View.generateViewId());
                        txt.setTextColor(Color.GRAY);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        txt.setLayoutParams(params);
                        (receivedlayout).addView(txt);
                        rTime=true;
                        CountDownTimer countDownTimer = new CountDownTimer(5000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {

                            }

                            @Override
                            public void onFinish() {
                                receivedlayout.removeView(txt);
                                rTime=false;
                            }
                        };
                        countDownTimer.start();
                    }
                }
            }
        });

    }

    public class ConversationAdapter extends ArrayAdapter<Message> {

        public Context context;
        public ArrayList<Message> messages=new ArrayList<>();
        public int res; 

        public ConversationAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Message> objects) {
            super(context, resource, objects);
            this.context=context;
            res=resource;
            messages=objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            View adapterLayout;


                if (msgs.get(position)!=null && FirebaseAuth.getInstance().getCurrentUser().getEmail().equals(msgs.get(position).getSender())) {
                    adapterLayout = layoutInflater.inflate(R.layout.sent_message, null);
                    TextView txt = adapterLayout.findViewById(R.id.senttext);
                    Typeface type = Typeface.createFromAsset(getAssets(), "fonts/Rubik-Regular.ttf");
                    //time.setText(msgs.get(position).getTime().toDate().toString().substring(0,16));
                    txt.setTypeface(type);
                    txt.setText(msgs.get(position).getMessage());
                    if(msgs.get(position).getPicturepath()!=null && !msgs.get(position).getPicturepath().equals("nopicture")){
                        ImageView imageView = new ImageView(Conversation.this);
                        LinearLayout linearLayout=adapterLayout.findViewById(R.id.sentlay);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        Log.d("TAG34",position+msgs.get(position).getId()+"\t"+msgs.get(position).getPicturepath());
                        StorageReference storageReference= FirebaseStorage.getInstance().getReference().child(msgs.get(position).getPicturepath());
                        //Log.d("TAG34",storageReference);
                        Glide.with(Conversation.this).load(storageReference).into(imageView);
                        imageView.setLayoutParams(params);
                        imageView.setBackgroundResource(R.drawable.sent_chatbubble);
                        linearLayout.addView(imageView);
                        linearLayout.removeView(txt);
                    }
                } else {
                    adapterLayout = layoutInflater.inflate(R.layout.received_message, null);
                    final ImageView img= adapterLayout.findViewById(R.id.proconv);
                    LinearLayout lay = adapterLayout.findViewById(R.id.veclay);
                    TextView name=adapterLayout.findViewById(R.id.namesender);
                    TextView txt = adapterLayout.findViewById(R.id.receievedtext);
                    Typeface type = Typeface.createFromAsset(getAssets(), "fonts/Rubik-Regular.ttf");
                    //time.setText(msgs.get(position).getTime().toDate().toString().substring(0,16));
                    txt.setTypeface(type);
                    name.setTypeface(type);
                    FirebaseFirestore.getInstance().collection(msgs.get(position).getSender()).document("picture").addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                            int selectedRes=-1;
                            if (documentSnapshot.get("option") != null)
                                selectedRes = Math.toIntExact((long) documentSnapshot.get("option"));
                            if (selectedRes == 0) {
                                img.setImageResource(R.drawable.a);
                            } else if (selectedRes == 1) {
                                img.setImageResource(R.drawable.b);
                            } else if (selectedRes == 2) {
                                img.setImageResource(R.drawable.c);
                            } else if (selectedRes == 3) {
                                img.setImageResource(R.drawable.d);
                            } else if (selectedRes == 4) {
                                img.setImageResource(R.drawable.e);
                            } else if (selectedRes == 5) {
                                img.setImageResource(R.drawable.f);
                            } else if (selectedRes == 6) {
                                img.setImageResource(R.drawable.g);
                            } else if (selectedRes == 7) {
                                img.setImageResource(R.drawable.h);
                            } else if (selectedRes == 8) {
                                img.setImageResource(R.drawable.i);
                            } else if (selectedRes == 9) {
                                img.setImageResource(R.drawable.j);
                            } else if (selectedRes == 10) {
                                img.setImageResource(R.drawable.k);
                            }
                        }
                    });
                    if(position>0 && msgs.get(position).getSender().equals(msgs.get(position-1).getSender())){
                        name.setVisibility(View.INVISIBLE);
                        img.setVisibility(View.INVISIBLE);
                        lay.removeView(name);
                        lay.removeView(img);
                    }
                    if(msgs.get(position)!=null) {
                        txt.setText(msgs.get(position).getMessage());
                        name.setText(msgs.get(position).getSender());

                    }
                    if(msgs.get(position).getPicturepath()!=null && !msgs.get(position).getPicturepath().equals("nopicture")){
                        ImageView imageView = new ImageView(Conversation.this);
                        LinearLayout linearLayout=adapterLayout.findViewById(R.id.veclay);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        Log.d("TAG34",position+msgs.get(position).getId()+"\t"+msgs.get(position).getPicturepath());
                        StorageReference storageReference= FirebaseStorage.getInstance().getReference().child(msgs.get(position).getPicturepath());
                        //Log.d("TAG34",storageReference);
                        Glide.with(Conversation.this).load(storageReference).into(imageView);
                        imageView.setLayoutParams(params);
                        imageView.setBackgroundResource(R.drawable.recieved_chatbubble);
                        linearLayout.addView(imageView);
                        linearLayout.removeView(txt);
                    }
                }


                return adapterLayout;
        }

    }
}
