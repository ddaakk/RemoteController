package com.example.ownclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.sax.StartElementListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.erz.joysticklibrary.JoyStick;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private TextView mtv_status;
    private Button mbtn_connect;
    private Button mbtn_quit;
    private Button mbtn_showMsg;
    private Button mbtn_leftClick;
    private Button mbtn_rightClick;
    private Button mbtn_scrollUp;
    private Button mbtn_scrollDown;
    private EditText sMsg;
    private ProgressDialog pro;

    private Socket socket = null;
    private ConnectivityManager cm;
    private DisplayHandler h;
    private Timer tm;

    private int PORT = 9000;
    private int Delay = 3000;
    private int rx;
    private int ry;
    private final Point pt = new Point(0, 0);
    private String IP = "10.0.2.2"; /*-------------------- LOCAL IP --------------------*/
    private boolean isConnected = false;
    private boolean isWifi = false;
    private boolean dialogShow = false;

    //SEND
    final static int CON=0x01;
    final static int MOUSE_REQ=0x02;
    final static int SEVER_EXIT=0x04;
    final static int SHOW_MESSAGE=0x08;
    final static int LEFT_CLICK=0x16;
    final static int RIGHT_CLICK=0x32;
    final static int SCROLL_UP=0x64;
    final static int SCROLL_DOWN=0x128;

    //RECV
    final static int MOUSE_POS=0x01;

    public void confirmDialog(final String msg) {
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        dialogShow = false;
                    }
                });
                alert.setMessage(msg);
                alert.show();
                dialogShow = true;
            }
        }, 0);

    }

    class DisplayHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what==0){
                pro.cancel();
            }
        }
    }
    class NetworkThread extends Thread {
        @Override
        public void run() {
            super.run();
            SocketAddress socketAddress = new InetSocketAddress(IP, PORT);
            socket = new Socket();
            try {
                socket.setSoTimeout(Delay);
                socket.connect(socketAddress, Delay);

                tm = new Timer();
                tm.schedule(new MouseTimer(), 0, 100);

                h.sendEmptyMessage(0);

            } catch (SocketTimeoutException e) {
                pro.dismiss();
                if (!dialogShow) {
                    confirmDialog("Connection Failed as TimeOut");
                    return;
                }
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class MoveMouseThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeInt(MOUSE_REQ);
                dos.writeInt(pt.x);
                dos.writeInt(pt.y);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class SendDialog extends Thread {
        String m_Text;
        @Override
        public void run() {
            super.run();
            try {
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeInt(SHOW_MESSAGE);
                dos.writeUTF(sMsg.getText().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class LeftClick extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeInt(LEFT_CLICK);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class RightClick extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeInt(RIGHT_CLICK);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ScrollUp extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeInt(SCROLL_UP);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ScrollDown extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeInt(SCROLL_DOWN);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class MouseTimer extends TimerTask {
        @Override
        public void run() {
            if (socket != null) {
                try {
                    DataInputStream dis = new DataInputStream(socket.getInputStream());
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                    dos.writeInt(CON); // SendOpcode

                    int opcode = dis.readInt(); // ReadOpcode
                    if (opcode == MOUSE_POS) {
                        rx = dis.readInt();
                        ry = dis.readInt();
                    }
                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mtv_status.setText(new String("Status: " + isConnected + ", " + "Wifi:" + isWifi + ", x:" + rx + ", y:" + ry));
                        }
                    }, 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ServerExitThread extends Thread {
        @Override
        public void run() {
            super.run();
            tm.cancel();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            DataOutputStream dos = null;
            try {
                dos = new DataOutputStream(socket.getOutputStream());
                dos.writeInt(SEVER_EXIT);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mtv_status = findViewById(R.id.textView);
        mbtn_connect = findViewById(R.id.button);
        mbtn_quit = findViewById(R.id.button2);
        mbtn_showMsg = findViewById(R.id.button5);
        mbtn_leftClick = findViewById(R.id.button3);
        mbtn_rightClick = findViewById(R.id.button4);
        mbtn_scrollUp = findViewById(R.id.button6);
        mbtn_scrollDown = findViewById(R.id.button7);
        sMsg = findViewById(R.id.editText3);


        mbtn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pro = ProgressDialog.show(MainActivity.this,null,"Connecting..");
                NetworkThread thread = new NetworkThread();
                thread.start();
            }
        });
        mbtn_quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!dialogShow) {
                    if (socket == null) {
                        confirmDialog("Connect First");
                        return;
                    }
                } else
                    return;
                ServerExitThread thread = new ServerExitThread();
                thread.start();
            }
        });
        mbtn_showMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!dialogShow) {
                    if (socket == null) {
                        confirmDialog("Connect First");
                        return;
                    }
                } else
                    return;
                SendDialog thread = new SendDialog();
                thread.start();
            }
        });

        mbtn_leftClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!dialogShow) {
                    if (socket == null) {
                        confirmDialog("Connect First");
                        return;
                    }
                } else
                    return;
                LeftClick thread = new LeftClick();
                thread.start();
            }
        });

        mbtn_rightClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!dialogShow) {
                    if (socket == null) {
                        confirmDialog("Connect First");
                        return;
                    }
                } else
                    return;
                RightClick thread = new RightClick();
                thread.start();
            }
        });

        mbtn_scrollUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!dialogShow) {
                    if (socket == null) {
                        confirmDialog("Connect First");
                        return;
                    }
                } else
                    return;
                ScrollUp thread = new ScrollUp();
                thread.start();
            }
        });

        mbtn_scrollDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!dialogShow) {
                    if (socket == null) {
                        confirmDialog("Connect First");
                        return;
                    }
                } else
                    return;
                ScrollDown thread = new ScrollDown();
                thread.start();
            }
        });

        JoyStick joyStick = (JoyStick) findViewById(R.id.joy1);
        JoyStick.JoyStickListener jl = new JoyStick.JoyStickListener() {
            @Override
            public void onMove(JoyStick joyStick, double angle, double power, int direction) {
                if (!dialogShow) {
                    if (socket == null) {
                        confirmDialog("Connect First");
                        return;
                    }
                } else
                    return;
                switch(direction) {
                    case JoyStick.DIRECTION_CENTER:
                        break;
                    case JoyStick.DIRECTION_LEFT: { pt.x--; break; }
                    case JoyStick.DIRECTION_LEFT_UP: { pt.x--; pt.y--; break; }
                    case JoyStick.DIRECTION_UP: { pt.y--; break; }
                    case JoyStick.DIRECTION_UP_RIGHT: { pt.x++; pt.y--; break; }
                    case JoyStick.DIRECTION_RIGHT: { pt.x++; break; }
                    case JoyStick.DIRECTION_RIGHT_DOWN: { pt.x++; pt.y++; break; }
                    case JoyStick.DIRECTION_DOWN: { pt.y++; break; }
                    case JoyStick.DIRECTION_DOWN_LEFT: { pt.x--; pt.y++; break; }
                }
                MoveMouseThread thread = new MoveMouseThread();
                thread.start();
                return;
            }

            @Override
            public void onTap() {
            }

            @Override
            public void onDoubleTap() {
            }
        };
        joyStick.setListener(jl);

        joyStick.setOnTouchListener(new JoyStick.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_UP)
                    pt.set(0, 0);
                return false;
            }
        });

        cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        isWifi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        mtv_status.setText("Status: "+isConnected+", "+"Wifi:"+isWifi);

        h = new DisplayHandler();
    }
}