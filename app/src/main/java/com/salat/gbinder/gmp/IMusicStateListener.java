package com.salat.gbinder.gmp;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IMusicStateListener extends IInterface {

    void onMediaDataChanged() throws RemoteException;

    void onProgressChanged(long position, long duration, String source, String displayId) throws RemoteException;

    void onPlayStateChanged(int state, String source, String displayId) throws RemoteException;

    void onPlayListChanged(String source, String displayId) throws RemoteException;

    void onFavorStateChanged(boolean isFavor, String source, String id, String displayId) throws RemoteException;

    void onLrcLoad(String lrc, long duration, String source, String displayId) throws RemoteException;

    void onPlayModeChange(int playMode, String source, String displayId) throws RemoteException;

    void onAudioFocusChange(int focusChange, String source, String displayId) throws RemoteException;

    void onContentListResult(String result) throws RemoteException;

    void onMediaChange(MusicInfo musicInfo, String source, String displayId) throws RemoteException;

    void onSourceChange(String source, String displayId) throws RemoteException;

    void onSearchResult(List result, String source) throws RemoteException;

    void onAppChanged(boolean isInApp, String source) throws RemoteException;

    void onAppDied(String source) throws RemoteException;

    public static abstract class Stub extends Binder implements IMusicStateListener {
        private static final String DESCRIPTOR = "com.salat.gbinder.gmp.IMusicStateListener";

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMusicStateListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && (iin instanceof IMusicStateListener)) {
                return (IMusicStateListener) iin;
            }
            return new Proxy(obj);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case INTERFACE_TRANSACTION:
                    reply.writeString(DESCRIPTOR);
                    return true;
                case 1:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    onMediaDataChanged();
                    reply.writeNoException();
                    return true;
    }
                case 2:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    long _arg0 = data.readLong();
                    long _arg1 = data.readLong();
                    String _arg2 = data.readString();
                    String _arg3 = data.readString();
                    onProgressChanged(_arg0, _arg1, _arg2, _arg3);
                    reply.writeNoException();
                    return true;
    }
                case 3:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    String _arg1 = data.readString();
                    String _arg2 = data.readString();
                    onPlayStateChanged(_arg0, _arg1, _arg2);
                    reply.writeNoException();
                    return true;
    }
                case 4:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    onPlayListChanged(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
    }
                case 5:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _arg0 = data.readInt() != 0;
                    String _arg1 = data.readString();
                    String _arg2 = data.readString();
                    String _arg3 = data.readString();
                    onFavorStateChanged(_arg0, _arg1, _arg2, _arg3);
                    reply.writeNoException();
                    return true;
    }
                case 6:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    String _arg0 = data.readString();
                    long _arg1 = data.readLong();
                    String _arg2 = data.readString();
                    String _arg3 = data.readString();
                    onLrcLoad(_arg0, _arg1, _arg2, _arg3);
                    reply.writeNoException();
                    return true;
    }
                case 7:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    String _arg1 = data.readString();
                    String _arg2 = data.readString();
                    onPlayModeChange(_arg0, _arg1, _arg2);
                    reply.writeNoException();
                    return true;
    }
                case 8:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    String _arg1 = data.readString();
                    String _arg2 = data.readString();
                    onAudioFocusChange(_arg0, _arg1, _arg2);
                    reply.writeNoException();
                    return true;
    }
                case 9:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    onContentListResult(data.readString());
                    reply.writeNoException();
                    return true;
    }
                case 10:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    MusicInfo _arg0 = data.readInt() != 0 ? MusicInfo.CREATOR.createFromParcel(data) : null;
                    String _arg1 = data.readString();
                    String _arg2 = data.readString();
                    onMediaChange(_arg0, _arg1, _arg2);
                    reply.writeNoException();
                    return true;
    }
                case 11:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    onSourceChange(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
    }
                case 12:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    List _arg0 = data.readArrayList(getClass().getClassLoader());
                    String _arg1 = data.readString();
                    onSearchResult(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
    }
                case 13:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _arg0 = data.readInt() != 0;
                    String _arg1 = data.readString();
                    onAppChanged(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
    }
                case 14:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    onAppDied(data.readString());
                    reply.writeNoException();
                    return true;
    }
            }
            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements IMusicStateListener {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                mRemote = remote;
            }

            @Override
            public IBinder asBinder() {
                return mRemote;
            }

            public String getInterfaceDescriptor() {
                return DESCRIPTOR;
            }

            @Override
            public void onMediaDataChanged() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void onProgressChanged(long position, long duration, String source, String displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeLong(position);
                    _data.writeLong(duration);
                    _data.writeString(source);
                    _data.writeString(displayId);
                    mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void onPlayStateChanged(int state, String source, String displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(state);
                    _data.writeString(source);
                    _data.writeString(displayId);
                    mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void onPlayListChanged(String source, String displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(source);
                    _data.writeString(displayId);
                    mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void onFavorStateChanged(boolean isFavor, String source, String id, String displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(isFavor ? 1 : 0);
                    _data.writeString(source);
                    _data.writeString(id);
                    _data.writeString(displayId);
                    mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void onLrcLoad(String lrc, long duration, String source, String displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(lrc);
                    _data.writeLong(duration);
                    _data.writeString(source);
                    _data.writeString(displayId);
                    mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void onPlayModeChange(int playMode, String source, String displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(playMode);
                    _data.writeString(source);
                    _data.writeString(displayId);
                    mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void onAudioFocusChange(int focusChange, String source, String displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(focusChange);
                    _data.writeString(source);
                    _data.writeString(displayId);
                    mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void onContentListResult(String result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(result);
                    mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void onMediaChange(MusicInfo musicInfo, String source, String displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    if (musicInfo != null) {
                        _data.writeInt(1);
                        musicInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(source);
                    _data.writeString(displayId);
                    mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void onSourceChange(String source, String displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(source);
                    _data.writeString(displayId);
                    mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void onSearchResult(List result, String source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeList(result);
                    _data.writeString(source);
                    mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void onAppChanged(boolean isInApp, String source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(isInApp ? 1 : 0);
                    _data.writeString(source);
                    mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void onAppDied(String source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(source);
                    mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
