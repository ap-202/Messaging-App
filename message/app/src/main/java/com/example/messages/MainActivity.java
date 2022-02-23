package com.example.messages;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.messages.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.data.model.Resource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FloatingActionButton add;
    FirebaseUser currentUser;
    boolean loggedin=false;
    boolean first=true;
    String newContact="";
    int requestedColor;
    ArrayList<Contact> contacts=new ArrayList<>();
    int selectedRes=0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "Signed-In", Toast.LENGTH_SHORT).show();
                loggedin = true;
                recreate();
            }
        }
        if (requestCode == 12) {
            if (resultCode == RESULT_OK) {
                try{

                newContact=data.getStringExtra("return");
                FirebaseFirestore.getInstance().collection(FirebaseAuth.getInstance().getCurrentUser().getEmail()).document(newContact).set(new Contact(newContact));
                }catch (Exception e){
                    String[] arr = data.getStringArrayExtra("returnarr");
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.main,menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.sign_out){
            AuthUI.getInstance().signOut(MainActivity.this).addOnCompleteListener(new OnCompleteListener<Void>() {
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(MainActivity.this,"Signed-OUT", Toast.LENGTH_SHORT).show();
                    loggedin=false;
                    startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setTheme(R.style.firetheme).build(),1);
                }
            });
        }
        if(item.getItemId() == R.id.color){

            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("Select a picture");
            String[] pictures = {"Baguette","Tomato","Strawberry","Raspberry","Pineapple","Pancakes","Orange","Cookie","Ice Cream","Hamburger","Cherry"};
            builder.setItems(pictures, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FirebaseFirestore.getInstance().collection(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                            .document("picture").set(new Picture(which));
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        }
        return true;
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().setTitle(Html.fromHtml("<font color='#000'>Messages </font>",1));

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //TextView t = new TextView(this);
//        t.setText("Messages");
//        t.setTextColor(Color.BLACK);
//        t.setTextSize(18);
//        getSupportActionBar().setCustomView(t);
        setContentView(R.layout.activity_main);
        add=findViewById(R.id.floatingActionButton3);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null && !loggedin) {
            try {
                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setTheme(R.style.firetheme).build(),1);
            } catch (Exception e) {
                Log.d("SIGNIN",e.toString()+"\n");
            }

        }else{

            final String str="";

            FirebaseFirestore db=FirebaseFirestore.getInstance();



            ListView listView=findViewById(R.id.list);
            final ContactAdapter contactAdapter=new ContactAdapter(this,R.layout.list_elements,contacts);
            listView.setAdapter(contactAdapter);
            contactAdapter.notifyDataSetChanged();

            final Query q=db.collection(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            q.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                    List<DocumentSnapshot> docs=queryDocumentSnapshots.getDocuments();
                    boolean here=false;
//                    Log.d("TAG123","ASDASD" +docs.size());
//                    if(docs.size()>contacts.size()){
//                        for(int x=0;x<docs.size()-contacts.size()-1;x++){
//                            Log.d("TAG1235",docs.get(x).getId());
//                            if(!docs.get(x).getId().equals("picture"))
//                            contacts.add(null);
//                        }
//                    }
                    if(first){
                        for(int x=0;x<docs.size();x++){
                            if(!docs.get(x).getId().equals("picture"))
                                contacts.add(null);
                        }
                        first=false;
                    }
                    else{
                        for(int x=0;x<docs.size()-contacts.size();x++){
                            if(!docs.get(x).getId().equals("picture"))
                                contacts.add(null);
                        }
                    }
                    Log.d("TAG123","ASDASD" +docs.size());
                    for(int x=0;x<docs.size();x++){
                        Log.d("TAG123",""+docs.get(x).getData());
                        if(!docs.get(x).getId().equals("picture")) {

                            contacts.set(x, docs.get(x).toObject(Contact.class));
                            contactAdapter.notifyDataSetChanged();
                        }

                    }

                    //Log.d("TSV",contacts.get(1).getColor()+"\t" + contacts.get(0).getColor());


                }
            });

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(contacts.get(i).getName());
                    String[] options = {"Delete"};
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(which==0){
                                FirebaseFirestore.getInstance().collection(FirebaseAuth.getInstance().getCurrentUser().getEmail()).document(contacts.get(i).getName()).delete();
                                contacts.remove(i);
                                contactAdapter.notifyDataSetChanged();
                            }
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return true;
                }
            });
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mAuth=FirebaseAuth.getInstance();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    Intent intent = new Intent(MainActivity.this,Conversation.class);
                    intent.putExtra("selecteduser",contacts.get(position).getName());
                    startActivity(intent);
                    contactAdapter.notifyDataSetChanged();
                }
            });
           add.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Intent intent2 = new Intent(MainActivity.this,NewContact.class);
                   startActivityForResult(intent2,12);

               }

           });

        }


    }

    @Override
    protected void onStart() {
        super.onStart();

    }
    public class ContactAdapter extends ArrayAdapter<Contact> {

        public Context context;
        public ArrayList<Contact> contacts=new ArrayList<>();
        public int res;

        public ContactAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Contact> objects) {
            super(context, resource, objects);
            this.context=context;
            res=resource;
            contacts=objects;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater=(LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            View adapterLayout;

                adapterLayout=layoutInflater.inflate(R.layout.list_elements,null);
            if(contacts.get(position)!=null) {
                    TextView msg = adapterLayout.findViewById(R.id.msg_text);
                    TextView user = adapterLayout.findViewById(R.id.msg_user);
                    TextView time = adapterLayout.findViewById(R.id.msg_time);

                    final ImageView img = adapterLayout.findViewById(R.id.imageView6);

                        FirebaseFirestore.getInstance().collection(contacts.get(position).getName()).document("picture").addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                                if (documentSnapshot.get("option") != null)
                                    selectedRes = Math.toIntExact((long) documentSnapshot.get("option"));
                                Log.d("TAGFG",contacts.get(position).getName()+" "+selectedRes);
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



                        Typeface type = Typeface.createFromAsset(getAssets(), "fonts/Rubik-Regular.ttf");
                        msg.setTypeface(type);
                        user.setTypeface(type);
                        time.setTypeface(type);
                        user.setText(contacts.get(position).getName());
                        Log.d("TAG", contacts.size() + "");
                        if (contacts.get(position).getLastmsg() != null) {
                            if (contacts.get(position).getLastmsg().getMessage().length() > 15)
                                msg.setText(contacts.get(position).getLastmsg().getMessage().substring(0, 20)+"...");
                            else
                                msg.setText(contacts.get(position).getLastmsg().getMessage());
                            time.setText(contacts.get(position).getLastmsg().getTime().toDate().toString().substring(0, 16));
                        } else {
                            msg.setText("No Messages");
                            time.setText("");
                        }
                    }

            return adapterLayout;
        }

    }
}

