package com.xyoye.dandanplay.utils.torrent.info;

import org.libtorrent4j.PeerInfo;
import org.libtorrent4j.PieceIndexBitfield;
import org.libtorrent4j.swig.peer_info;

public class TorrentPeerInfo extends PeerInfo {
    protected int port;
    protected PieceIndexBitfield pieces;
    protected boolean isUtp;

    public TorrentPeerInfo(peer_info p) {
        super(p);

        port = p.getIp().port();
        pieces = new PieceIndexBitfield(p.getPieces());
        isUtp = p.getFlags().and_(peer_info.utp_socket).nonZero();
    }

    public int port() {
        return port;
    }

    public PieceIndexBitfield pieces() {
        return pieces;
    }

    public boolean isUtp() {
        return isUtp;
    }
}
