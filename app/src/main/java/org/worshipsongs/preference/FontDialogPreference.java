package org.worshipsongs.preference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.worshipsongs.CommonConstants;
import org.worshipsongs.activity.FontTabActivity;
import org.worshipsongs.activity.FontTabBarActivity;
import org.worshipsongs.activity.FontTabFragment;
import org.worshipsongs.utils.PropertyUtils;
import org.worshipsongs.worship.R;

/**
 * Author:Madasamy
 * version:1.0.0
 */
public class FontDialogPreference extends Preference
{
    FragmentManager fragmentManager;
    private Activity activity;

    public FontDialogPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setPersistent(true);
    }

    @Override
    protected void onClick()
    {
        super.onClick();
        LayoutInflater layoutInflater = LayoutInflater.from(FontDialogPreference.this.getContext());
        SharedPreferences customSharedPreference = PreferenceManager.getDefaultSharedPreferences(FontDialogPreference.this.getContext());
        View promptsView = layoutInflater.inflate(R.layout.font_size_tab, null);
        SeekBar fontSizeSeekBar = (SeekBar) promptsView.findViewById(R.id.portrait_font_size);
        final int fontSize = customSharedPreference.getInt("fontSize", 18);
        fontSizeSeekBar.setProgress(fontSize);
        fontSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                saveFontSizePreference("fontSize", progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

        SeekBar landScapeSeekbar = (SeekBar) promptsView.findViewById(R.id.landscape_font_size);
        final int landScapeFontSize = customSharedPreference.getInt("landScapeFontSize", 20);
        landScapeSeekbar.setProgress(landScapeFontSize);
        landScapeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                saveFontSizePreference("landScapeFontSize", progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });
        Button doneButton = (Button) promptsView.findViewById(R.id.doneButton);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FontDialogPreference.this.getContext());
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setCancelable(false);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        doneButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                alertDialog.dismiss();
            }
        });


    }

    private void saveFontSizePreference(String key, int fontSize)
    {
        SharedPreferences fontSizePreference = PreferenceManager.getDefaultSharedPreferences(FontDialogPreference.this.getContext());
        SharedPreferences.Editor editor = fontSizePreference.edit();
        editor.putInt(key, fontSize);
        editor.commit();
    }
}
