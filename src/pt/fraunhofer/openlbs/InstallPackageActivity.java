package pt.fraunhofer.openlbs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import pt.fraunhofer.openlbs.aux.Constants;
import pt.fraunhofer.openlbs.aux.INETTools;
import pt.fraunhofer.openlbs.db.DBAdapter;
import pt.fraunhofer.openlbs.entities.Package;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class InstallPackageActivity extends Activity {
	private static final String TAG = "InstallPackageActivity";
	private static final int DIALOG_SHOW_PACKAGE = 0;
	private static final int DIALOG_DOWNLOAD_PROGRESS = 1;
	private static final int DIALOG_NO_INTERNET = 2;
	private static final int DIALOG_UNZIP_PROGRESS = 3;

	private static final String DOWNLOAD_PROGRESS_NOTIFICATION = "download_notification";
	private static final String DOWNLOAD_SIZE_NOTIFICATION = "download_size";
	private static final String DOWNLOAD_FAILED = "download_failed";
	private static final String DOWNLOAD_FINISHED = "download_finished";
	private static final String DOWNLOAD_INFORMATION_TYPE = "download_information_type";

	private static final String UNZIP_INFORMATION_TYPE = "unzip_information_type";
	private static final String UNZIP_COMPLETION_NOTIFICATION = "unzip_completion_notification";
	private static final String UNDATA_COMPLETION_NOTIFICATION = "undata_completion_notification";

	private static final String CONTENT_FILE_URL = Constants.OPENLBS_BASE_URL + "/packages/#";

	private static final int PROGRESS_DIALOG_REFRESH_RATE = 1024;
	private static final int BUFFER_SIZE = 1024;

	private Package thisPackage;
	private String thisPackageContentURL;
	private ProgressDialog downloadDialog;
	private ProgressDialog unzipDialog;
	private File tempFile;

	private Runnable fetchContent = new Runnable() {
		
		private boolean terminate = false;

		public void run() {
			Message msg = downloadHandler.obtainMessage();

			try {
				// set the download URL, a url that points to a file on the
				// internet
				// this is the file to be downloaded
				URL url = new URL(thisPackageContentURL);

				// create the new connection
				HttpURLConnection urlConnection = (HttpURLConnection) url
						.openConnection();

				// set up some things on the connection
				urlConnection.setRequestMethod("GET");
				urlConnection.setDoOutput(true);

				// and connect!
				urlConnection.connect();

				// File SDCardRoot = Environment.getExternalStorageDirectory();
				File tmpdir = new File(Constants.APP_TMP_DATA_FOLDER);

				if (!tmpdir.isDirectory())
					tmpdir.mkdirs();

				tempFile = new File(tmpdir, thisPackage.getContent_file_name());

				if (tempFile.isFile())
					tempFile.delete();

				// this will be used to write the downloaded data into the file
				// we created
				FileOutputStream fileOutput = new FileOutputStream(tempFile);

				// this will be used in reading the data from the internet
				InputStream inputStream = urlConnection.getInputStream();

				// this is the total size of the file
				int totalSize = urlConnection.getContentLength();

				Bundle b = new Bundle();
				b.putString(DOWNLOAD_INFORMATION_TYPE,
						DOWNLOAD_SIZE_NOTIFICATION);
				b.putInt(DOWNLOAD_SIZE_NOTIFICATION, totalSize);
				msg.setData(b);
				downloadHandler.sendMessage(msg);

				// variable to store total downloaded bytes
				int downloadedSize = 0;

				// create a buffer...
				byte[] buffer = new byte[BUFFER_SIZE];
				int bufferLength = 0;
				int refreshTicker = 0;

				while ((bufferLength = inputStream.read(buffer)) > 0) {
					
					if(terminate)
						return;
					
					// add the data in the buffer to the file in the file output
					// stream (the file on the sd card
					fileOutput.write(buffer, 0, bufferLength);
					// add up the size so we know how much is downloaded
					downloadedSize += bufferLength;

					refreshTicker++;

					if (refreshTicker == PROGRESS_DIALOG_REFRESH_RATE) {
						// this is where you would do something to report the
						// prgress, like this maybe
						Bundle bn = new Bundle();
						Message msgn = downloadHandler.obtainMessage();
						bn.putString(DOWNLOAD_INFORMATION_TYPE,
								DOWNLOAD_PROGRESS_NOTIFICATION);
						bn.putInt(DOWNLOAD_PROGRESS_NOTIFICATION,
								downloadedSize);
						msgn.setData(bn);
						downloadHandler.sendMessage(msgn);

						refreshTicker = 0;

						Log.d(TAG, "Current progress: " + downloadedSize);
					}
				}

				Bundle bn = new Bundle();
				Message msgo = downloadHandler.obtainMessage();
				bn.putString(DOWNLOAD_INFORMATION_TYPE, DOWNLOAD_FINISHED);
				msgo.setData(bn);
				downloadHandler.sendMessage(msgo);

				// close the output stream when done
				fileOutput.close();

				// catch some possible errors...
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				Message msgf = downloadHandler.obtainMessage();
				Bundle bf = new Bundle();
				bf.putString(DOWNLOAD_INFORMATION_TYPE, DOWNLOAD_FAILED);
				msgf.setData(bf);
				downloadHandler.sendMessage(msgf);
			}

		}
		
	};

	private Runnable unzipContents = new Runnable() {

		public void run() {

			File oldContentDirectory = new File(
					Constants.APP_CONTENT_DATA_FOLDER + File.separator
							+ thisPackage.getName());

			if (oldContentDirectory.exists()) {
				Log.d(TAG, "Found old content: "
						+ oldContentDirectory.toString() + ". Deleting!");
				deleteDir(oldContentDirectory);
			}

			try {
				BufferedOutputStream dest = null;
				BufferedInputStream is = null;
				ZipEntry entry;
				ZipFile zipfile = new ZipFile(tempFile);
				Enumeration e = zipfile.entries();
				while (e.hasMoreElements()) {
					entry = (ZipEntry) e.nextElement();
					Log.d(TAG, "Extracting: " + entry);

					String fileName = entry.getName();
					File newFile = new File(Constants.APP_CONTENT_DATA_FOLDER
							+ File.separator + fileName);

					// create directories as needed
					new File(newFile.getParent()).mkdirs();

					is = new BufferedInputStream(zipfile.getInputStream(entry));
					int count;
					byte data[] = new byte[BUFFER_SIZE];
					FileOutputStream fos = new FileOutputStream(newFile);
					dest = new BufferedOutputStream(fos, BUFFER_SIZE);

					while ((count = is.read(data, 0, BUFFER_SIZE)) != -1) {
						dest.write(data, 0, count);
					}

					dest.flush();
					dest.close();
					is.close();
				}
				Log.d(TAG, "Deleting temporary file " + tempFile.toString());
				tempFile.delete();

				Log.d(TAG, "And we are done with the unzipping.");

				Message msgf = unholyHandler.obtainMessage();
				Bundle bf = new Bundle();
				bf.putString(UNZIP_INFORMATION_TYPE,
						UNZIP_COMPLETION_NOTIFICATION);
				msgf.setData(bf);
				unholyHandler.sendMessage(msgf);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		public boolean deleteDir(File dir) {
			if (dir.isDirectory()) {
				String[] children = dir.list();

				for (int i = 0; i < children.length; i++) {
					boolean success = deleteDir(new File(dir, children[i]));

					if (!success) {
						return false;
					}
				}

			}
			// The directory is now empty so delete it

			Log.w(TAG, "Deleting: " + dir.toString());
			return dir.delete();
		}

	};

	private Runnable updateDatabase = new Runnable() {

		public void run() {
			DBAdapter mDb = new DBAdapter(getBaseContext());
			mDb.open();

			// find if the same package is already installed and delete it
			findAndDelete(mDb);

			// install this package in the database
			addToDatabase(mDb);
			
			mDb.close();
			
			Message msgf = unholyHandler.obtainMessage();
			Bundle bf = new Bundle();
			bf.putString(UNZIP_INFORMATION_TYPE,
					UNDATA_COMPLETION_NOTIFICATION);
			msgf.setData(bf);
			unholyHandler.sendMessage(msgf);
		}

		private void findAndDelete(DBAdapter mDb) {
			int package_id = mDb.exists(thisPackage);
			if (package_id > 0)
				thisPackage.setId(package_id);
				mDb.recursiveDelete(thisPackage);
		}

		private void addToDatabase(DBAdapter mDb) {
			mDb.addPackage(thisPackage);
		}
	};

	final Handler downloadHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			String messageType = msg.getData().getString(
					DOWNLOAD_INFORMATION_TYPE);

			if (messageType.equals(DOWNLOAD_SIZE_NOTIFICATION)) {
				downloadDialog.setMax(msg.getData().getInt(
						DOWNLOAD_SIZE_NOTIFICATION));
			}

			if (messageType.equals(DOWNLOAD_PROGRESS_NOTIFICATION)) {
				downloadDialog.setProgress(msg.getData().getInt(
						DOWNLOAD_PROGRESS_NOTIFICATION));
			}

			if (messageType.equals(DOWNLOAD_FINISHED)) {
				downloadDialog.dismiss();
				showDialog(DIALOG_UNZIP_PROGRESS);

				Thread unzipThread = new Thread(null, unzipContents,
						"ContentUnzip");
				unzipThread.start();
			}

			if (messageType.equals(DOWNLOAD_FAILED)) {

			}
		}

	};

	final Handler unholyHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			String messageType = msg.getData()
					.getString(UNZIP_INFORMATION_TYPE);

			if (messageType.equals(UNZIP_COMPLETION_NOTIFICATION)) {
				unzipDialog.setMessage("Updating database!");

				Thread databaseThread = new Thread(null, updateDatabase,
						"DatabaseUpdate");
				databaseThread.start();
			}
			
			if (messageType.equals(UNDATA_COMPLETION_NOTIFICATION)) {
				unzipDialog.dismiss();
				
				finish();
			}
			
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		Intent i = getIntent();
		Bundle b = i.getExtras();

		thisPackage = (Package) b.getSerializable("package");
		thisPackageContentURL = CONTENT_FILE_URL.replace("#", thisPackage
				.getContent_file_name());

		Log.d(TAG, "Content file: " + thisPackage.getContent_file_name());

		if (INETTools.hasInternet(this)) {
			Thread downloadThread = new Thread(null, fetchContent,
					"ContentFetch");
			showDialog(DIALOG_DOWNLOAD_PROGRESS);
			downloadThread.start();

		} else {
			showDialog(DIALOG_NO_INTERNET);
		}
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "Stopping the install tag.");
		
		super.onStop();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_DOWNLOAD_PROGRESS:
			downloadDialog = new ProgressDialog(this);
			downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			downloadDialog
					.setMessage("Downloading contents.\nHold on a jiff...");
			downloadDialog.setCancelable(true);

			return downloadDialog;

		case DIALOG_UNZIP_PROGRESS:
			unzipDialog = new ProgressDialog(this);
			unzipDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			unzipDialog.setMessage("Unzipping contents.\nHold on a jiff...");
			unzipDialog.setCancelable(true);

			return unzipDialog;

		case DIALOG_NO_INTERNET:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("No connection to the Internet!").setCancelable(
					false).setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							finish();
						}
					});

			Dialog dialog = builder.create();
			return dialog;

		default:
			return null;
		}

	}

}
