package com.salat.gbinder.gmp;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IMusicManager extends IInterface {

    void doPlay(int position, MusicInfo musicInfo) throws RemoteException;

    void play() throws RemoteException;

    void pause() throws RemoteException;

    void pre() throws RemoteException;

    void next() throws RemoteException;

    void seekTo(long position) throws RemoteException;

    void fastForward(long time) throws RemoteException;

    void fastRewind(long time) throws RemoteException;

    void activeLastSource(boolean autoPlay) throws RemoteException;

    void getPlayList(IMusicInfoListener listener) throws RemoteException;

    void addFavor(String source) throws RemoteException;

    void cancelFavor(String source) throws RemoteException;

    void getFavor() throws RemoteException;

    int getCurrentPlayState(String source) throws RemoteException;

    boolean supportLastSource() throws RemoteException;

    long getCurrentPosition() throws RemoteException;

    void getCurrentMediaInfo(String source) throws RemoteException;

    void startApp() throws RemoteException;

    void setPlayMode(int playMode) throws RemoteException;

    int getPlayMode(String source) throws RemoteException;

    void requestSource() throws RemoteException;

    void getOnlineUserInfo(IMusicUserInfoListener listener) throws RemoteException;

    void addListener(IMusicStateListener stateListener, IMusicInfoListener infoListener, IMusicUserInfoListener userInfoListener) throws RemoteException;

    void getContent(String contentId, IMusicStateListener listener) throws RemoteException;

    void playContent(int type, String id, int position, boolean autoPlay) throws RemoteException;

    void setSource(String source, String extra) throws RemoteException;

    String getSource(String extra) throws RemoteException;

    void searchMusicByName(String keyword, IMusicStateListener listener) throws RemoteException;

    void playMusicByNameAndPosition(String name, int position) throws RemoteException;

    void openFavor() throws RemoteException;

    void openHistory() throws RemoteException;

    List getCommonUseSource(String source) throws RemoteException;

    void searchAndPlayMusicFromFlow(String keyword) throws RemoteException;

    void openChannelTab(String tab) throws RemoteException;

    void openPlayList() throws RemoteException;

    void playFavor() throws RemoteException;

    void semanticSearch(String keyword, String artist, String album, String extra) throws RemoteException;

    void semanticSearchV2(String keyword, IMusicQueryCallback callback) throws RemoteException;

    void screenChange(int screenState) throws RemoteException;

    void openLoginPage() throws RemoteException;

    void replayCurrent() throws RemoteException;

    void openLyric() throws RemoteException;

    void semanticSearchAndPlay(String keyword, List words, String extra, boolean autoPlay, boolean fromVr, String scene, IMusicQueryCallback callback) throws RemoteException;

    void switchSourceQuality(int quality) throws RemoteException;

    void setSourceAndPlay(String source, boolean autoPlay, String extra) throws RemoteException;

    int onUIWordingTriggered(String wording) throws RemoteException;

    void closeLikeList() throws RemoteException;

    void openHistoryList(int type, boolean autoPlay) throws RemoteException;

    boolean isUserMusicVip() throws RemoteException;

    boolean isVipQuality(int quality) throws RemoteException;

    boolean isAgreedUserProtocol() throws RemoteException;

    int isSupportChangeMode(int source) throws RemoteException;

    boolean isMusicQualitySwitch(int quality) throws RemoteException;

    int notifyVrStatusNotifierStatus(int status) throws RemoteException;

    MusicInfo queryMediaInfoSync() throws RemoteException;

    String getAppAudioStatus(String source) throws RemoteException;

    void setFlowAudioStatus(boolean active) throws RemoteException;

    public static abstract class Stub extends Binder implements IMusicManager {
        private static final String DESCRIPTOR = "com.salat.gbinder.gmp.IMusicManager";

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMusicManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && (iin instanceof IMusicManager)) {
                return (IMusicManager) iin;
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
                    MusicInfo _arg1 = data.readInt() != 0 ? MusicInfo.CREATOR.createFromParcel(data) : null;
                    doPlay(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
    }
                case 2:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    play();
                    reply.writeNoException();
                    return true;
    }
                case 3:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    pause();
                    reply.writeNoException();
                    return true;
    }
                case 4:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    pre();
                    reply.writeNoException();
                    return true;
    }
                case 5:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    next();
                    reply.writeNoException();
                    return true;
    }
                case 6:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    seekTo(data.readLong());
                    reply.writeNoException();
                    return true;
    }
                case 7:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    fastForward(data.readLong());
                    reply.writeNoException();
                    return true;
    }
                case 8:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    fastRewind(data.readLong());
                    reply.writeNoException();
                    return true;
    }
                case 9:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    activeLastSource(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
    }
                case 10:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    getPlayList(IMusicInfoListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
    }
                case 11:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    addFavor(data.readString());
                    reply.writeNoException();
                    return true;
    }
                case 12:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    cancelFavor(data.readString());
                    reply.writeNoException();
                    return true;
    }
                case 13:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    getFavor();
                    reply.writeNoException();
                    return true;
    }
                case 14:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    int _result = getCurrentPlayState(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
    }
                case 15:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = supportLastSource();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
    }
                case 16:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    long _result = getCurrentPosition();
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
    }
                case 17:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    getCurrentMediaInfo(data.readString());
                    reply.writeNoException();
                    return true;
    }
                case 18:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    startApp();
                    reply.writeNoException();
                    return true;
    }
                case 19:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    setPlayMode(data.readInt());
                    reply.writeNoException();
                    return true;
    }
                case 20:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    int _result = getPlayMode(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
    }
                case 21:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    requestSource();
                    reply.writeNoException();
                    return true;
    }
                case 22:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    getOnlineUserInfo(IMusicUserInfoListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
    }
                case 23:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    IMusicStateListener _arg0 = IMusicStateListener.Stub.asInterface(data.readStrongBinder());
                    IMusicInfoListener _arg1 = IMusicInfoListener.Stub.asInterface(data.readStrongBinder());
                    IMusicUserInfoListener _arg2 = IMusicUserInfoListener.Stub.asInterface(data.readStrongBinder());
                    addListener(_arg0, _arg1, _arg2);
                    reply.writeNoException();
                    return true;
    }
                case 24:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    getContent(data.readString(), IMusicStateListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
    }
                case 25:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    String _arg1 = data.readString();
                    int _arg2 = data.readInt();
                    boolean _arg3 = data.readInt() != 0;
                    playContent(_arg0, _arg1, _arg2, _arg3);
                    reply.writeNoException();
                    return true;
    }
                case 26:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    setSource(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
    }
                case 27:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    String _result = getSource(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
    }
                case 28:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    searchMusicByName(data.readString(), IMusicStateListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
    }
                case 29:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    playMusicByNameAndPosition(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
    }
                case 30:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    openFavor();
                    reply.writeNoException();
                    return true;
    }
                case 31:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    openHistory();
                    reply.writeNoException();
                    return true;
    }
                case 32:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    List _result = getCommonUseSource(data.readString());
                    reply.writeNoException();
                    reply.writeList(_result);
                    return true;
    }
                case 33:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    searchAndPlayMusicFromFlow(data.readString());
                    reply.writeNoException();
                    return true;
    }
                case 34:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    openChannelTab(data.readString());
                    reply.writeNoException();
                    return true;
    }
                case 35:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    openPlayList();
                    reply.writeNoException();
                    return true;
    }
                case 36:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    playFavor();
                    reply.writeNoException();
                    return true;
    }
                case 37:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    String _arg0 = data.readString();
                    String _arg1 = data.readString();
                    String _arg2 = data.readString();
                    String _arg3 = data.readString();
                    semanticSearch(_arg0, _arg1, _arg2, _arg3);
                    reply.writeNoException();
                    return true;
    }
                case 38:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    semanticSearchV2(data.readString(), IMusicQueryCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
    }
                case 39:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    screenChange(data.readInt());
                    reply.writeNoException();
                    return true;
    }
                case 40:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    openLoginPage();
                    reply.writeNoException();
                    return true;
    }
                case 41:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    replayCurrent();
                    reply.writeNoException();
                    return true;
    }
                case 42:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    openLyric();
                    reply.writeNoException();
                    return true;
    }
                case 43:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    String _arg0 = data.readString();
                    List _arg1 = data.readArrayList(getClass().getClassLoader());
                    String _arg2 = data.readString();
                    boolean _arg3 = data.readInt() != 0;
                    boolean _arg4 = data.readInt() != 0;
                    String _arg5 = data.readString();
                    IMusicQueryCallback _arg6 = IMusicQueryCallback.Stub.asInterface(data.readStrongBinder());
                    semanticSearchAndPlay(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6);
                    reply.writeNoException();
                    return true;
    }
                case 44:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    switchSourceQuality(data.readInt());
                    reply.writeNoException();
                    return true;
    }
                case 45:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    String _arg0 = data.readString();
                    boolean _arg1 = data.readInt() != 0;
                    String _arg2 = data.readString();
                    setSourceAndPlay(_arg0, _arg1, _arg2);
                    reply.writeNoException();
                    return true;
    }
                case 46:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    int _result = onUIWordingTriggered(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
    }
                case 47:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    closeLikeList();
                    reply.writeNoException();
                    return true;
    }
                case 48:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    boolean _arg1 = data.readInt() != 0;
                    openHistoryList(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
    }
                case 49:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = isUserMusicVip();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
    }
                case 50:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = isVipQuality(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
    }
                case 51:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = isAgreedUserProtocol();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
    }
                case 52:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    int _result = isSupportChangeMode(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
    }
                case 53:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = isMusicQualitySwitch(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
    }
                case 54:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    int _result = notifyVrStatusNotifierStatus(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
    }
                case 55:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    MusicInfo _result = queryMediaInfoSync();
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
    }
                case 56:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    String _result = getAppAudioStatus(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
    }
                case 57:
                    {
                    data.enforceInterface(DESCRIPTOR);
                    setFlowAudioStatus(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
    }
            }
            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements IMusicManager {
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
            public void doPlay(int position, MusicInfo musicInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(position);
                    if (musicInfo != null) {
                        _data.writeInt(1);
                        musicInfo.writeToParcel(_data, 0);
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

            @Override
            public void play() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void pause() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void pre() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void next() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void seekTo(long position) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeLong(position);
                    mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void fastForward(long time) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeLong(time);
                    mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void fastRewind(long time) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeLong(time);
                    mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void activeLastSource(boolean autoPlay) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(autoPlay ? 1 : 0);
                    mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void getPlayList(IMusicInfoListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void addFavor(String source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(source);
                    mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void cancelFavor(String source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(source);
                    mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void getFavor() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public int getCurrentPlayState(String source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(source);
                    mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public boolean supportLastSource() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt() != 0;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public long getCurrentPosition() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                long _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public void getCurrentMediaInfo(String source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(source);
                    mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void startApp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void setPlayMode(int playMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(playMode);
                    mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public int getPlayMode(String source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(source);
                    mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public void requestSource() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void getOnlineUserInfo(IMusicUserInfoListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void addListener(IMusicStateListener stateListener, IMusicInfoListener infoListener, IMusicUserInfoListener userInfoListener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeStrongBinder(stateListener != null ? stateListener.asBinder() : null);
                    _data.writeStrongBinder(infoListener != null ? infoListener.asBinder() : null);
                    _data.writeStrongBinder(userInfoListener != null ? userInfoListener.asBinder() : null);
                    mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void getContent(String contentId, IMusicStateListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(contentId);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void playContent(int type, String id, int position, boolean autoPlay) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(id);
                    _data.writeInt(position);
                    _data.writeInt(autoPlay ? 1 : 0);
                    mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void setSource(String source, String extra) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(source);
                    _data.writeString(extra);
                    mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public String getSource(String extra) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                String _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(extra);
                    mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public void searchMusicByName(String keyword, IMusicStateListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(keyword);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void playMusicByNameAndPosition(String name, int position) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(position);
                    mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void openFavor() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void openHistory() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public List getCommonUseSource(String source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                List _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(source);
                    mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readArrayList(getClass().getClassLoader());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public void searchAndPlayMusicFromFlow(String keyword) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(keyword);
                    mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void openChannelTab(String tab) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(tab);
                    mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void openPlayList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void playFavor() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void semanticSearch(String keyword, String artist, String album, String extra) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(keyword);
                    _data.writeString(artist);
                    _data.writeString(album);
                    _data.writeString(extra);
                    mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void semanticSearchV2(String keyword, IMusicQueryCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(keyword);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void screenChange(int screenState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(screenState);
                    mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void openLoginPage() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void replayCurrent() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void openLyric() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void semanticSearchAndPlay(String keyword, List words, String extra, boolean autoPlay, boolean fromVr, String scene, IMusicQueryCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(keyword);
                    _data.writeList(words);
                    _data.writeString(extra);
                    _data.writeInt(autoPlay ? 1 : 0);
                    _data.writeInt(fromVr ? 1 : 0);
                    _data.writeString(scene);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void switchSourceQuality(int quality) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(quality);
                    mRemote.transact(44, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void setSourceAndPlay(String source, boolean autoPlay, String extra) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(source);
                    _data.writeInt(autoPlay ? 1 : 0);
                    _data.writeString(extra);
                    mRemote.transact(45, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public int onUIWordingTriggered(String wording) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(wording);
                    mRemote.transact(46, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public void closeLikeList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(47, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void openHistoryList(int type, boolean autoPlay) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(autoPlay ? 1 : 0);
                    mRemote.transact(48, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public boolean isUserMusicVip() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(49, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt() != 0;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public boolean isVipQuality(int quality) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(quality);
                    mRemote.transact(50, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt() != 0;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public boolean isAgreedUserProtocol() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(51, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt() != 0;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public int isSupportChangeMode(int source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(source);
                    mRemote.transact(52, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public boolean isMusicQualitySwitch(int quality) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(quality);
                    mRemote.transact(53, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt() != 0;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public int notifyVrStatusNotifierStatus(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(status);
                    mRemote.transact(54, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public MusicInfo queryMediaInfoSync() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                MusicInfo _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(55, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt() != 0 ? MusicInfo.CREATOR.createFromParcel(_reply) : null;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public String getAppAudioStatus(String source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                String _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(source);
                    mRemote.transact(56, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public void setFlowAudioStatus(boolean active) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(active ? 1 : 0);
                    mRemote.transact(57, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
