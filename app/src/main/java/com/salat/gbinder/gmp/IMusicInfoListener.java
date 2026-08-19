package com.salat.gbinder.gmp;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IMusicInfoListener extends IInterface {

    void onMusicListResult(List musicList, String source, String displayId) throws RemoteException;

    void onMusicCurrentMediaInfoResult(MusicInfo musicInfo, String source, String displayId) throws RemoteException;

    public static abstract class Stub extends Binder implements IMusicInfoListener {
        private static final String DESCRIPTOR = "com.salat.gbinder.gmp.IMusicInfoListener";

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMusicInfoListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && (iin instanceof IMusicInfoListener)) {
                return (IMusicInfoListener) iin;
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
                    List _arg0 = data.readArrayList(getClass().getClassLoader());
                    String _arg1 = data.readString();
                    String _arg2 = data.readString();
                    onMusicListResult(_arg0, _arg1, _arg2);
                    reply.writeNoException();
                    return true;
    }
                case 2:
                    {
                        data.enforceInterface(DESCRIPTOR);
                        MusicInfo _arg0 = data.readInt() != 0 ? MusicInfo.CREATOR.createFromParcel(data) : null;
                        String _arg1 = data.readString();
                        String _arg2 = data.readString();
                        onMusicCurrentMediaInfoResult(_arg0, _arg1, _arg2);
                        reply.writeNoException();
                        return true;
                    }
            }
            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements IMusicInfoListener {
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
            public void onMusicListResult(List musicList, String source, String displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeList(musicList);
                    _data.writeString(source);
                    _data.writeString(displayId);
                    mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void onMusicCurrentMediaInfoResult(MusicInfo musicInfo, String source, String displayId) throws RemoteException {
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
                    mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
