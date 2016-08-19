package com.example.chaneric.sshtest3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

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
            sftpChannel.get("/home/pi/image.jpg", "/storage/emulated/0/SSHTest/image.jpg");
            Log.d("image.jpg", " has been downloaded");
            File imgfile = new File("/storage/sdcard/SSHTest/image.jpg");




            sftpChannel.exit();
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
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

                String host = "raspberryPi";
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
