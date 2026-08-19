package com.salat.gbinder.gmp;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMusicQueryCallback extends IInterface {

    void onSuccess(int code, String result) throws RemoteException;

    void onError(int code) throws RemoteException;

    public static abstract class Stub extends Binder implements IMusicQueryCallback {
        private static final String DESCRIPTOR = "com.salat.gbinder.gmp.IMusicQueryCallback";

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMusicQueryCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && (iin instanceof IMusicQueryCallback)) {
                return (IMusicQueryCallback) iin;
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
                    int _arg0 = data.readInt();
                    String _arg1 = data.readString();
                    onSuccess(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
    }
                case 2:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    onError(data.readInt());
                    reply.writeNoException();
                    return true;
    }
            }
            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements IMusicQueryCallback {
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
            public void onSuccess(int code, String result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(code);
                    _data.writeString(result);
                    mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void onError(int code) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(code);
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
