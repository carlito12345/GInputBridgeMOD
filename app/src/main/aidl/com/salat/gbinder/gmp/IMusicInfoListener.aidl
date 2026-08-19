package com.salat.gbinder.gmp;

import com.salat.gbinder.gmp.MusicInfo;

interface IMusicInfoListener {
    void onMusicListResult(in List musicList, String source, String displayId);
    void onMusicCurrentMediaInfoResult(in MusicInfo musicInfo, String source, String displayId);
}
