package de.bjusystems.vdrmanager.remote;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.hampelratte.svdrp.Connection;
import org.hampelratte.svdrp.Response;
import org.hampelratte.svdrp.commands.HITK;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.androvdr.widget.AnimatedTextView;
import de.androvdr.widget.FontAwesome;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.gui.ColoredButton;
import de.bjusystems.vdrmanager.gui.Utils;
import de.bjusystems.vdrmanager.tasks.VoidAsyncTask;

/**
 * The type Remote activity.
 */
public class RemoteActivity extends Activity implements OnClickListener, View.OnLongClickListener {

    private static final int TAG_KEY = -100;

    private Connection connection;

    private AnimatedTextView result;
    private AlphaAnimation out;
    private AlphaAnimation in;

    private View dummyContextMenuView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View view = getLayoutInflater().inflate(R.layout.remote, null);

        setContentView(view);

        dummyContextMenuView = view.findViewById(R.id.resultwrapper);
        registerForContextMenu(dummyContextMenuView);
        view.setOnLongClickListener(this);


        ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.remoteroot);

        //Button button = (Button) viewGroup.findViewById(R.id.red);
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

        //SearchableSpinner spinner2 = (SearchableSpinner) findViewById(R.id.search2);
        //spinner2.setList(getResources().getStringArray(R.array.font));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.remote_menu, menu);
        return true;
    }


    @Override
    public void onBackPressed() {
        if (isBackKeyRemapped() == false) {
            super.onBackPressed();
        } else {
            new HitkAsyncTask() {
            }.execute("Back");

        }
    }


    private void resetOverrides() {
        SharedPreferences sharedPref = getSharedPreferences("remote_" + Preferences.get().getCurrentVdr().getId(), Context.MODE_PRIVATE);
        sharedPref
                .edit()
                .clear()
                .commit();
        restart();
    }

    private boolean isBackKeyRemapped() {
        SharedPreferences sharedPref = getSharedPreferences("misc_" + Preferences.get().getCurrentVdr().getId(), Context.MODE_PRIVATE);
        return sharedPref.getBoolean("backishitk", false);
    }

    private void setBackKeyRemapped(boolean value) {
        SharedPreferences sharedPref = getSharedPreferences("misc_" + Preferences.get().getCurrentVdr().getId(), Context.MODE_PRIVATE);
        sharedPref//
                .edit()//
                .putBoolean("backishitk", value)//
                .commit();
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isBackKeyRemapped() == false) {
                return super.onKeyLongPress(keyCode, event);
            }
            super.onBackPressed();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }


    /**
     * Export.
     */
    public void export() {
        SharedPreferences sharedPref = getSharedPreferences("remote_" + Preferences.get().getCurrentVdr().getId(), Context.MODE_PRIVATE);
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.remoteroot);
        ArrayList<Pair<String, String>> list = new ArrayList<>();
        gather(viewGroup, list, sharedPref);

        JSONObject root = new JSONObject();
        try {
            for (Pair<String, String> stringStringPair : list) {
                root.put(stringStringPair.first, stringStringPair.second);
            }
        } catch (JSONException jse) {
            Utils.say(this, jse.getLocalizedMessage());
        }

        Intent i = new Intent(android.content.Intent.ACTION_SEND);
        i.setType("application/json");
        i.putExtra(android.content.Intent.EXTRA_TEXT, root.toString());
        startActivity(Intent.createChooser(i, getResources().getString(R.string.export_custom_key_mapping)));
    }

    /**
     * Gather.
     *
     * @param viewGroup  the view group
     * @param pair       the pair
     * @param sharedPref the shared pref
     */
    public void gather(ViewGroup viewGroup, ArrayList<Pair<String, String>> pair, SharedPreferences sharedPref) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View v = viewGroup.getChildAt(i);
            if (v instanceof ViewGroup) {
                gather((ViewGroup) v, pair, sharedPref);
            } else if (v instanceof Button) {
                if (v.getTag() == null) {
                    continue;
                }
                String hitk = sharedPref.getString("key_" + String.valueOf(v.getTag()), null);
                String label = sharedPref.getString("label_" + String.valueOf(v.getTag()), null);
                if (hitk == null && label == null) {
                    continue;
                }
                pair.add(Pair.create(hitk, label));
            }
        }
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

    /**
     * Sets all button listener.
     *
     * @param viewGroup the view group
     */
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
                setOverrideLabel((Button) v, label);
            }
        }
    }

    private void setOverrideLabel(Button b, CharSequence label) {
        if (label == null) {
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

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.reset: {
                resetOverrides();
                return true;
            }
            case R.id.exprt: {
                export();
                return true;
            }
            case R.id.imprt: {
                Utils.say(this, R.string.not_yet_implemented);
                return true;
            }
            case R.id.remapback: {
                if (item.isChecked()) {
                    setBackKeyRemapped(false);
                } else {
                    setBackKeyRemapped(true);
                    Utils.say(this, R.string.remapback_hint);
                }
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onClick(final View v) {
        new HitkAsyncTask() {
        }.execute(getCurrentTag(v));
    }

    ;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.remote_menu, menu);
        MenuItem item = menu.findItem(R.id.remapback);
    }

    @Override
    public boolean onLongClick(final View v) {

        if (v.getId() == R.id.remoteroot) {
            openContextMenu(dummyContextMenuView);
            return true;
        }

        CharSequence text = ((Button) v).getText();
        final String tag = (String) v.getTag();
        final String override = (String) v.getTag(TAG_KEY);
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

        Object otag = view.getTag(TAG_KEY);
        if (otag == null) {
            return (String) tag;
        }
        return (String) otag;
    }

    private void setOverrideTag(View view, String hitk) {
        view.setTag(TAG_KEY, hitk);
    }

    /**
     * The type Hitk async task.
     */
    class HitkAsyncTask extends AsyncTask<String, Void, Void> {

        /**
         * The Send.
         */
        Response send;

        /**
         * The Ex.
         */
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


    }
}
