package org.worshipsongs.service;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.worshipsongs.CommonConstants;
import org.worshipsongs.activity.SplashScreenActivity;
import org.worshipsongs.dao.SongDao;
import org.worshipsongs.dialog.CustomDialogBuilder;
import org.worshipsongs.domain.DialogConfiguration;
import org.worshipsongs.worship.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * Author : Madasamy
 * Version : 3.x
 */

public class RemoteImportDatabaseService implements ImportDatabaseService
{
    private Map<String, Object> objects;
    private SongDao songDao;
    private AppCompatActivity appCompatActivity;
    private SharedPreferences sharedPreferences;

    @Override
    public void loadDb(AppCompatActivity appCompatActivity, Map<String, Object> objects)
    {
        this.appCompatActivity = appCompatActivity;
        songDao = new SongDao(appCompatActivity);
        this.objects = objects;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appCompatActivity);
        if (isWifiOrMobileDataConnectionExists()) {
            showRemoteUrlConfigurationDialog();
        } else {
            showNetWorkWarningDialog();
        }
    }

    @Override
    public String getName()
    {
        return RemoteImportDatabaseService.class.getSimpleName();
    }

    @Override
    public int getOrder()
    {
        return 0;
    }

    private boolean isWifiOrMobileDataConnectionExists()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) appCompatActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            if (networkInfo.isConnected()) {
                if ((networkInfo.getType() == ConnectivityManager.TYPE_WIFI) || (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void showRemoteUrlConfigurationDialog()
    {
        DialogConfiguration dialogConfiguration = new DialogConfiguration(appCompatActivity.getString(R.string.url), "");
        dialogConfiguration.setEditTextVisibility(true);
        CustomDialogBuilder customDialogBuilder = new CustomDialogBuilder(appCompatActivity, dialogConfiguration);
        final EditText editText = customDialogBuilder.getEditText();
        editText.setText(R.string.remoteUrl);
        AlertDialog.Builder builder = customDialogBuilder.getBuilder();
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String url = editText.getText().toString();
                new AsyncDownloadTask().execute(url);
                dialog.cancel();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });
        builder.show();

    }

    private void showNetWorkWarningDialog()
    {
        DialogConfiguration dialogConfiguration = new DialogConfiguration(appCompatActivity.getString(R.string.warning),
                appCompatActivity.getString(R.string.message_network_warning));
        CustomDialogBuilder customDialogBuilder = new CustomDialogBuilder(appCompatActivity, dialogConfiguration);
        customDialogBuilder.getBuilder().setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });
        customDialogBuilder.getBuilder().show();
    }

    private class AsyncDownloadTask extends AsyncTask<String, Void, Boolean>
    {
        private File destinationFile = null;
        private ProgressBar progressBar = (ProgressBar) objects.get(CommonConstants.PROGRESS_BAR_KEY);
        private TextView resultTextView = (TextView) objects.get(CommonConstants.TEXTVIEW_KEY);
        private Button revertDatabaseButton = (Button)objects.get(CommonConstants.REVERT_DATABASE_BUTTON_KEY);

        @Override
        protected void onPreExecute()
        {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... strings)
        {
            try {
                int count;
                destinationFile = new File(appCompatActivity.getCacheDir().getAbsolutePath(), CommonConstants.DATABASE_NAME);
                String remoteUrl = strings[0];
                URL url = new URL(remoteUrl);
                URLConnection conection = url.openConnection();
                conection.setReadTimeout(60000);
                conection.setConnectTimeout(60000);
                conection.connect();
                int lenghtOfFile = conection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream(), 10 * 1024);
                // Output stream to write file in SD card
                OutputStream output = new FileOutputStream(destinationFile);
                byte data[] = new byte[1024];
                while ((count = input.read(data)) != -1) {
                    // Write data to file
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
                return true;
            } catch (Exception ex) {
                Log.e(this.getClass().getSimpleName(), "Error", ex);
                return false;
            } finally {
                destinationFile.deleteOnExit();
            }
        }

        @Override
        protected void onPostExecute(Boolean successfull)
        {
            resultTextView.setText("");
            if (successfull) {
                Log.i(SplashScreenActivity.class.getSimpleName(), "Remote database copied successfully.");
                validateDatabase(destinationFile.getAbsolutePath(), resultTextView);
                revertDatabaseButton.setVisibility(View.VISIBLE);
                sharedPreferences.edit().putBoolean(CommonConstants.SHOW_REVERT_DATABASE_BUTTON_KEY, true).apply();
            }
            progressBar.setVisibility(View.GONE);
        }
    }

    private void validateDatabase(String absolutePath, TextView resultTextView)
    {
        try {
            songDao.close();
            songDao.copyDatabase(absolutePath, true);
            songDao.open();
            if (songDao.isValidDataBase()) {
                resultTextView.setText(getCountQueryResult());
            } else {
                showWarningDialog();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showWarningDialog()
    {
        DialogConfiguration dialogConfiguration = new DialogConfiguration(appCompatActivity.getString(R.string.warning),
                appCompatActivity.getString(R.string.message_database_invalid));
        CustomDialogBuilder customDialogBuilder = new CustomDialogBuilder(appCompatActivity, dialogConfiguration);
        customDialogBuilder.getBuilder().setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                try {
                    songDao.close();
                    songDao.copyDatabase("", true);
                    songDao.open();
                    dialog.cancel();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        customDialogBuilder.getBuilder().show();
    }

    public String getCountQueryResult()
    {
        String count = String.valueOf(songDao.count());
        return String.format(appCompatActivity.getString(R.string.songs_count) , count);
    }

}
