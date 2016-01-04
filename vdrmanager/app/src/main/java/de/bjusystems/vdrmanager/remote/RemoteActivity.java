package de.bjusystems.vdrmanager.remote;


import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.view.*;
import de.bjusystems.vdrmanager.gui.Utils;
import org.hampelratte.svdrp.Connection;
import org.hampelratte.svdrp.Response;
import org.hampelratte.svdrp.commands.HITK;

import de.androvdr.widget.AnimatedTextView;
import de.androvdr.widget.FontAwesome;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.gui.ColoredButton;
import de.bjusystems.vdrmanager.tasks.VoidAsyncTask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class RemoteActivity extends Activity implements OnClickListener, View.OnLongClickListener {


    private Connection connection;

    private AnimatedTextView result;
    private AlphaAnimation out;
    private AlphaAnimation in;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View view = getLayoutInflater().inflate(R.layout.remote, null);

        setContentView(view);

        view.setOnLongClickListener(this);


        ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.root);

        Button button = (Button) viewGroup.findViewById(R.id.red);
        //button.getBackground().setColorFilter(0xFF00FF00, PorterDuff.Mode.MULTIPLY);

        setAllButtonListener(viewGroup);

        result = (AnimatedTextView) findViewById(R.id.result);
        //Animation in = AnimationUtils.loadAnimation(this,android.R.anim.fade_in);
        //Animation out = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
/*      result.setInAnimation(in);
        result.setOutAnimation(out);
        result.setFactory(this);*/
        out = new AlphaAnimation(1.0f, 0.0f);
        out.setDuration(100);

        in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(100);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.remote_menu, menu);
        return true;
    }


    @Override
    public void onBackPressed() {
        if(isBackKeyRemapped() == false) {
            super.onBackPressed();
        } else {
            new HitkAsyncTask() {
            }.execute("Back");

        }
    }


    private void resetOverrides(){
        SharedPreferences sharedPref = getSharedPreferences("remote_" + Preferences.get().getCurrentVdr().getId(), Context.MODE_PRIVATE);
        sharedPref
                .edit()
                .clear()
                .commit();
        restart();
    }

    private void setBackKeyRemapped(boolean value){
       SharedPreferences sharedPref = getSharedPreferences("misc_" + Preferences.get().getCurrentVdr().getId(), Context.MODE_PRIVATE);
        sharedPref//
                .edit()//
                .putBoolean("backishitk", value)//
                .commit();
    }

    private boolean isBackKeyRemapped(){
       SharedPreferences sharedPref = getSharedPreferences("misc_" + Preferences.get().getCurrentVdr().getId(), Context.MODE_PRIVATE);
        return sharedPref.getBoolean("backishitk", false);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(isBackKeyRemapped() == false){
                return super.onKeyLongPress(keyCode, event);
            }
            super.onBackPressed();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.remapback);
        item.setChecked(isBackKeyRemapped());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.reset: {
                resetOverrides();
                return true;
            }
            case R.id.exprt: {
                Utils.say(this, R.string.not_yet_implemented);
                return true;
            }
            case R.id.imprt: {
                Utils.say(this, R.string.not_yet_implemented);
                return true;
            }
            case R.id.remapback: {
                if(item.isChecked()){
                    setBackKeyRemapped(false);
                } else {
                    setBackKeyRemapped(true);
                    Utils.say(this, R.string.remapback_hint);
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (connection != null) {
            new VoidAsyncTask() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        connection.close();
                        connection = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();

        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Preferences.init(this);
    }

    public void setAllButtonListener(ViewGroup viewGroup) {

        //
        SharedPreferences sharedPref = getSharedPreferences("remote_" + Preferences.get().getCurrentVdr().getId(), Context.MODE_PRIVATE);
        View v;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            v = viewGroup.getChildAt(i);
            if (v instanceof ViewGroup) {
                setAllButtonListener((ViewGroup) v);
            } else if (v instanceof Button) {
                if (v.getTag() == null) {
                    continue;
                }
                ((Button) v).setOnClickListener(this);
                ((Button) v).setOnLongClickListener(this);

                String hitk = sharedPref.getString("key_" + String.valueOf(v.getTag()), null);
                setOverrideTag(v, hitk);
                String label = sharedPref.getString("label_" + String.valueOf(v.getTag()), null);
                setOverrideLabel((Button)v, label);
            }
        }
    }

    private void setOverrideLabel(Button b, CharSequence label) {
        if(label == null){
            return;
        }
        b.setText(label);
    }


    private void restart() {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            recreate();
        } else {
            Intent intent = getIntent();
            overridePendingTransition(0, 0);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();

            overridePendingTransition(0, 0);
            startActivity(intent);
        }
    }


    class HitkAsyncTask extends  AsyncTask<String, Void, Void>{

        Response send;

        Exception ex;

        @Override
        protected void onPostExecute(Void aVoid) {
            result.setText("");
            result.fadeIn();
            if (send != null) {
                result.setText(String.valueOf(send.getMessage()));
                result.fadeOut();
                //Utils.say(getBaseContext(), send.toString());
            } else if (ex != null) {
                result.setText(ex.getMessage());
            }
        }

        @Override
        protected Void doInBackground(String... hitk) {
            try {

                if (connection == null) {
                    connection = new Connection(Preferences.get().getHost(), Preferences.get().getSvdrpPort());
                }

                send = connection.send(new HITK(hitk[0]));
            } catch (Exception ex) {
                this.ex = ex;
            }

            return null;
        }



    };

    @Override
    public void onClick(final View v) {
        new HitkAsyncTask() {
        }.execute(getCurrentTag(v));
    }

    @Override
    public boolean onLongClick(final View v) {


        if (v.getId() == R.id.root) {
            openOptionsMenu();
            return true;
        }

        CharSequence text = ((Button) v).getText();
        final String tag = (String) v.getTag();
        final String override = (String) v.getTag(-100);
        String current = override != null ? override : tag;

        View view = getLayoutInflater().inflate(R.layout.edit_remote_key, null);


        final EditText hitk = (EditText) view.findViewById(R.id.hitk);
        TextView hitkLabel = (TextView) view.findViewById(R.id.hitkLabel);
        if (v instanceof ColoredButton) {
            hitk.setVisibility(View.GONE);
            hitkLabel.setVisibility(View.GONE);
        } else {
            hitk.setVisibility(View.VISIBLE);
            hitkLabel.setVisibility(View.VISIBLE);
            hitk.setTypeface(
                    FontAwesome.getFontAwesome(this)
            );
        }
        hitk.setText(text);
        final Spinner hitkspinner = (Spinner) view.findViewById(R.id.hitkSpinner);

        final ArrayList<String> keys = new ArrayList<>();
        for (de.bjusystems.vdrmanager.remote.HITK hk : de.bjusystems.vdrmanager.remote.HITK.values()) {
            keys.add(hk.getValue());
        }
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, keys.toArray(new String[]{}));
// Specify the layout to use when the list of choices appears
        // hitkspinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        hitkspinner.setAdapter(adapter);
        int selected = -1;
        for (int i = 0; i < keys.size(); ++i) {
            String k = keys.get(i);
            if (k.equals(current)) {
                selected = i;
                break;
            }
        }
        hitkspinner.setSelection(selected);
        hitkspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CharSequence nhk = hitk.getText();
                ((Button) v).setText(nhk);
                String hitk = (String) hitkspinner.getSelectedItem();
                putVdrKey("key_" + tag, hitk);
                putVdrKey("label_" + tag, nhk);
                setOverrideTag(v, hitk);

            }
        })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setView(view);

        builder.create().show();


        return false;
    }

    private void putVdrKey(String key, CharSequence value) {
        SharedPreferences sharedPref = getSharedPreferences("remote_" + Preferences.get().getCurrentVdr().getId(), Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.putString(key, String.valueOf(value));
        edit.commit();
    }

    private String getCurrentTag(View view) {
        Object tag = view.getTag();
        if (tag instanceof String == false) {
            return null;
        }

        Object otag = view.getTag(-100);
        if (otag == null) {
            return (String) tag;
        }
        return (String) otag;
    }

    private void setOverrideTag(View view, String hitk) {
        view.setTag(-100, hitk);
    }
}
