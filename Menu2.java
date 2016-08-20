package com.example.chaneric.sshtest3;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class Menu2 extends AppCompatActivity {
    Session session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu2);
        try {
            session = new startConnection().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    public void capture(View view) throws ExecutionException, InterruptedException {
        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
            channel.setCommand("raspistill -n -t 1 -o image.jpg");
            channel.connect();
            channel.disconnect();
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void getPhoto(View view) throws ExecutionException, InterruptedException, SftpException {
        try {
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            sftpChannel.get("/home/pi/Downloads/notif.jpg", Environment.getExternalStorageDirectory() + "/SSHTest/notif.jpg");
            Log.d("image.jpg", " has been downloaded");



            ImageView imageView = new ImageView(getApplicationContext());
            Bitmap image = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() +"/SSHTest/notif.jpg");
            imageView.setImageBitmap(image);

            Dialog builder = new Dialog(this);
            builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
            builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {

                }
            });
            builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            builder.show();


            sftpChannel.exit();
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    public void readFile(View view) throws ExecutionException, InterruptedException, SftpException, IOException {
        try {
            Channel channel = session.openChannel("sftp");
            channel.connect();
            String output = "0";
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            while (output.equals("0")) {
                Thread.sleep(5000);
                InputStream input = sftpChannel.get("/home/pi/Downloads/notifs");
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(input));
                    output = br.readLine();
                    if (output.equals("1")) {
                        try {
                            ChannelExec channel2 = (ChannelExec) session.openChannel("exec");
                            BufferedReader in = new BufferedReader(new InputStreamReader(channel2.getInputStream()));
                            channel2.setCommand("cd Downloads;echo 0 > notifs");
                            channel2.connect();
                            channel2.disconnect();
                        } catch (JSchException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } finally {
                    input.close();
                }
            }
            sftpChannel.exit();
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    public void poll(View view) {
        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
            channel.setCommand("echo Hello >> notifs");
            channel.connect();
            channel.disconnect();
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class startConnection extends AsyncTask<Session, Session, Session> {

        @Override
        protected Session doInBackground(Session... voids) {
            Session session;
            try {
                Properties props = new Properties();
                props.put("StrictHostKeyChecking", "no");

                String host = "192.168.1.62";
                String user = "pi";
                String pwd = "raspberry";
                int port = 22;

                JSch jsch = new JSch();
                session = jsch.getSession(user, host, port);
                session.setConfig(props);
                session.setPassword(pwd);
                session.connect();
                return session;
            } catch (JSchException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
