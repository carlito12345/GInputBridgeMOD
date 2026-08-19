package com.salat.gbinder.gmp;

interface IMusicQueryCallback {
    void onSuccess(int code, String result);
    void onError(int code);
}
