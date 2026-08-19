package com.salat.gbinder.gmp;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMusicUserInfoListener extends IInterface {

    void onUserInfoResult(LoginUserInfo userInfo) throws RemoteException;

    public static abstract class Stub extends Binder implements IMusicUserInfoListener {
        private static final String DESCRIPTOR = "com.salat.gbinder.gmp.IMusicUserInfoListener";

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMusicUserInfoListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && (iin instanceof IMusicUserInfoListener)) {
                return (IMusicUserInfoListener) iin;
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
                    LoginUserInfo _arg0 = data.readInt() != 0 ? LoginUserInfo.CREATOR.createFromParcel(data) : null;
                    onUserInfoResult(_arg0);
                    reply.writeNoException();
                    return true;
    }
            }
            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements IMusicUserInfoListener {
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
            public void onUserInfoResult(LoginUserInfo userInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    if (userInfo != null) {
                        _data.writeInt(1);
                        userInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
